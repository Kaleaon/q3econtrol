package com.example.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.model.BatteryInfo
import com.example.model.PowerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Battery Telemetry & Power Optimization Monitor.
 * Provides real-time metrics and low-power modes for portable media players.
 */
class BatteryManagerHelper(private val context: Context) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)

                val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                val source = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                    else -> "Battery Discharging"
                }

                val health = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Healthy"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    else -> "Normal"
                }

                _batteryInfo.value = _batteryInfo.value.copy(
                    levelPercent = pct,
                    isCharging = isCharging,
                    powerSource = source,
                    temperatureCelsius = temp,
                    voltageMv = voltage,
                    health = health
                )
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }

    fun setPowerMode(mode: PowerMode) {
        _batteryInfo.value = _batteryInfo.value.copy(powerMode = mode)
    }

    /**
     * Estimated runtime remaining in hours for Timkoo Q3E (~1400 mAh battery)
     */
    fun estimateRemainingHours(): Float {
        val pct = _batteryInfo.value.levelPercent
        return when (_batteryInfo.value.powerMode) {
            PowerMode.ULTRA_SAVER -> (pct / 100f) * 18.0f // up to 18 hrs audio playback with Wi-Fi idle
            PowerMode.BALANCED -> (pct / 100f) * 12.0f
            PowerMode.PERFORMANCE -> (pct / 100f) * 7.5f
        }
    }
}
