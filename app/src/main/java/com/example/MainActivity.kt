package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeaderBar
import com.example.ui.screens.BluetoothScreen
import com.example.ui.screens.FmRadioScreen
import com.example.ui.screens.PowerScreen
import com.example.ui.screens.SettingsThemeScreen
import com.example.ui.screens.WifiScreen
import com.example.ui.theme.KthemePack
import com.example.ui.theme.LocalKtheme
import com.example.ui.theme.Q3eKthemeProvider
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val activeKtheme by viewModel.activeKtheme.collectAsState()

            Q3eKthemeProvider(activePack = activeKtheme) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val ktheme = LocalKtheme.current
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val activeKtheme by viewModel.activeKtheme.collectAsState()
    val batteryInfo by viewModel.batteryHelper.batteryInfo.collectAsState()
    val isRadioPlaying by viewModel.fmHelper.isPlaying.collectAsState()
    val radioFreq by viewModel.fmHelper.frequency.collectAsState()
    val statusNotification by viewModel.statusNotification.collectAsState()

    // Graceful permission requests for Wi-Fi scanning (Location) and Bluetooth on modern Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Handle results gracefully
        viewModel.wifiHelper.updateConnectionState()
        viewModel.bluetoothHelper.refreshPairedDevices()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HeaderBar(
                batteryInfo = batteryInfo,
                activeKtheme = activeKtheme,
                isRadioPlaying = isRadioPlaying,
                radioFreq = radioFreq,
                statusNotification = statusNotification,
                onThemeToggle = {
                    val nextPack = when (activeKtheme) {
                        KthemePack.TERMINAL_NEON -> KthemePack.SWEEP_LCARS
                        KthemePack.SWEEP_LCARS -> KthemePack.METRO
                        KthemePack.METRO -> KthemePack.LUMIYA_DARK
                        KthemePack.LUMIYA_DARK -> KthemePack.FRUTIGER_AERO
                        KthemePack.FRUTIGER_AERO -> KthemePack.TERMINAL_NEON
                    }
                    viewModel.setKtheme(nextPack)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .background(ktheme.surface)
                    .border(width = 1.dp, color = ktheme.border)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = ktheme.surface,
                contentColor = ktheme.onSurface
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = (activeTab == tab)
                    val icon = getTabIcon(tab)

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ktheme.primary,
                            selectedTextColor = ktheme.primary,
                            unselectedIconColor = ktheme.onSurfaceVariant,
                            unselectedTextColor = ktheme.onSurfaceVariant,
                            indicatorColor = ktheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                AppTab.WIFI -> WifiScreen(viewModel = viewModel)
                AppTab.FM_RADIO -> FmRadioScreen(viewModel = viewModel)
                AppTab.BLUETOOTH -> BluetoothScreen(viewModel = viewModel)
                AppTab.POWER -> PowerScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsThemeScreen(viewModel = viewModel)
            }
        }
    }
}

private fun getTabIcon(tab: AppTab): ImageVector {
    return when (tab) {
        AppTab.WIFI -> Icons.Default.Wifi
        AppTab.FM_RADIO -> Icons.Default.Radio
        AppTab.BLUETOOTH -> Icons.Default.Bluetooth
        AppTab.POWER -> Icons.Default.BatteryChargingFull
        AppTab.SETTINGS -> Icons.Default.Palette
    }
}
