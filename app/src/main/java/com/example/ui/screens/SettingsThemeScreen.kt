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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WifiBandPreference
import com.example.ui.theme.FrutigerAeroPalette
import com.example.ui.theme.FrutigerAeroStyle
import com.example.ui.theme.KthemePack
import com.example.ui.theme.LocalKtheme
import com.example.ui.theme.LumiyaDarkPalette
import com.example.ui.theme.LumiyaDarkStyle
import com.example.ui.theme.MetroPalette
import com.example.ui.theme.MetroStyle
import com.example.ui.theme.SweepLcarsPalette
import com.example.ui.theme.SweepLcarsStyle
import com.example.ui.theme.TerminalNeonPalette
import com.example.ui.theme.TerminalNeonStyle
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsThemeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val activePack by viewModel.activeKtheme.collectAsState()
    val securityConfig by viewModel.wifiHelper.globalSecurityConfig.collectAsState()
    val isWifiOn by viewModel.wifiHelper.isWifiEnabled.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ktheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Ktheme Selector Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = ktheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "KTHEME DESIGN SYSTEM",
                        color = ktheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Modular palette packs from github.com/kaleaon/ktheme",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. Palette Cards
        KthemePack.values().forEach { pack ->
            val isSelected = (activePack == pack)
            val (samplePalette, sampleStyle) = when (pack) {
                KthemePack.TERMINAL_NEON -> Pair(TerminalNeonPalette, TerminalNeonStyle)
                KthemePack.SWEEP_LCARS -> Pair(SweepLcarsPalette, SweepLcarsStyle)
                KthemePack.METRO -> Pair(MetroPalette, MetroStyle)
                KthemePack.LUMIYA_DARK -> Pair(LumiyaDarkPalette, LumiyaDarkStyle)
                KthemePack.FRUTIGER_AERO -> Pair(FrutigerAeroPalette, FrutigerAeroStyle)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(sampleStyle.cardShape)
                        .background(samplePalette.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) samplePalette.primary else samplePalette.border,
                            shape = sampleStyle.cardShape
                        )
                        .clickable { viewModel.setKtheme(pack) }
                        .padding(14.dp)
                        .testTag("theme_pack_${pack.name}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pack.displayName,
                                color = samplePalette.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(sampleStyle.badgeShape)
                                        .background(samplePalette.secondary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = samplePalette.background,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Color Swatch Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SwatchCircle(samplePalette.background, "Canvas")
                            SwatchCircle(samplePalette.surface, "Surface")
                            SwatchCircle(samplePalette.primary, "Primary")
                            SwatchCircle(samplePalette.secondary, "Accent")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pack.description,
                        color = samplePalette.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 3. Global Wi-Fi Security & Hardware Radio Settings
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ktheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "GLOBAL WI-FI & SECURITY SETTINGS",
                        color = ktheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manage global radio power and secure connection policies",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
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
                // Master Wi-Fi Power Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Wi-Fi Power Toggle", color = ktheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(if (isWifiOn) "Wi-Fi radio is broadcasting" else "Wi-Fi powered off", color = ktheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = isWifiOn,
                        onCheckedChange = { viewModel.toggleWifi(it) },
                        modifier = Modifier.testTag("settings_wifi_power_toggle"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ktheme.primary)
                    )
                }

                // Global Secure Only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Require Secure Encrypted Wi-Fi", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Filters out open & unencrypted public access points", color = ktheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = securityConfig.requireSecureWifiOnly,
                        onCheckedChange = { viewModel.updateGlobalSecurityConfig(securityConfig.copy(requireSecureWifiOnly = it)) },
                        modifier = Modifier.testTag("settings_secure_only_toggle"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ktheme.secondary)
                    )
                }

                // Strict WPA2 / WPA3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enforce Strict WPA2 / WPA3", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Blocks legacy compromised WEP networks", color = ktheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = securityConfig.requireWpa2OrWpa3,
                        onCheckedChange = { viewModel.updateGlobalSecurityConfig(securityConfig.copy(requireWpa2OrWpa3 = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ktheme.secondary)
                    )
                }

                // Frequency Band selection
                Text("Frequency Band Limit:", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WifiBandPreference.values().forEach { band ->
                        val isSelected = securityConfig.bandPreference == band
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ktheme.primary else ktheme.surfaceVariant)
                                .clickable { viewModel.updateGlobalSecurityConfig(securityConfig.copy(bandPreference = band)) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (band) {
                                    WifiBandPreference.AUTO_DUAL -> "Auto Dual"
                                    WifiBandPreference.ONLY_2_4_GHZ -> "2.4 GHz (Q3E)"
                                    WifiBandPreference.ONLY_5_GHZ -> "5 GHz"
                                },
                                color = if (isSelected) ktheme.background else ktheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 4. Device Profile Info
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ktheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DEVICE PROFILE & SPECIFICATIONS",
                    color = ktheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ktheme.surface)
                    .border(1.dp, ktheme.border, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecRow("Target Hardware", "Timkoo Q3E / Android DAP Media Players")
                SpecRow("OS Compatibility", "Android 4.4 KitKat (API 19) through Android 16")
                SpecRow("FM Radio Subsystem", "Direct IFMRadioService & /dev/radio0 Driver + Online Backup")
                SpecRow("Online Station Catalog", "Radiobee & CleverFerret live directory streaming")
                SpecRow("Wi-Fi Auto-Switching", "Smart Signal Hysteresis with Captive Portal Detector")
                SpecRow("Bluetooth Audio", "A2DP Sink Router with 12s Auto-Stop Discovery")
                SpecRow("Power Footprint", "Event-Driven Sleeping Background Service")
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SwatchCircle(color: Color, desc: String) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun SpecRow(label: String, value: String) {
    val ktheme = LocalKtheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = ktheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = ktheme.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
