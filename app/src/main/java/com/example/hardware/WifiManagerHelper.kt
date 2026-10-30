package com.example.hardware

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import com.example.model.AutoSwitchConfig
import com.example.model.CurrentWifiConnection
import com.example.model.WifiBandPreference
import com.example.model.WifiGlobalSecurityConfig
import com.example.model.WifiNetworkItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

/**
 * High-efficiency Wi-Fi manager with captive portal detection, global security filtering,
 * and intelligent battery-saving auto-network switching.
 * Tailored for Android 4.4 KitKat through modern Android.
 */
class WifiManagerHelper(private val context: Context) {

    private val tag = "WifiManagerHelper"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isWifiEnabled = MutableStateFlow(wifiManager.isWifiEnabled)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _currentConnection = MutableStateFlow(CurrentWifiConnection())
    val currentConnection: StateFlow<CurrentWifiConnection> = _currentConnection.asStateFlow()

    private val _rawScanResults = MutableStateFlow<List<WifiNetworkItem>>(emptyList())
    private val _filteredScanResults = MutableStateFlow<List<WifiNetworkItem>>(emptyList())
    val scanResults: StateFlow<List<WifiNetworkItem>> = _filteredScanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _autoSwitchConfig = MutableStateFlow(AutoSwitchConfig())
    val autoSwitchConfig: StateFlow<AutoSwitchConfig> = _autoSwitchConfig.asStateFlow()

    private val _globalSecurityConfig = MutableStateFlow(WifiGlobalSecurityConfig())
    val globalSecurityConfig: StateFlow<WifiGlobalSecurityConfig> = _globalSecurityConfig.asStateFlow()

    private val _autoSwitchStatusMessage = MutableStateFlow("Auto-Switch Idle (Signal Healthy)")
    val autoSwitchStatusMessage: StateFlow<String> = _autoSwitchStatusMessage.asStateFlow()

    // Locally tracked saved networks for auto-switching
    private val savedNetworkSsidSet = mutableSetOf<String>()

    private var autoSwitchLoopJob: Job? = null

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                    val enabled = (state == WifiManager.WIFI_STATE_ENABLED)
                    _isWifiEnabled.value = enabled
                    if (enabled) {
                        updateConnectionState()
                        startScan()
                    } else {
                        _currentConnection.value = CurrentWifiConnection()
                        _rawScanResults.value = emptyList()
                        _filteredScanResults.value = emptyList()
                    }
                }
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    _isScanning.value = false
                    processScanResults()
                }
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                WifiManager.RSSI_CHANGED_ACTION -> {
                    updateConnectionState()
                }
            }
        }
    }

    init {
        registerReceiver()
        updateConnectionState()
        refreshConfiguredNetworks()
        startAutoSwitchSupervisor()
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
        }
        context.registerReceiver(wifiReceiver, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(wifiReceiver)
        } catch (_: Exception) {}
        autoSwitchLoopJob?.cancel()
    }

    @Suppress("DEPRECATION")
    private fun refreshConfiguredNetworks() {
        try {
            val configured = wifiManager.configuredNetworks ?: emptyList()
            for (conf in configured) {
                val cleanSsid = conf.SSID?.replace("\"", "") ?: continue
                savedNetworkSsidSet.add(cleanSsid)
            }
        } catch (e: Exception) {
            Log.d(tag, "Configured networks: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun updateConnectionState() {
        if (!wifiManager.isWifiEnabled) {
            _currentConnection.value = CurrentWifiConnection()
            return
        }

        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
        val netInfo: NetworkInfo? = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val isConnected = netInfo?.isConnected == true && wifiInfo != null && wifiInfo.networkId != -1

        if (isConnected && wifiInfo != null) {
            val rawSsid = wifiInfo.ssid ?: "Unknown"
            val cleanSsid = if (rawSsid.startsWith("\"") && rawSsid.endsWith("\"")) {
                rawSsid.substring(1, rawSsid.length - 1)
            } else {
                rawSsid
            }

            if (cleanSsid != "<unknown ssid>" && cleanSsid.isNotBlank()) {
                savedNetworkSsidSet.add(cleanSsid)
            }

            val ipInt = wifiInfo.ipAddress
            val ipString = Formatter.formatIpAddress(ipInt)
            val rssi = wifiInfo.rssi
            val linkSpeed = wifiInfo.linkSpeed
            val freq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try { wifiInfo.frequency } catch (_: Exception) { 2412 }
            } else 2412

            _currentConnection.value = CurrentWifiConnection(
                isConnected = true,
                ssid = cleanSsid,
                bssid = wifiInfo.bssid ?: "--",
                ipAddress = ipString,
                linkSpeedMbps = linkSpeed,
                rssi = rssi,
                frequencyMhz = freq,
                hasInternetAccess = true
            )

            // Probe captive portal asynchronously
            checkCaptivePortalStatus()
        } else {
            _currentConnection.value = CurrentWifiConnection(
                isConnected = false,
                ssid = "Disconnected",
                hasInternetAccess = false
            )
        }
    }

    private fun checkCaptivePortalStatus() {
        scope.launch(Dispatchers.IO) {
            try {
                // Compatible with Android 4.4 KitKat generate_204 endpoint
                val url = URL("http://connectivitycheck.gstatic.com/generate_204")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 3500
                    readTimeout = 3500
                    useCaches = false
                }
                val responseCode = connection.responseCode
                val redirectLocation = connection.getHeaderField("Location")
                connection.disconnect()

                val isCaptive = responseCode in 300..399 || (responseCode == 200 && connection.contentLength > 0)
                val targetPortal = redirectLocation ?: if (isCaptive) "http://192.168.1.1" else null

                _currentConnection.value = _currentConnection.value.copy(
                    isCaptivePortalDetected = isCaptive,
                    captivePortalUrl = targetPortal
                )
            } catch (e: Exception) {
                Log.d(tag, "Captive portal check error: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!wifiManager.isWifiEnabled) return
        _isScanning.value = true
        try {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
        } catch (e: Exception) {
            Log.e(tag, "Start scan failed: ${e.message}")
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun processScanResults() {
        try {
            val results: List<ScanResult> = wifiManager.scanResults ?: emptyList()
            val currentSsid = _currentConnection.value.ssid

            val items = results.mapNotNull { scan ->
                val ssid = scan.SSID
                if (ssid.isNullOrBlank()) return@mapNotNull null

                val security = parseSecurity(scan.capabilities)
                val freq = scan.frequency
                val isConnected = (ssid == currentSsid && _currentConnection.value.isConnected)
                val isSaved = savedNetworkSsidSet.contains(ssid)
                val isCaptive = (security == "Open" && scan.capabilities.contains("ESS", ignoreCase = true))

                WifiNetworkItem(
                    ssid = ssid,
                    bssid = scan.BSSID ?: "--",
                    rssi = scan.level,
                    frequencyMhz = freq,
                    security = security,
                    isConnected = isConnected,
                    isSaved = isSaved,
                    hasCaptivePortal = isCaptive,
                    captivePortalUrl = if (isCaptive) "http://192.168.1.1" else null
                )
            }.distinctBy { it.ssid }

            _rawScanResults.value = items
            applyGlobalSecurityFilter(items)

            // Run auto-switch evaluation
            evaluateAutoSwitch(_filteredScanResults.value)
        } catch (e: Exception) {
            Log.e(tag, "Process scan results error: ${e.message}")
        }
    }

    private fun applyGlobalSecurityFilter(items: List<WifiNetworkItem>) {
        val sec = _globalSecurityConfig.value
        val filtered = items.filter { item ->
            // Filter 1: Require Secure Only
            if (sec.requireSecureWifiOnly && !item.isSecure) return@filter false
            // Filter 2: Disallow WEP if strict WPA2/WPA3 required
            if (sec.requireWpa2OrWpa3 && item.security == "WEP") return@filter false
            // Filter 3: Frequency Band
            when (sec.bandPreference) {
                WifiBandPreference.ONLY_2_4_GHZ -> item.frequencyMhz in 2400..2500
                WifiBandPreference.ONLY_5_GHZ -> item.frequencyMhz > 2500
                WifiBandPreference.AUTO_DUAL -> true
            }
        }.sortedWith(compareByDescending<WifiNetworkItem> { it.isConnected }
            .thenByDescending { it.isSaved }
            .thenByDescending { it.rssi })

        _filteredScanResults.value = filtered
    }

    private fun parseSecurity(capabilities: String): String {
        return when {
            capabilities.contains("WPA3", ignoreCase = true) || capabilities.contains("SAE", ignoreCase = true) -> "WPA3"
            capabilities.contains("WPA2", ignoreCase = true) -> "WPA2"
            capabilities.contains("WPA", ignoreCase = true) -> "WPA"
            capabilities.contains("WEP", ignoreCase = true) -> "WEP"
            else -> "Open"
        }
    }

    private fun evaluateAutoSwitch(networks: List<WifiNetworkItem>) {
        val config = _autoSwitchConfig.value
        if (!config.isEnabled) {
            _autoSwitchStatusMessage.value = "Auto-Switch Disabled"
            return
        }

        val current = _currentConnection.value
        if (!current.isConnected) {
            val strongestSaved = networks.firstOrNull { it.isSaved }
            if (strongestSaved != null) {
                _autoSwitchStatusMessage.value = "Connecting to saved AP: ${strongestSaved.ssid}"
                connectToNetwork(strongestSaved.ssid, null)
            } else {
                _autoSwitchStatusMessage.value = "Disconnected (No saved networks in range)"
            }
            return
        }

        val currentRssi = current.rssi
        if (currentRssi >= config.minRssiThreshold) {
            _autoSwitchStatusMessage.value = "Current signal healthy (${currentRssi} dBm)"
            return
        }

        val candidate = networks.firstOrNull {
            it.isSaved && it.ssid != current.ssid && (it.rssi - currentRssi) >= config.hysteresisDb
        }

        if (candidate != null) {
            _autoSwitchStatusMessage.value = "Switching to stronger AP: ${candidate.ssid} (${candidate.rssi} dBm vs ${currentRssi} dBm)"
            connectToNetwork(candidate.ssid, null)
        } else {
            _autoSwitchStatusMessage.value = "Signal weak (${currentRssi} dBm), searching candidates..."
        }
    }

    private fun startAutoSwitchSupervisor() {
        autoSwitchLoopJob?.cancel()
        autoSwitchLoopJob = scope.launch(Dispatchers.IO) {
            while (true) {
                delay(_autoSwitchConfig.value.scanIntervalSeconds * 1000L)
                val current = _currentConnection.value
                val needsScan = !current.isConnected || current.rssi < _autoSwitchConfig.value.minRssiThreshold
                if (needsScan && _isWifiEnabled.value) {
                    startScan()
                }
            }
        }
    }

    /**
     * Connects to a network using native Android 4.4+ compatible WifiConfiguration
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun connectToNetwork(ssid: String, password: String?): Boolean {
        savedNetworkSsidSet.add(ssid)

        return try {
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"$ssid\""
                if (password.isNullOrBlank()) {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                } else {
                    preSharedKey = "\"$password\""
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                }
            }

            val netId = wifiManager.addNetwork(wifiConfig)
            if (netId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
                scope.launch {
                    delay(2000)
                    updateConnectionState()
                }
                true
            } else {
                val existing = wifiManager.configuredNetworks?.find { it.SSID == "\"$ssid\"" }
                if (existing != null) {
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(existing.networkId, true)
                    wifiManager.reconnect()
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Connect network failed: ${e.message}")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectCurrent() {
        try {
            wifiManager.disconnect()
            updateConnectionState()
        } catch (e: Exception) {
            Log.e(tag, "Disconnect error: ${e.message}")
        }
    }

    /**
     * Toggles Wi-Fi power On/Off with full KitKat and modern Android compatibility.
     */
    fun toggleWifi(turnOn: Boolean? = null): Boolean {
        return try {
            val targetState = turnOn ?: !wifiManager.isWifiEnabled
            @Suppress("DEPRECATION")
            val success = wifiManager.setWifiEnabled(targetState)
            _isWifiEnabled.value = targetState

            if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // If on Android 10+ where setWifiEnabled is restricted by Google, open system panel
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(panelIntent)
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Toggle wifi error: ${e.message}")
            false
        }
    }

    fun updateAutoSwitchConfig(config: AutoSwitchConfig) {
        _autoSwitchConfig.value = config
    }

    fun updateGlobalSecurityConfig(config: WifiGlobalSecurityConfig) {
        _globalSecurityConfig.value = config
        applyGlobalSecurityFilter(_rawScanResults.value)
    }
}
