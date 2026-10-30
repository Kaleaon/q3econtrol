package com.example.model

/**
 * Data models for Wi-Fi, FM Radio, Online Stations, Bluetooth, and Battery management
 * designed with Android 4.4+ compatibility in mind.
 */

data class WifiNetworkItem(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequencyMhz: Int = 2412,
    val security: String = "WPA2",
    val isConnected: Boolean = false,
    val isSaved: Boolean = false,
    val hasCaptivePortal: Boolean = false,
    val captivePortalUrl: String? = null
) {
    val signalPercent: Int
        get() = ((rssi + 100).coerceIn(0, 50) * 2).coerceIn(0, 100)

    val signalBars: Int
        get() = when {
            rssi >= -60 -> 4
            rssi >= -70 -> 3
            rssi >= -80 -> 2
            rssi >= -90 -> 1
            else -> 0
        }

    val isSecure: Boolean
        get() = security != "Open" && security != "None"
}

data class CurrentWifiConnection(
    val isConnected: Boolean = false,
    val ssid: String = "Disconnected",
    val bssid: String = "--:--:--:--:--:--",
    val ipAddress: String = "0.0.0.0",
    val linkSpeedMbps: Int = 0,
    val rssi: Int = -100,
    val frequencyMhz: Int = 0,
    val hasInternetAccess: Boolean = false,
    val isCaptivePortalDetected: Boolean = false,
    val captivePortalUrl: String? = null
)

data class AutoSwitchConfig(
    val isEnabled: Boolean = true,
    val minRssiThreshold: Int = -75, // Switch if current drops below this
    val hysteresisDb: Int = 15,      // Switch candidate must be at least this much stronger
    val scanIntervalSeconds: Int = 45 // Conserves battery; only active when signal is degraded
)

data class WifiGlobalSecurityConfig(
    val requireSecureWifiOnly: Boolean = false, // Ignore open networks
    val blockInsecureCaptivePortals: Boolean = false, // Require HTTPS on portals
    val requireWpa2OrWpa3: Boolean = false, // Disallow WEP
    val bandPreference: WifiBandPreference = WifiBandPreference.AUTO_DUAL,
    val wifiSleepPolicyAlwaysOn: Boolean = true
)

enum class WifiBandPreference(val label: String) {
    AUTO_DUAL("Auto (2.4 GHz & 5 GHz)"),
    ONLY_2_4_GHZ("2.4 GHz Only (Optimal for Timkoo Q3E)"),
    ONLY_5_GHZ("5 GHz High Speed Only")
}

data class RadioStation(
    val name: String,
    val frequencyMhz: Float,
    val streamUrl: String,
    val genre: String,
    val isPreset: Boolean = true,
    val source: String = "Default Hardware Preset",
    val country: String = "Global",
    val bitrateKbps: Int = 128
)

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isBonded: Boolean = false,
    val isConnected: Boolean = false,
    val deviceCategory: BluetoothDeviceCategory = BluetoothDeviceCategory.AUDIO_HEADPHONES,
    val batteryPercent: Int = -1 // -1 means unknown or not reported
)

enum class BluetoothDeviceCategory(val label: String) {
    AUDIO_HEADPHONES("Headphones / Earbuds"),
    AUDIO_SPEAKER("Portable Speaker"),
    CAR_AUDIO("Car Audio Kit"),
    AUDIO_OTHER("Audio Peripheral"),
    GENERIC("Bluetooth Device")
}

data class BatteryInfo(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val powerSource: String = "Battery",
    val temperatureCelsius: Float = 25.0f,
    val voltageMv: Int = 4000,
    val health: String = "Good",
    val powerMode: PowerMode = PowerMode.BALANCED
)

enum class PowerMode(val label: String, val description: String) {
    ULTRA_SAVER(
        "Ultra Saver",
        "Wi-Fi scans only on disconnect. Disables Bluetooth discovery. Maximum Timkoo Q3E battery runtime."
    ),
    BALANCED(
        "Balanced (Default)",
        "Smart event-driven Wi-Fi scans. Auto-stops Bluetooth discovery after 12s."
    ),
    PERFORMANCE(
        "Performance",
        "Frequent Wi-Fi scan updates & active radio telemetry."
    )
}
