package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardware.BatteryManagerHelper
import com.example.hardware.BluetoothManagerHelper
import com.example.hardware.FmChipManager
import com.example.hardware.WifiManagerHelper
import com.example.model.AutoSwitchConfig
import com.example.model.BluetoothDeviceInfo
import com.example.model.PowerMode
import com.example.model.RadioStation
import com.example.model.WifiGlobalSecurityConfig
import com.example.ui.theme.KthemePack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    WIFI("Wi-Fi"),
    FM_RADIO("FM Radio"),
    BLUETOOTH("Bluetooth"),
    POWER("Power"),
    SETTINGS("Ktheme & Security")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val wifiHelper = WifiManagerHelper(application)
    val fmHelper = FmChipManager(application)
    val bluetoothHelper = BluetoothManagerHelper(application)
    val batteryHelper = BatteryManagerHelper(application)

    private val _activeTab = MutableStateFlow(AppTab.WIFI)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _activeKtheme = MutableStateFlow(KthemePack.TERMINAL_NEON)
    val activeKtheme: StateFlow<KthemePack> = _activeKtheme.asStateFlow()

    private val _statusNotification = MutableStateFlow<String?>(null)
    val statusNotification: StateFlow<String?> = _statusNotification.asStateFlow()

    fun selectTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun setKtheme(pack: KthemePack) {
        _activeKtheme.value = pack
        showNotification("Theme switched: ${pack.displayName}")
    }

    fun showNotification(msg: String) {
        _statusNotification.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(3500)
            if (_statusNotification.value == msg) {
                _statusNotification.value = null
            }
        }
    }

    // Wi-Fi Actions
    fun toggleWifi(turnOn: Boolean? = null) {
        val willBeOn = turnOn ?: !wifiHelper.isWifiEnabled.value
        wifiHelper.toggleWifi(willBeOn)
        showNotification(if (willBeOn) "Wi-Fi turned ON" else "Wi-Fi turned OFF")
    }

    fun scanWifi() {
        wifiHelper.startScan()
        showNotification("Scanning for Wi-Fi networks...")
    }

    fun connectWifi(ssid: String, password: String?) {
        showNotification("Connecting to $ssid...")
        val ok = wifiHelper.connectToNetwork(ssid, password)
        if (ok) {
            showNotification("Connection initiated for $ssid")
        } else {
            showNotification("Failed to connect to $ssid")
        }
    }

    fun disconnectWifi() {
        wifiHelper.disconnectCurrent()
        showNotification("Wi-Fi disconnected")
    }

    fun setAutoSwitchConfig(config: AutoSwitchConfig) {
        wifiHelper.updateAutoSwitchConfig(config)
        showNotification(if (config.isEnabled) "Smart Auto-Switch enabled" else "Smart Auto-Switch paused")
    }

    fun updateGlobalSecurityConfig(config: WifiGlobalSecurityConfig) {
        wifiHelper.updateGlobalSecurityConfig(config)
        showNotification("Global Wi-Fi security profile updated")
    }

    fun launchCaptivePortal(url: String?) {
        val targetUrl = url ?: "http://connectivitycheck.gstatic.com/generate_204"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            showNotification("Cannot launch browser for portal: $targetUrl")
        }
    }

    // FM Radio & Online Directory Actions
    fun launchOemRadio(): Boolean {
        val launched = fmHelper.launchOemRadioApp()
        if (launched) {
            showNotification("Launching native Timkoo FM Radio app")
        } else {
            showNotification("No OEM FM Radio app found. Direct chip & stream active.")
        }
        return launched
    }

    fun tuneFrequency(freq: Float) {
        fmHelper.tuneFrequency(freq)
    }

    fun stepFrequency(step: Float) {
        fmHelper.stepFrequency(step)
    }

    fun playPreset(preset: RadioStation) {
        fmHelper.playStation(preset)
        showNotification("Playing: ${preset.name}")
    }

    fun toggleRadioPlayback() {
        fmHelper.togglePlayPause()
    }

    fun toggleDirectChip(freq: Float) {
        val newPower = !fmHelper.isChipActive.value
        fmHelper.sendDirectChipHardwareCommand(freq, newPower)
        showNotification(if (newPower) "Direct hardware FM chip powered ON" else "Direct hardware FM chip powered OFF")
    }

    fun setRadioVolume(vol: Float) {
        fmHelper.setVolume(vol)
    }

    fun setSleepTimer(minutes: Int) {
        fmHelper.setSleepTimer(minutes)
        showNotification(if (minutes > 0) "Sleep timer set for $minutes min" else "Sleep timer disabled")
    }

    fun fetchOnlineStations(query: String? = null, genre: String? = null) {
        fmHelper.fetchOnlineStations(query, genre)
        showNotification("Fetching stations from Radiobee & CleverFerret...")
    }

    fun importCustomStation(name: String, url: String, genre: String) {
        val st = fmHelper.importCustomStation(name, url, genre)
        showNotification("Imported: ${st.name}")
    }

    fun bookmarkStation(station: RadioStation) {
        fmHelper.bookmarkStationToPresets(station)
        showNotification("Bookmarked ${station.name} to presets")
    }

    // Bluetooth Actions
    fun toggleBluetooth() {
        bluetoothHelper.toggleBluetooth()
    }

    fun scanBluetooth() {
        bluetoothHelper.startScan()
        showNotification("Bluetooth discovery started (12s auto-timeout)")
    }

    fun stopBluetoothScan() {
        bluetoothHelper.stopScan()
        showNotification("Bluetooth discovery stopped")
    }

    fun connectBluetooth(device: BluetoothDeviceInfo) {
        showNotification("Connecting to ${device.name}...")
        bluetoothHelper.connectOrPair(device)
    }

    fun disconnectBluetooth(device: BluetoothDeviceInfo) {
        showNotification("Disconnecting ${device.name}...")
        bluetoothHelper.disconnectDevice(device)
    }

    // Power Actions
    fun setPowerMode(mode: PowerMode) {
        batteryHelper.setPowerMode(mode)
        when (mode) {
            PowerMode.ULTRA_SAVER -> {
                wifiHelper.updateAutoSwitchConfig(wifiHelper.autoSwitchConfig.value.copy(scanIntervalSeconds = 90))
                bluetoothHelper.stopScan()
                showNotification("Ultra Saver: Background polling minimized")
            }
            PowerMode.BALANCED -> {
                wifiHelper.updateAutoSwitchConfig(wifiHelper.autoSwitchConfig.value.copy(scanIntervalSeconds = 45))
                showNotification("Balanced: Smart low-power active")
            }
            PowerMode.PERFORMANCE -> {
                wifiHelper.updateAutoSwitchConfig(wifiHelper.autoSwitchConfig.value.copy(scanIntervalSeconds = 15))
                showNotification("Performance: Frequent scan telemetry enabled")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wifiHelper.unregister()
        fmHelper.unregister()
        bluetoothHelper.unregister()
        batteryHelper.unregister()
    }
}
