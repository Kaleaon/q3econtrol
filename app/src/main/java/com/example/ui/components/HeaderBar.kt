package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryInfo
import com.example.ui.theme.KthemePack
import com.example.ui.theme.LocalKtheme
import com.example.ui.theme.LocalKthemeStyle

@Composable
fun HeaderBar(
    batteryInfo: BatteryInfo,
    activeKtheme: KthemePack,
    isRadioPlaying: Boolean,
    radioFreq: Float,
    statusNotification: String?,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val kthemeStyle = LocalKthemeStyle.current

    if (kthemeStyle.isLcars) {
        // True Starfleet LCARS Sweep Console Header
        LcarsSweepHeader(
            batteryInfo = batteryInfo,
            isRadioPlaying = isRadioPlaying,
            radioFreq = radioFreq,
            statusNotification = statusNotification,
            onThemeToggle = onThemeToggle,
            modifier = modifier
        )
    } else if (kthemeStyle.isMetro) {
        // True Microsoft Metro Live Header (Flat 0.dp sharp tiles, stark typography)
        MetroHeader(
            batteryInfo = batteryInfo,
            isRadioPlaying = isRadioPlaying,
            radioFreq = radioFreq,
            statusNotification = statusNotification,
            onThemeToggle = onThemeToggle,
            modifier = modifier
        )
    } else {
        // Standard Cyber / Terminal / Aero Header
        StandardHeader(
            batteryInfo = batteryInfo,
            activeKtheme = activeKtheme,
            isRadioPlaying = isRadioPlaying,
            radioFreq = radioFreq,
            statusNotification = statusNotification,
            onThemeToggle = onThemeToggle,
            modifier = modifier
        )
    }
}

/**
 * Authentic LCARS Sweep Architecture:
 * - Left rounded elbow sweep bracket (Okuda Amber #FF9900)
 * - Horizontal split frame rail with gap breaks
 * - Segmented serial block [47201 // Q3E]
 * - Pill capsule telemetry badges
 */
@Composable
private fun LcarsSweepHeader(
    batteryInfo: BatteryInfo,
    isRadioPlaying: Boolean,
    radioFreq: Float,
    statusNotification: String?,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(top = 6.dp, bottom = 6.dp, start = 8.dp, end = 10.dp)
    ) {
        // Upper LCARS Sweep Arch Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left LCARS Elbow Cap
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(ktheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "47201",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Sweep Horizontal Rail Segment 1 (Lilac)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(14.dp)
                    .background(ktheme.tertiary)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Title block
            Text(
                text = "LCARS // Q3E COMPANION",
                color = ktheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Sweep Horizontal Rail Segment 2 (Peach)
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(14.dp)
                    .background(ktheme.secondary)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Palette Toggle Pill
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ktheme.primary)
                    .testTag("theme_quick_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Toggle Ktheme",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lower LCARS Telemetry Readout Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Serial Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(ktheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SYS-402 // OK",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isRadioPlaying) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(ktheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FREQ ${String.format("%.1f", radioFreq)} MHz",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Battery Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (batteryInfo.levelPercent <= 20) ktheme.danger else ktheme.primary)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PWR ${batteryInfo.levelPercent}%",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Notification Banner
        AnimatedVisibility(visible = !statusNotification.isNullOrBlank(), enter = fadeIn(), exit = fadeOut()) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ktheme.secondary)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = ">> ${statusNotification?.uppercase() ?: ""}",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Authentic Microsoft Metro / Zune Architecture:
 * - 0.dp sharp geometry
 * - Oversized stark typography
 * - Solid flat accent color blocks (Metro Cyan #00A4EF)
 */
@Composable
private fun MetroHeader(
    batteryInfo: BatteryInfo,
    isRadioPlaying: Boolean,
    radioFreq: Float,
    statusNotification: String?,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(width = 1.dp, color = ktheme.border)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Metro Solid Accent Block
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(ktheme.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Q3E COMPANION",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "timkoo media hub",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRadioPlaying) {
                    Box(
                        modifier = Modifier
                            .background(ktheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", radioFreq)} FM",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Sharp Flat Battery Tile
                Box(
                    modifier = Modifier
                        .background(if (batteryInfo.levelPercent <= 20) ktheme.danger else ktheme.surfaceVariant)
                        .border(1.dp, ktheme.primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${batteryInfo.levelPercent}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .background(ktheme.surfaceVariant)
                        .border(1.dp, ktheme.border)
                        .testTag("theme_quick_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Toggle Ktheme",
                        tint = ktheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = !statusNotification.isNullOrBlank(), enter = fadeIn(), exit = fadeOut()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ktheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusNotification ?: "",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StandardHeader(
    batteryInfo: BatteryInfo,
    activeKtheme: KthemePack,
    isRadioPlaying: Boolean,
    radioFreq: Float,
    statusNotification: String?,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ktheme.surface)
            .border(width = 1.dp, color = ktheme.border)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ktheme.secondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Q3E COMPANION",
                        color = ktheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = "Timkoo Q3E Hardware Hub",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRadioPlaying) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ktheme.secondary.copy(alpha = 0.15f))
                            .border(1.dp, ktheme.secondary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio Playing",
                            tint = ktheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", radioFreq),
                            color = ktheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                val batteryColor = when {
                    batteryInfo.levelPercent <= 15 -> ktheme.danger
                    batteryInfo.levelPercent <= 30 -> ktheme.warning
                    else -> ktheme.success
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ktheme.surfaceVariant)
                        .border(1.dp, ktheme.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when {
                        batteryInfo.isCharging -> Icons.Default.BatteryChargingFull
                        batteryInfo.levelPercent <= 15 -> Icons.Default.BatteryAlert
                        else -> Icons.Default.BatteryFull
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Battery Level",
                        tint = batteryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${batteryInfo.levelPercent}%",
                        color = ktheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("theme_quick_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Toggle Ktheme",
                        tint = ktheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !statusNotification.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ktheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, ktheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusNotification ?: "",
                    color = ktheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
