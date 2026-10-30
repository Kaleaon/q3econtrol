package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.RadioStation
import com.example.ui.theme.LocalKtheme
import com.example.viewmodel.MainViewModel

/**
 * Specialized LCARS-Style FM Radio Tuner Component.
 * Features:
 * 1. LCARS Sweep Frame & Elbow Arch
 * 2. Vertical Button Array (Starfleet Stack) with asymmetrical endcaps
 * 3. Diagnostic Readout Strips for Frequency Spectrum and Signal Strength (RSSI / dBm)
 * 4. Sub-carrier audio telemetry & pilot lock indicators
 */
@Composable
fun LcarsFmRadioTuner(
    viewModel: MainViewModel,
    frequency: Float,
    isPlaying: Boolean,
    isChipActive: Boolean,
    isHeadsetPlugged: Boolean,
    chipStatus: String,
    oemPackage: String?,
    stationName: String,
    stationSource: String,
    volume: Float,
    sleepTimer: Int,
    presets: List<RadioStation>,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current

    // LCARS Palette tokens
    val lcarsAmber = Color(0xFFFF9900)
    val lcarsPeach = Color(0xFFFF7700)
    val lcarsLilac = Color(0xFFCC99CC)
    val lcarsIceBlue = Color(0xFF99CCFF)
    val lcarsRedAlert = Color(0xFFDD2233)
    val lcarsDarkSurface = Color(0xFF0D0A10)
    val lcarsFrameBorder = Color(0xFF3B1E4A)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(width = 1.5.dp, color = lcarsFrameBorder, shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 4.dp, bottomEnd = 4.dp))
            .padding(10.dp)
    ) {
        // 1. LCARS Sweep Header Arch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Sweep Elbow Block
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(topStart = 13.dp, bottomStart = 13.dp))
                    .background(lcarsAmber),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RAD-472",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Rail Segment 1 (Lilac)
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(13.dp)
                    .background(lcarsLilac)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Main Title Readout
            Text(
                text = "FM TUNER // LCARS SUB-CARRIER",
                color = lcarsAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Rail Segment 2 (Peach)
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(lcarsPeach)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Main Two-Column Layout: Left Vertical Button Stack + Right Diagnostic Readout Strips
        Row(modifier = Modifier.fillMaxWidth()) {

            // LEFT COLUMN: Vertical LCARS Button Array
            Column(
                modifier = Modifier.width(115.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Button 1: Power Chip
                LcarsStackButton(
                    code = "01",
                    label = if (isChipActive) "CHIP ON" else "CHIP OFF",
                    color = if (isChipActive) lcarsIceBlue else lcarsAmber,
                    testTag = "lcars_btn_chip",
                    onClick = { viewModel.toggleDirectChip(frequency) }
                )

                // Button 2: Play/Standby
                LcarsStackButton(
                    code = "02",
                    label = if (isPlaying) "AUDIO MUT" else "AUDIO PLY",
                    color = if (isPlaying) lcarsPeach else lcarsLilac,
                    testTag = "lcars_btn_play",
                    onClick = { viewModel.toggleRadioPlayback() }
                )

                // Button 3: Step Up
                LcarsStackButton(
                    code = "03",
                    label = "SEEK +0.1",
                    color = lcarsAmber,
                    testTag = "lcars_btn_seek_up",
                    onClick = { viewModel.stepFrequency(0.1f) }
                )

                // Button 4: Step Down
                LcarsStackButton(
                    code = "04",
                    label = "SEEK -0.1",
                    color = lcarsLilac,
                    testTag = "lcars_btn_seek_down",
                    onClick = { viewModel.stepFrequency(-0.1f) }
                )

                // Button 5: Scan Diag
                LcarsStackButton(
                    code = "05",
                    label = "SCAN DIAG",
                    color = lcarsPeach,
                    testTag = "lcars_btn_scan",
                    onClick = { viewModel.fetchOnlineStations() }
                )

                // Button 6: Antenna Status
                LcarsStackButton(
                    code = "06",
                    label = if (isHeadsetPlugged) "ANT: LOCK" else "ANT: DISC",
                    color = if (isHeadsetPlugged) lcarsIceBlue else lcarsRedAlert,
                    testTag = "lcars_btn_antenna",
                    onClick = { /* Status display */ }
                )

                // Button 7: OEM FM App (if present)
                if (oemPackage != null) {
                    LcarsStackButton(
                        code = "07",
                        label = "OEM LAUNCH",
                        color = lcarsAmber,
                        testTag = "lcars_btn_oem",
                        onClick = { viewModel.launchOemRadio() }
                    )
                }

                // Button 8: Timer
                LcarsStackButton(
                    code = "08",
                    label = if (sleepTimer > 0) "TMR: ${sleepTimer}M" else "TMR: OFF",
                    color = lcarsLilac,
                    testTag = "lcars_btn_timer",
                    onClick = {
                        val next = when (sleepTimer) {
                            0 -> 15
                            15 -> 30
                            30 -> 60
                            else -> 0
                        }
                        viewModel.setSleepTimer(next)
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // RIGHT COLUMN: Diagnostic Readout Strips & Frequency Display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(lcarsDarkSurface)
                    .border(1.dp, lcarsFrameBorder)
                    .padding(10.dp)
            ) {
                // Top Telemetry Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FREQ SPECTRAL READOUT",
                        color = lcarsLilac,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isPlaying) "CARRIER LOCKED" else "CARRIER STANDBY",
                        color = if (isPlaying) lcarsIceBlue else lcarsPeach,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Big LCARS Frequency Number Display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%05.2f", frequency),
                        color = lcarsAmber,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MHz",
                        color = lcarsPeach,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Text(
                    text = "STATION // ${stationName.uppercase()}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    text = "FEED: ${stationSource.uppercase()} | ${chipStatus.uppercase()}",
                    color = lcarsLilac,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                // DIAGNOSTIC STRIP 1: Frequency Spectrum Segmentation Strip (87.5 - 108.0 MHz)
                Text(
                    text = "BAND DIAGNOSTIC STRIP (87.5 - 108.0 MHz)",
                    color = lcarsPeach,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(3.dp))
                FrequencySpectrumDiagnosticStrip(frequency = frequency)

                Spacer(modifier = Modifier.height(10.dp))

                // DIAGNOSTIC STRIP 2: Signal Strength & RSSI Diagnostic Strip
                val signalDbm = if (isPlaying) -62 else -95
                val signalRatio = if (isPlaying) 0.82f else 0.20f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SIGNAL LEVEL: $signalDbm dBm",
                        color = lcarsAmber,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PILOT: 19 kHz STEREO",
                        color = if (isPlaying) lcarsIceBlue else Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                SignalStrengthDiagnosticStrip(ratio = signalRatio)

                Spacer(modifier = Modifier.height(10.dp))

                // DIAGNOSTIC STRIP 3: Audio Volume & Pilot Carrier Ladder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAIN: ${(volume * 100).toInt()}%",
                        color = lcarsLilac,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = volume,
                        onValueChange = { viewModel.setRadioVolume(it) },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .testTag("lcars_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = lcarsPeach,
                            activeTrackColor = lcarsAmber,
                            inactiveTrackColor = lcarsFrameBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Lower LCARS Sub-Channel Presets Horizontal Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            presets.take(5).forEachIndexed { idx, preset ->
                val isSelected = Math.abs(frequency - preset.frequencyMhz) < 0.15f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (isSelected) lcarsAmber else lcarsFrameBorder)
                        .clickable { viewModel.playPreset(preset) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${preset.frequencyMhz}",
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Standard LCARS Vertical Stack Button with asymmetric left curved endcap
 */
@Composable
private fun LcarsStackButton(
    code: String,
    label: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 3.dp, bottomEnd = 3.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = code,
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.Black,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

/**
 * Segmented Frequency Spectrum Diagnostic Strip
 * Models 20 discrete band segments with the active tuned frequency highlighted
 */
@Composable
private fun FrequencySpectrumDiagnosticStrip(frequency: Float) {
    val lcarsAmber = Color(0xFFFF9900)
    val lcarsFrameBorder = Color(0xFF3B1E4A)
    val totalSegments = 20
    val activeIndex = (((frequency - 87.5f) / (108.0f - 87.5f)) * totalSegments).toInt().coerceIn(0, totalSegments - 1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until totalSegments) {
            val isCurrent = (i == activeIndex)
            val isNear = Math.abs(i - activeIndex) <= 1
            val segmentColor = when {
                isCurrent -> lcarsAmber
                isNear -> Color(0xFFFF7700)
                else -> lcarsFrameBorder
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isCurrent) 12.dp else 8.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(segmentColor)
            )
        }
    }
}

/**
 * Multi-Tier Signal Strength & RSSI Diagnostic Strip
 */
@Composable
private fun SignalStrengthDiagnosticStrip(ratio: Float) {
    val lcarsAmber = Color(0xFFFF9900)
    val lcarsPeach = Color(0xFFFF7700)
    val lcarsLilac = Color(0xFFCC99CC)
    val lcarsIceBlue = Color(0xFF99CCFF)
    val lcarsFrameBorder = Color(0xFF221128)

    val segments = 16
    val litCount = (ratio * segments).toInt().coerceIn(1, segments)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until segments) {
            val isLit = (i < litCount)
            val barColor = when {
                !isLit -> lcarsFrameBorder
                i < 4 -> lcarsAmber
                i < 8 -> lcarsPeach
                i < 12 -> lcarsLilac
                else -> lcarsIceBlue
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(barColor)
            )
        }
    }
}
