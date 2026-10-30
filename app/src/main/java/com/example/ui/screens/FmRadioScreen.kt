package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.example.model.RadioStation
import com.example.ui.components.LcarsFmRadioTuner
import com.example.ui.theme.LocalKtheme
import com.example.ui.theme.LocalKthemeStyle
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FmRadioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ktheme = LocalKtheme.current
    val kthemeStyle = LocalKthemeStyle.current

    var preferLcarsLayout by remember { mutableStateOf(kthemeStyle.isLcars) }
    LaunchedEffect(kthemeStyle.isLcars) {
        preferLcarsLayout = kthemeStyle.isLcars
    }

    val frequency by viewModel.fmHelper.frequency.collectAsState()
    val isPlaying by viewModel.fmHelper.isPlaying.collectAsState()
    val isChipActive by viewModel.fmHelper.isChipActive.collectAsState()
    val isHeadsetPlugged by viewModel.fmHelper.isHeadsetPlugged.collectAsState()
    val chipStatus by viewModel.fmHelper.hardwareChipStatus.collectAsState()
    val oemPackage by viewModel.fmHelper.detectedOemPackage.collectAsState()
    val stationName by viewModel.fmHelper.currentStationName.collectAsState()
    val stationSource by viewModel.fmHelper.currentStationSource.collectAsState()
    val volume by viewModel.fmHelper.volume.collectAsState()
    val sleepTimer by viewModel.fmHelper.sleepTimerMinutes.collectAsState()
    val presets by viewModel.fmHelper.presets.collectAsState()
    val onlineStations by viewModel.fmHelper.onlineStations.collectAsState()
    val isFetching by viewModel.fmHelper.isFetchingStations.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenreFilter by remember { mutableStateOf("All") }
    var showImportDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customGenre by remember { mutableStateOf("Custom") }

    val genreCategories = listOf("All", "CleverFerret", "RadioBee", "Lo-Fi", "Rock", "News", "Jazz", "Electronic")

    val filteredOnlineStations = onlineStations.filter { station ->
        val matchesQuery = searchQuery.isBlank() ||
                station.name.contains(searchQuery, ignoreCase = true) ||
                station.genre.contains(searchQuery, ignoreCase = true) ||
                station.source.contains(searchQuery, ignoreCase = true)

        val matchesGenre = when (selectedGenreFilter) {
            "All" -> true
            "CleverFerret" -> station.source.contains("CleverFerret", ignoreCase = true)
            "RadioBee" -> station.source.contains("RadioBee", ignoreCase = true)
            else -> station.genre.contains(selectedGenreFilter, ignoreCase = true)
        }

        matchesQuery && matchesGenre
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ktheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // View Mode Switcher Header Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (preferLcarsLayout) "LCARS DIAGNOSTIC CONSOLE" else "FM HARDWARE TUNER",
                    color = ktheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .clip(kthemeStyle.badgeShape)
                        .background(ktheme.surfaceVariant)
                        .border(1.dp, ktheme.border, kthemeStyle.badgeShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(kthemeStyle.badgeShape)
                            .background(if (preferLcarsLayout) ktheme.primary else Color.Transparent)
                            .clickable { preferLcarsLayout = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("view_mode_lcars")
                    ) {
                        Text(
                            text = "LCARS",
                            color = if (preferLcarsLayout) Color.Black else ktheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(kthemeStyle.badgeShape)
                            .background(if (!preferLcarsLayout) ktheme.primary else Color.Transparent)
                            .clickable { preferLcarsLayout = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("view_mode_dial")
                    ) {
                        Text(
                            text = "DIAL",
                            color = if (!preferLcarsLayout) Color.Black else ktheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (preferLcarsLayout) {
            item {
                LcarsFmRadioTuner(
                    viewModel = viewModel,
                    frequency = frequency,
                    isPlaying = isPlaying,
                    isChipActive = isChipActive,
                    isHeadsetPlugged = isHeadsetPlugged,
                    chipStatus = chipStatus,
                    oemPackage = oemPackage,
                    stationName = stationName,
                    stationSource = stationSource,
                    volume = volume,
                    sleepTimer = sleepTimer,
                    presets = presets
                )
            }
        } else {
            // 1. Hardware FM Chip Status & Direct Driver Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Chip Status",
                            tint = ktheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FM CHIP HARDWARE DRIVER",
                                color = ktheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = chipStatus,
                                color = ktheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Direct Chip Power Toggle
                    Button(
                        onClick = { viewModel.toggleDirectChip(frequency) },
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("toggle_chip_power_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isChipActive) ktheme.secondary else ktheme.surfaceVariant,
                            contentColor = if (isChipActive) ktheme.background else ktheme.onSurface
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isChipActive) "CHIP ON" else "CHIP OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (oemPackage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.launchOemRadio() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("launch_oem_fm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ktheme.primary.copy(alpha = 0.15f),
                            contentColor = ktheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LAUNCH TIMKOO OEM FM RADIO APP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isHeadsetPlugged) ktheme.secondary.copy(alpha = 0.12f) else ktheme.warning.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isHeadsetPlugged) Icons.Default.Headphones else Icons.Default.Warning,
                        contentDescription = "Antenna",
                        tint = if (isHeadsetPlugged) ktheme.secondary else ktheme.warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHeadsetPlugged) "Headphones Connected (FM Antenna Ready)" else "Headphones unplugged. Plug in 3.5mm cable as antenna, or stream online below.",
                        color = if (isHeadsetPlugged) ktheme.secondary else ktheme.warning,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. Large Tactical Frequency Tuner Hero
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ktheme.surface)
                    .border(1.dp, ktheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TUNER & STREAM RECEIVER",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stationSource,
                        color = ktheme.secondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.1f", frequency),
                        color = ktheme.primary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MHz",
                        color = ktheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = stationName,
                    color = ktheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Fine Tune Controls & Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.stepFrequency(-0.1f) },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("fm_step_down_button"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("- 0.1", color = ktheme.primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }

                    Slider(
                        value = frequency,
                        onValueChange = { viewModel.tuneFrequency(it) },
                        valueRange = 87.5f..108.0f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("fm_frequency_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = ktheme.primary,
                            activeTrackColor = ktheme.primary,
                            inactiveTrackColor = ktheme.surfaceVariant
                        )
                    )

                    OutlinedButton(
                        onClick = { viewModel.stepFrequency(0.1f) },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("fm_step_up_button"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("+ 0.1", color = ktheme.primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.toggleRadioPlayback() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .testTag("radio_play_pause_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) ktheme.secondary else ktheme.primary,
                        contentColor = ktheme.background
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Radio Playback",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPlaying) "BROADCAST PLAYING" else "RADIO MUTED / STANDBY",
                    color = if (isPlaying) ktheme.secondary else ktheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 3. Volume & Sleep Timer Control Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = ktheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Slider(
                        value = volume,
                        onValueChange = { viewModel.setRadioVolume(it) },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("radio_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = ktheme.primary,
                            activeTrackColor = ktheme.primary
                        )
                    )
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = ktheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        color = ktheme.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = ktheme.secondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (sleepTimer > 0) "SLEEP TIMER: ${sleepTimer}m" else "SLEEP TIMER: OFF",
                            color = ktheme.onSurface,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 15, 30, 60).forEach { mins ->
                            val isSelected = (sleepTimer == mins)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) ktheme.secondary else ktheme.surfaceVariant)
                                    .clickable { viewModel.setSleepTimer(mins) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (mins == 0) "OFF" else "${mins}m",
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

        // 4. Quick Presets Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FAVORITE PRESETS (${presets.size})",
                    color = ktheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("import_custom_station_button"),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IMPORT URL", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = Math.abs(frequency - preset.frequencyMhz) < 0.15f
                    StationPresetChip(
                        station = preset,
                        isSelected = isSelected,
                        onClick = { viewModel.playPreset(preset) }
                    )
                }
            }
        }

        // 5. Radiobee & CleverFerret Online Station Catalog
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "RADIOBEE & CLEVERFERRET STATIONS",
                        color = ktheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Live feeds from Radiobee, CleverFerret, and open directories",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { viewModel.fetchOnlineStations(searchQuery, if (selectedGenreFilter == "All") null else selectedGenreFilter) },
                    enabled = !isFetching,
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("fetch_online_stations_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ktheme.secondary, contentColor = ktheme.background),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ktheme.background, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFetching) "SYNCING..." else "SYNC", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by station name, tag or artist", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ktheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("station_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ktheme.secondary,
                    unfocusedBorderColor = ktheme.border,
                    focusedTextColor = ktheme.onSurface,
                    unfocusedTextColor = ktheme.onSurface
                )
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                genreCategories.forEach { cat ->
                    val isCatSelected = (selectedGenreFilter == cat)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCatSelected) ktheme.secondary else ktheme.surface)
                            .border(1.dp, if (isCatSelected) ktheme.secondary else ktheme.border, RoundedCornerShape(6.dp))
                            .clickable { selectedGenreFilter = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isCatSelected) ktheme.background else ktheme.onSurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Online Stations List
        if (filteredOnlineStations.isEmpty()) {
            item {
                Text(
                    text = "No stations match '$searchQuery'. Tap SYNC to query directories.",
                    color = ktheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            items(filteredOnlineStations) { station ->
                OnlineStationRow(
                    station = station,
                    isCurrentPlaying = (station.name == stationName && isPlaying),
                    onPlay = { viewModel.playPreset(station) },
                    onBookmark = { viewModel.bookmarkStation(station) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Custom Station Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ktheme.surface,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    text = "IMPORT CUSTOM RADIO STREAM",
                    color = ktheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add custom Radiobee, CleverFerret, Icecast, or direct HTTP/M3U stream URLs.",
                        color = ktheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Station Name (e.g. My Favorite Stream)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_station_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("Stream URL (http://...:8000/live)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_station_url_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customGenre,
                        onValueChange = { customGenre = it },
                        label = { Text("Genre (e.g. Cyberpunk, Talk, Jazz)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrl.isNotBlank()) {
                            viewModel.importCustomStation(customName, customUrl, customGenre)
                            showImportDialog = false
                            customName = ""
                            customUrl = ""
                        }
                    },
                    modifier = Modifier.testTag("dialog_import_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ktheme.primary, contentColor = ktheme.background),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("SAVE & PLAY", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showImportDialog = false },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CANCEL", color = ktheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
private fun StationPresetChip(
    station: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ktheme.primary.copy(alpha = 0.2f) else ktheme.surface)
            .border(1.dp, if (isSelected) ktheme.primary else ktheme.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Radio,
            contentDescription = null,
            tint = if (isSelected) ktheme.primary else ktheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = "${station.frequencyMhz} MHz",
                color = if (isSelected) ktheme.primary else ktheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = station.genre,
                color = ktheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun OnlineStationRow(
    station: RadioStation,
    isCurrentPlaying: Boolean,
    onPlay: () -> Unit,
    onBookmark: () -> Unit
) {
    val ktheme = LocalKtheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentPlaying) ktheme.secondary.copy(alpha = 0.12f) else ktheme.surface)
            .border(1.dp, if (isCurrentPlaying) ktheme.secondary else ktheme.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCurrentPlaying) Icons.Default.MusicNote else Icons.Default.Radio,
            contentDescription = null,
            tint = if (isCurrentPlaying) ktheme.secondary else ktheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                color = if (isCurrentPlaying) ktheme.secondary else ktheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${station.genre} • ${station.source} • ${station.country}",
                color = ktheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp).testTag("bookmark_station_${station.name}")) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = ktheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Button(
            onClick = onPlay,
            modifier = Modifier
                .height(28.dp)
                .testTag("play_station_${station.name}"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCurrentPlaying) ktheme.secondary else ktheme.surfaceVariant,
                contentColor = if (isCurrentPlaying) ktheme.background else ktheme.primary
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(if (isCurrentPlaying) "PLAYING" else "PLAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
