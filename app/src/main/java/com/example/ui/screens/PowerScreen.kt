package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PowerMode
import com.example.ui.theme.LocalKtheme
import com.example.viewmodel.MainViewModel

@Composable
fun PowerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val batteryInfo by viewModel.batteryHelper.batteryInfo.collectAsState()
    val estHours = viewModel.batteryHelper.estimateRemainingHours()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ktheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Battery Telemetry Hero
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ktheme.surface)
                    .border(1.dp, ktheme.border, RoundedCornerShape(12.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BATTERY TELEMETRY & RUNTIME",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val levelColor = when {
                    batteryInfo.levelPercent <= 15 -> ktheme.danger
                    batteryInfo.levelPercent <= 30 -> ktheme.warning
                    else -> ktheme.success
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${batteryInfo.levelPercent}",
                        color = levelColor,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "%",
                        color = levelColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (batteryInfo.isCharging) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Charging",
                            tint = ktheme.secondary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(bottom = 6.dp)
                        )
                    }
                }

                Text(
                    text = "${batteryInfo.powerSource} • Status: ${batteryInfo.health}",
                    color = ktheme.onSurface,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { batteryInfo.levelPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = levelColor,
                    trackColor = ktheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row: Temp, Voltage, Estimated Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TEMP", color = ktheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format("%.1f°C", batteryInfo.temperatureCelsius),
                            color = ktheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "VOLTAGE", color = ktheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "${batteryInfo.voltageMv} mV",
                            color = ktheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "EST. RUNTIME", color = ktheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format("~%.1fh", estHours),
                            color = ktheme.secondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 2. Power Profiles Selector
        item {
            Text(
                text = "POWER CONSERVATION PROFILES",
                color = ktheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        PowerMode.values().forEach { mode ->
            val isSelected = batteryInfo.powerMode == mode
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ktheme.primary.copy(alpha = 0.12f) else ktheme.surface)
                        .border(1.dp, if (isSelected) ktheme.primary else ktheme.border, RoundedCornerShape(10.dp))
                        .clickable { viewModel.setPowerMode(mode) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) ktheme.primary else ktheme.onSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ktheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) ktheme.primary else ktheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = mode.description,
                            color = ktheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3. Timkoo Q3E Hardware Battery Tips
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TIMKOO Q3E BATTERY OPTIMIZATION TIPS",
                color = ktheme.secondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ktheme.surface)
                    .border(1.dp, ktheme.border, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BatteryTipRow(
                    icon = Icons.Default.Headphones,
                    title = "Hardware FM vs Streaming",
                    body = "Using the native hardware FM tuner consumes ~20mA vs ~140mA for Wi-Fi streaming. Wired FM radio provides up to 5x battery runtime!"
                )
                BatteryTipRow(
                    icon = Icons.Default.Wifi,
                    title = "Smart Wi-Fi Hysteresis",
                    body = "The app's auto-switcher stays dormant when signal is strong, cutting background CPU wakeups by >85%."
                )
                BatteryTipRow(
                    icon = Icons.Default.Lightbulb,
                    title = "Ktheme Terminal & Neon",
                    body = "Using the pure OLED black Terminal theme turns off inactive pixels on compatible displays, extending playback time."
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BatteryTipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    val ktheme = LocalKtheme.current

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ktheme.secondary,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = ktheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = body,
                color = ktheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
