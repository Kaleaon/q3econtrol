package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hardware.StationDirectoryService
import com.example.model.AutoSwitchConfig
import com.example.model.WifiBandPreference
import com.example.model.WifiGlobalSecurityConfig
import com.example.model.WifiNetworkItem
import com.example.ui.theme.TerminalNeonPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Q3E Companion", appName)
    }

    @Test
    fun testWifiSignalCalculation() {
        val strongItem = WifiNetworkItem(
            ssid = "Home_Wifi",
            bssid = "00:11:22:33:44:55",
            rssi = -55
        )
        assertEquals(4, strongItem.signalBars)
        assertTrue(strongItem.signalPercent >= 80)
        assertTrue(strongItem.isSecure)

        val openItem = WifiNetworkItem(
            ssid = "Public_Open",
            bssid = "AA:BB:CC:DD:EE:FF",
            rssi = -85,
            security = "Open"
        )
        assertEquals(1, openItem.signalBars)
        assertFalse(openItem.isSecure)
    }

    @Test
    fun testAutoSwitchConfigDefaults() {
        val config = AutoSwitchConfig()
        assertTrue(config.isEnabled)
        assertEquals(-75, config.minRssiThreshold)
        assertEquals(15, config.hysteresisDb)
    }

    @Test
    fun testStationDirectoryService() {
        val service = StationDirectoryService()
        assertTrue(service.defaultCatalog.isNotEmpty())

        val radiobeeStation = service.defaultCatalog.find { it.source.contains("RadioBee") }
        assertTrue(radiobeeStation != null)

        val cleverFerretStation = service.defaultCatalog.find { it.source.contains("CleverFerret") }
        assertTrue(cleverFerretStation != null)

        // Test custom parsing
        val custom = service.parseCustomStation("My Cool Stream", "http://stream.example.com/audio.mp3", "Synth")
        assertEquals("My Cool Stream", custom.name)
        assertEquals("http://stream.example.com/audio.mp3", custom.streamUrl)
    }

    @Test
    fun testGlobalSecurityConfig() {
        val config = WifiGlobalSecurityConfig(
            requireSecureWifiOnly = true,
            bandPreference = WifiBandPreference.ONLY_2_4_GHZ
        )
        assertTrue(config.requireSecureWifiOnly)
        assertEquals(WifiBandPreference.ONLY_2_4_GHZ, config.bandPreference)
    }

    @Test
    fun testKthemeTerminalTokens() {
        assertTrue(TerminalNeonPalette.isDark)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF05080F), TerminalNeonPalette.background)
    }

    @Test
    fun testSweepLcarsTokens() {
        assertTrue(com.example.ui.theme.SweepLcarsPalette.isDark)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), com.example.ui.theme.SweepLcarsPalette.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFF9900), com.example.ui.theme.SweepLcarsPalette.primary)
        assertTrue(com.example.ui.theme.SweepLcarsStyle.isLcars)
    }

    @Test
    fun testMetroTokens() {
        assertTrue(com.example.ui.theme.MetroPalette.isDark)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), com.example.ui.theme.MetroPalette.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF00A4EF), com.example.ui.theme.MetroPalette.primary)
        assertTrue(com.example.ui.theme.MetroStyle.isMetro)
    }
}
