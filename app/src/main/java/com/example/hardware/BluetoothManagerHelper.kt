package com.example.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.model.BluetoothDeviceCategory
import com.example.model.BluetoothDeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/**
 * Battery-optimized Bluetooth device & audio manager.
 * Compatible with Android 4.4+ and modern Android.
 * Automatically terminates device discovery after 12s to conserve power.
 */
class BluetoothManagerHelper(private val context: Context) {

    private val tag = "BluetoothManager"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanCountdown = MutableStateFlow(0)
    val scanCountdown: StateFlow<Int> = _scanCountdown.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceInfo>> = _discoveredDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = _connectedDevice.asStateFlow()

    private var a2dpProfile: BluetoothProfile? = null
    private var scanTimeoutJob: Job? = null

    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
                    if (state == BluetoothAdapter.STATE_ON) {
                        refreshPairedDevices()
                    } else {
                        _isScanning.value = false
                        _discoveredDevices.value = emptyList()
                        _connectedDevice.value = null
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    _scanCountdown.value = 0
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        val name = try { device.name ?: "Unknown Device" } catch (_: SecurityException) { "Bluetooth Device" }
                        val category = categorizeDevice(device)
                        val battery = readBatteryLevel(intent, device)

                        val info = BluetoothDeviceInfo(
                            name = name,
                            address = device.address,
                            isBonded = device.bondState == BluetoothDevice.BOND_BONDED,
                            isConnected = false,
                            deviceCategory = category,
                            batteryPercent = battery
                        )

                        val currentList = _discoveredDevices.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { it.address == info.address }
                        if (existingIndex >= 0) {
                            currentList[existingIndex] = info
                        } else {
                            currentList.add(info)
                        }
                        _discoveredDevices.value = currentList
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshPairedDevices()
                }
            }
        }
    }

    init {
        registerReceiver()
        initA2dpProfile()
        refreshPairedDevices()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
    }

    private fun initA2dpProfile() {
        bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile == BluetoothProfile.A2DP) {
                    a2dpProfile = proxy
                    refreshConnectedDevices()
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.A2DP) {
                    a2dpProfile = null
                }
            }
        }, BluetoothProfile.A2DP)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
            a2dpProfile?.let { bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, it) }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            _pairedDevices.value = emptyList()
            return
        }
        try {
            val bonded = adapter.bondedDevices ?: emptySet()
            _pairedDevices.value = bonded.map { device ->
                val name = device.name ?: "Unknown Device"
                val isConnected = checkIsDeviceConnected(device)
                val info = BluetoothDeviceInfo(
                    name = name,
                    address = device.address,
                    isBonded = true,
                    isConnected = isConnected,
                    deviceCategory = categorizeDevice(device),
                    batteryPercent = -1
                )
                if (isConnected) {
                    _connectedDevice.value = info
                }
                info
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to get bonded devices: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshConnectedDevices() {
        val profile = a2dpProfile ?: return
        try {
            val connected = profile.connectedDevices
            if (!connected.isNullOrEmpty()) {
                val primary = connected.first()
                val info = BluetoothDeviceInfo(
                    name = primary.name ?: "Audio Device",
                    address = primary.address,
                    isBonded = true,
                    isConnected = true,
                    deviceCategory = categorizeDevice(primary),
                    batteryPercent = -1
                )
                _connectedDevice.value = info
            }
        } catch (e: Exception) {
            Log.w(tag, "Error reading A2DP connected devices: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun toggleBluetooth(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        return try {
            if (adapter.isEnabled) {
                stopScan()
                adapter.disable()
            } else {
                adapter.enable()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to toggle bluetooth: ${e.message}")
            false
        }
    }

    /**
     * Starts battery-saving device scan.
     * Enforces a strict 12-second countdown after which scanning automatically shuts off.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }

        _discoveredDevices.value = emptyList()
        val started = adapter.startDiscovery()
        if (started) {
            _isScanning.value = true
            _scanCountdown.value = 12

            scanTimeoutJob?.cancel()
            scanTimeoutJob = scope.launch {
                for (sec in 12 downTo 1) {
                    _scanCountdown.value = sec
                    delay(1000)
                }
                stopScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanTimeoutJob?.cancel()
        _scanCountdown.value = 0
        _isScanning.value = false
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (_: Exception) {}
    }

    /**
     * Initiates pairing or audio profile connection to the specified device.
     */
    @SuppressLint("MissingPermission")
    fun connectOrPair(deviceInfo: BluetoothDeviceInfo): Boolean {
        val adapter = bluetoothAdapter ?: return false
        stopScan() // Save battery immediately upon interaction

        try {
            val device = adapter.getRemoteDevice(deviceInfo.address)
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                // Initiate Android pairing (KitKat & modern compatible)
                return device.createBond()
            } else {
                // Try A2DP connect via reflection if bonded
                val profile = a2dpProfile
                if (profile != null) {
                    try {
                        val connectMethod: Method = profile.javaClass.getMethod("connect", BluetoothDevice::class.java)
                        connectMethod.invoke(profile, device)
                        _connectedDevice.value = deviceInfo.copy(isConnected = true)
                        return true
                    } catch (e: Exception) {
                        Log.d(tag, "Reflection connect: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Connection failed: ${e.message}")
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice(deviceInfo: BluetoothDeviceInfo): Boolean {
        val profile = a2dpProfile ?: return false
        val adapter = bluetoothAdapter ?: return false
        return try {
            val device = adapter.getRemoteDevice(deviceInfo.address)
            val disconnectMethod: Method = profile.javaClass.getMethod("disconnect", BluetoothDevice::class.java)
            disconnectMethod.invoke(profile, device)
            if (_connectedDevice.value?.address == deviceInfo.address) {
                _connectedDevice.value = null
            }
            refreshPairedDevices()
            true
        } catch (e: Exception) {
            Log.e(tag, "Disconnect error: ${e.message}")
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkIsDeviceConnected(device: BluetoothDevice): Boolean {
        val profile = a2dpProfile ?: return false
        return try {
            profile.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
        } catch (_: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun categorizeDevice(device: BluetoothDevice): BluetoothDeviceCategory {
        val btClass = device.bluetoothClass ?: return BluetoothDeviceCategory.GENERIC
        val major = btClass.majorDeviceClass
        val deviceClass = btClass.deviceClass

        return when {
            major == BluetoothClass.Device.Major.AUDIO_VIDEO -> {
                when (deviceClass) {
                    BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES,
                    BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> BluetoothDeviceCategory.AUDIO_HEADPHONES
                    BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> BluetoothDeviceCategory.AUDIO_SPEAKER
                    BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> BluetoothDeviceCategory.CAR_AUDIO
                    else -> BluetoothDeviceCategory.AUDIO_OTHER
                }
            }
            else -> BluetoothDeviceCategory.GENERIC
        }
    }

    @SuppressLint("MissingPermission")
    private fun readBatteryLevel(intent: Intent, device: BluetoothDevice): Int {
        // First check intent extras
        val extraBattery = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
        if (extraBattery in 0..100) return extraBattery

        // Try reflection
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val level = method.invoke(device) as? Int ?: -1
            if (level in 0..100) level else -1
        } catch (_: Exception) {
            -1
        }
    }
}
