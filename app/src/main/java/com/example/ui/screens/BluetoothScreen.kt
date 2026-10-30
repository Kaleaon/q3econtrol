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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothDeviceCategory
import com.example.model.BluetoothDeviceInfo
import com.example.ui.theme.LocalKtheme
import com.example.viewmodel.MainViewModel

@Composable
fun BluetoothScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val isEnabled by viewModel.bluetoothHelper.isBluetoothEnabled.collectAsState()
    val isScanning by viewModel.bluetoothHelper.isScanning.collectAsState()
    val countdown by viewModel.bluetoothHelper.scanCountdown.collectAsState()
    val connectedDevice by viewModel.bluetoothHelper.connectedDevice.collectAsState()
    val pairedDevices by viewModel.bluetoothHelper.pairedDevices.collectAsState()
    val discoveredDevices by viewModel.bluetoothHelper.discoveredDevices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ktheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Bluetooth Master Toggle Card
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
                        imageVector = if (isEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                        contentDescription = "Bluetooth Toggle Icon",
                        tint = if (isEnabled) ktheme.primary else ktheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEnabled) "Bluetooth Enabled" else "Bluetooth Disabled",
                            color = ktheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isEnabled) "Ready for audio peripheral connection" else "Radio powered off to save battery",
                            color = ktheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleBluetooth() },
                    modifier = Modifier.testTag("bluetooth_master_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ktheme.primary,
                        checkedTrackColor = ktheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        if (isEnabled) {
            // 2. Connected Device Hero Card (if active)
            connectedDevice?.let { connected ->
                item {
                    ConnectedAudioHeroCard(
                        device = connected,
                        onDisconnect = { viewModel.disconnectBluetooth(connected) }
                    )
                }
            }

            // 3. Scan & Radar Card
            item {
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
                        Column {
                            Text(
                                text = "DEVICE DISCOVERY",
                                color = ktheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isScanning) "Auto-terminating in ${countdown}s (battery saver)" else "12-second safe discovery sessions",
                                color = if (isScanning) ktheme.secondary else ktheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (isScanning) {
                            Button(
                                onClick = { viewModel.stopBluetoothScan() },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("stop_bluetooth_scan_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ktheme.danger, contentColor = ktheme.onBackground),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("STOP", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.scanBluetooth() },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("scan_bluetooth_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ktheme.primary, contentColor = ktheme.background),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SCAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (isScanning) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (countdown / 12f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = ktheme.secondary,
                            trackColor = ktheme.surfaceVariant
                        )
                    }
                }
            }

            // 4. Paired Devices List
            item {
                Text(
                    text = "PAIRED AUDIO DEVICES (${pairedDevices.size})",
                    color = ktheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Text(
                        text = "No paired devices yet. Put your headphones or speaker into pairing mode and tap SCAN.",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                items(pairedDevices) { device ->
                    BluetoothDeviceRow(
                        device = device,
                        isPairedSection = true,
                        onConnectClick = { viewModel.connectBluetooth(device) },
                        onDisconnectClick = { viewModel.disconnectBluetooth(device) }
                    )
                }
            }

            // 5. Discovered Nearby Devices
            if (discoveredDevices.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "NEARBY DISCOVERED DEVICES (${discoveredDevices.size})",
                        color = ktheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                items(discoveredDevices) { device ->
                    BluetoothDeviceRow(
                        device = device,
                        isPairedSection = false,
                        onConnectClick = { viewModel.connectBluetooth(device) },
                        onDisconnectClick = { viewModel.disconnectBluetooth(device) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ConnectedAudioHeroCard(
    device: BluetoothDeviceInfo,
    onDisconnect: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ktheme.surface)
            .border(1.dp, ktheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
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
                        .background(ktheme.secondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACTIVE AUDIO SINK",
                    color = ktheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .height(28.dp)
                    .testTag("disconnect_bluetooth_hero_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ktheme.danger),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("DISCONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = getCategoryIcon(device.deviceCategory),
                contentDescription = null,
                tint = ktheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = device.name,
                    color = ktheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${device.deviceCategory.label} • ${device.address}",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (device.batteryPercent in 0..100) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ktheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = ktheme.success, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Device Battery: ${device.batteryPercent}%",
                    color = ktheme.onSurface,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun BluetoothDeviceRow(
    device: BluetoothDeviceInfo,
    isPairedSection: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (device.isConnected) ktheme.secondary.copy(alpha = 0.08f) else ktheme.surface)
            .border(1.dp, if (device.isConnected) ktheme.secondary.copy(alpha = 0.4f) else ktheme.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getCategoryIcon(device.deviceCategory),
            contentDescription = null,
            tint = if (device.isConnected) ktheme.secondary else ktheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                color = ktheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${device.deviceCategory.label} • ${device.address}",
                color = ktheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (device.isConnected) {
            Button(
                onClick = onDisconnectClick,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("disconnect_dev_${device.address}"),
                colors = ButtonDefaults.buttonColors(containerColor = ktheme.surfaceVariant, contentColor = ktheme.danger),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("DISCONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        } else {
            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("connect_dev_${device.address}"),
                colors = ButtonDefaults.buttonColors(containerColor = ktheme.primary, contentColor = ktheme.background),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(if (isPairedSection) "CONNECT" else "PAIR & CONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

private fun getCategoryIcon(category: BluetoothDeviceCategory): ImageVector {
    return when (category) {
        BluetoothDeviceCategory.AUDIO_HEADPHONES -> Icons.Default.Headphones
        BluetoothDeviceCategory.AUDIO_SPEAKER -> Icons.Default.Speaker
        BluetoothDeviceCategory.CAR_AUDIO -> Icons.Default.DirectionsCar
        else -> Icons.Default.BluetoothConnected
    }
}
