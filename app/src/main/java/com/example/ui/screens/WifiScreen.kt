package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoSwitchConfig
import com.example.model.CurrentWifiConnection
import com.example.model.WifiBandPreference
import com.example.model.WifiGlobalSecurityConfig
import com.example.model.WifiNetworkItem
import com.example.ui.theme.LocalKtheme
import com.example.viewmodel.MainViewModel

@Composable
fun WifiScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val isWifiEnabled by viewModel.wifiHelper.isWifiEnabled.collectAsState()
    val currentConn by viewModel.wifiHelper.currentConnection.collectAsState()
    val scanResults by viewModel.wifiHelper.scanResults.collectAsState()
    val isScanning by viewModel.wifiHelper.isScanning.collectAsState()
    val autoSwitchConfig by viewModel.wifiHelper.autoSwitchConfig.collectAsState()
    val autoSwitchStatus by viewModel.wifiHelper.autoSwitchStatusMessage.collectAsState()
    val securityConfig by viewModel.wifiHelper.globalSecurityConfig.collectAsState()

    var showConnectDialogFor by remember { mutableStateOf<WifiNetworkItem?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showAutoSwitchDetails by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ktheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Wi-Fi Power Master Toggle Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ktheme.surface)
                    .border(1.dp, ktheme.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isWifiEnabled) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Wi-Fi Toggle Icon",
                        tint = if (isWifiEnabled) ktheme.primary else ktheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isWifiEnabled) "Wi-Fi Power: ON" else "Wi-Fi Power: OFF",
                            color = ktheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isWifiEnabled) "Compatible with Android 4.4+ and modern networks" else "Radio dormant (saves ~80mA battery on Q3E)",
                            color = ktheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isWifiEnabled,
                        onCheckedChange = { viewModel.toggleWifi(it) },
                        modifier = Modifier.testTag("wifi_master_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ktheme.primary,
                            checkedTrackColor = ktheme.primary.copy(alpha = 0.3f),
                            uncheckedThumbColor = ktheme.onSurfaceVariant,
                            uncheckedTrackColor = ktheme.surfaceVariant
                        )
                    )
                }
            }
        }

        if (isWifiEnabled) {
            // 2. Current Connection Hero Card
            item {
                CurrentConnectionHeroCard(
                    connection = currentConn,
                    onDisconnect = { viewModel.disconnectWifi() },
                    onOpenCaptivePortal = { viewModel.launchCaptivePortal(currentConn.captivePortalUrl) }
                )
            }

            // 3. Global Security & Wi-Fi Settings Card
            item {
                GlobalSecurityCard(
                    config = securityConfig,
                    isExpanded = showSecuritySettings,
                    onToggleExpand = { showSecuritySettings = !showSecuritySettings },
                    onConfigChange = { viewModel.updateGlobalSecurityConfig(it) }
                )
            }

            // 4. Smart Auto-Switch Supervisor Card
            item {
                AutoSwitchCard(
                    config = autoSwitchConfig,
                    status = autoSwitchStatus,
                    isExpanded = showAutoSwitchDetails,
                    onToggleExpand = { showAutoSwitchDetails = !showAutoSwitchDetails },
                    onToggleEnabled = { enabled ->
                        viewModel.setAutoSwitchConfig(autoSwitchConfig.copy(isEnabled = enabled))
                    },
                    onThresholdChange = { threshold ->
                        viewModel.setAutoSwitchConfig(autoSwitchConfig.copy(minRssiThreshold = threshold.toInt()))
                    },
                    onHysteresisChange = { hyst ->
                        viewModel.setAutoSwitchConfig(autoSwitchConfig.copy(hysteresisDb = hyst.toInt()))
                    }
                )
            }

            // 5. Available Networks Header & Scan Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DETECTED NETWORKS (${scanResults.size})",
                        color = ktheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { viewModel.scanWifi() },
                        enabled = !isScanning,
                        modifier = Modifier
                            .testTag("scan_wifi_button")
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ktheme.surfaceVariant,
                            contentColor = ktheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ktheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCANNING...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCAN", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Scan Results List
            if (scanResults.isEmpty() && !isScanning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ktheme.surface)
                            .border(1.dp, ktheme.border, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "No Networks",
                                tint = ktheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No networks match your current security filter",
                                color = ktheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adjust Global Security or tap SCAN",
                                color = ktheme.primary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                items(scanResults) { network ->
                    NetworkItemRow(
                        network = network,
                        onConnectClick = {
                            if (network.security == "Open" || network.isSaved) {
                                viewModel.connectWifi(network.ssid, null)
                            } else {
                                showConnectDialogFor = network
                                passwordInput = ""
                            }
                        },
                        onOpenPortalClick = {
                            viewModel.launchCaptivePortal(network.captivePortalUrl)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Connect with Password Dialog
    showConnectDialogFor?.let { targetNet ->
        AlertDialog(
            onDismissRequest = { showConnectDialogFor = null },
            containerColor = ktheme.surface,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    text = "CONNECT TO ${targetNet.ssid.uppercase()}",
                    color = ktheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    Text(
                        text = "Security: ${targetNet.security} | Signal: ${targetNet.rssi} dBm",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Network Password", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wifi_password_input"),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = ktheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ktheme.primary,
                            unfocusedBorderColor = ktheme.border,
                            focusedTextColor = ktheme.onSurface,
                            unfocusedTextColor = ktheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectWifi(targetNet.ssid, passwordInput)
                        showConnectDialogFor = null
                    },
                    modifier = Modifier.testTag("dialog_connect_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ktheme.primary, contentColor = ktheme.background),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CONNECT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConnectDialogFor = null },
                    modifier = Modifier.testTag("dialog_cancel_button"),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CANCEL", color = ktheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun GlobalSecurityCard(
    config: WifiGlobalSecurityConfig,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onConfigChange: (WifiGlobalSecurityConfig) -> Unit
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ktheme.surface)
            .border(1.dp, ktheme.border, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                        text = "GLOBAL SECURITY & BANDS",
                        color = ktheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Filters open APs, enforces encryption, and restricts frequency bands",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Expand Security Settings",
                    tint = ktheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle 1: Require Secure Wi-Fi Only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Require Secure Wi-Fi Only", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Hide unencrypted / open public networks", color = ktheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = config.requireSecureWifiOnly,
                        onCheckedChange = { onConfigChange(config.copy(requireSecureWifiOnly = it)) },
                        modifier = Modifier.testTag("require_secure_toggle"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ktheme.secondary)
                    )
                }

                // Toggle 2: WPA2 / WPA3 Strict
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enforce WPA2/WPA3 (Block WEP)", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Disallow obsolete and vulnerable WEP encryption", color = ktheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = config.requireWpa2OrWpa3,
                        onCheckedChange = { onConfigChange(config.copy(requireWpa2OrWpa3 = it)) },
                        modifier = Modifier.testTag("require_wpa2_toggle"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ktheme.secondary)
                    )
                }

                // Band preference
                Text("Frequency Band Preference:", color = ktheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WifiBandPreference.values().forEach { band ->
                        val isSelected = config.bandPreference == band
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ktheme.primary else ktheme.surfaceVariant)
                                .clickable { onConfigChange(config.copy(bandPreference = band)) }
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
    }
}

@Composable
private fun CurrentConnectionHeroCard(
    connection: CurrentWifiConnection,
    onDisconnect: () -> Unit,
    onOpenCaptivePortal: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ktheme.surface)
            .border(1.dp, if (connection.isConnected) ktheme.primary.copy(alpha = 0.5f) else ktheme.border, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (connection.isConnected) ktheme.success else ktheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (connection.isConnected) "ACTIVE CONNECTION" else "NO CONNECTION",
                    color = if (connection.isConnected) ktheme.success else ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            if (connection.isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("disconnect_wifi_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ktheme.danger),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("DISCONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = connection.ssid,
            color = ktheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        if (connection.isConnected) {
            Spacer(modifier = Modifier.height(10.dp))

            val progress = ((connection.rssi + 100).coerceIn(0, 50) / 50f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SIGNAL: ${connection.rssi} dBm",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${connection.linkSpeedMbps} Mbps",
                    color = ktheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (progress > 0.5f) ktheme.secondary else ktheme.warning,
                trackColor = ktheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IP: ${connection.ipAddress}",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "BSSID: ${connection.bssid}",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (connection.isCaptivePortalDetected) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ktheme.warning.copy(alpha = 0.15f))
                        .border(1.dp, ktheme.warning, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Captive Portal Alert",
                        tint = ktheme.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CAPTIVE LOGIN PORTAL DETECTED",
                            color = ktheme.warning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Authentication required before Internet access",
                            color = ktheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Button(
                        onClick = onOpenCaptivePortal,
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("open_captive_portal_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ktheme.warning, contentColor = ktheme.background),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOGIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoSwitchCard(
    config: AutoSwitchConfig,
    status: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit,
    onHysteresisChange: (Float) -> Unit
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ktheme.surface)
            .border(1.dp, ktheme.border, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SMART AUTO-SWITCH",
                        color = ktheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onToggleExpand, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune Auto-Switch",
                            tint = ktheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = status,
                    color = ktheme.secondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Switch(
                checked = config.isEnabled,
                onCheckedChange = onToggleEnabled,
                modifier = Modifier.testTag("auto_switch_toggle"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ktheme.secondary,
                    checkedTrackColor = ktheme.secondary.copy(alpha = 0.3f)
                )
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "Low-power policy: Wi-Fi scans are halted while current connection is strong. When RSSI drops below threshold, the player auto-switches to stronger saved APs.",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Switch Trigger Threshold",
                        color = ktheme.onSurface,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${config.minRssiThreshold} dBm",
                        color = ktheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = config.minRssiThreshold.toFloat(),
                    onValueChange = onThresholdChange,
                    valueRange = -90f..-60f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = ktheme.primary,
                        activeTrackColor = ktheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Signal Difference (Hysteresis)",
                        color = ktheme.onSurface,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "+${config.hysteresisDb} dBm",
                        color = ktheme.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = config.hysteresisDb.toFloat(),
                    onValueChange = onHysteresisChange,
                    valueRange = 5f..25f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = ktheme.secondary,
                        activeTrackColor = ktheme.secondary
                    )
                )
            }
        }
    }
}

@Composable
private fun NetworkItemRow(
    network: WifiNetworkItem,
    onConnectClick: () -> Unit,
    onOpenPortalClick: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (network.isConnected) ktheme.primary.copy(alpha = 0.08f) else ktheme.surface)
            .border(1.dp, if (network.isConnected) ktheme.primary.copy(alpha = 0.4f) else ktheme.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onConnectClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (network.security == "Open") Icons.Default.LockOpen else Icons.Default.Lock,
            contentDescription = "Security",
            tint = if (network.security == "Open") ktheme.warning else ktheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = network.ssid,
                    color = ktheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (network.isSaved) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVED",
                        color = ktheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "${network.security} | ${network.rssi} dBm (${network.signalPercent}%) | ${network.frequencyMhz} MHz",
                color = ktheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (network.hasCaptivePortal) {
            IconButton(
                onClick = onOpenPortalClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "Login Portal",
                    tint = ktheme.warning,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (network.isConnected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = ktheme.success,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("connect_network_${network.ssid}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ktheme.surfaceVariant,
                    contentColor = ktheme.primary
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(if (network.isSaved) "RECONNECT" else "CONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
