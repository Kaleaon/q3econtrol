package com.example.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Method

/**
 * Direct FM Chip Driver, Radiobee & CleverFerret Online Station Hub,
 * and Audio Manager for Timkoo Q3E and Android devices.
 */
class FmChipManager(private val context: Context) {

    private val tag = "FmChipManager"
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val directoryService = StationDirectoryService()

    // State flows
    private val _frequency = MutableStateFlow(98.7f)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isChipActive = MutableStateFlow(false)
    val isChipActive: StateFlow<Boolean> = _isChipActive.asStateFlow()

    private val _isHeadsetPlugged = MutableStateFlow(false)
    val isHeadsetPlugged: StateFlow<Boolean> = _isHeadsetPlugged.asStateFlow()

    private val _hardwareChipStatus = MutableStateFlow("Probing chip...")
    val hardwareChipStatus: StateFlow<String> = _hardwareChipStatus.asStateFlow()

    private val _detectedOemPackage = MutableStateFlow<String?>(null)
    val detectedOemPackage: StateFlow<String?> = _detectedOemPackage.asStateFlow()

    private val _currentStationName = MutableStateFlow("98.7 FM - CleverFerret Synthwave")
    val currentStationName: StateFlow<String> = _currentStationName.asStateFlow()

    private val _currentStationSource = MutableStateFlow("Default Catalog")
    val currentStationSource: StateFlow<String> = _currentStationSource.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    // Presets and Online Directory
    private val _presets = MutableStateFlow<List<RadioStation>>(directoryService.defaultCatalog.take(7))
    val presets: StateFlow<List<RadioStation>> = _presets.asStateFlow()

    private val _onlineStations = MutableStateFlow<List<RadioStation>>(directoryService.defaultCatalog)
    val onlineStations: StateFlow<List<RadioStation>> = _onlineStations.asStateFlow()

    private val _isFetchingStations = MutableStateFlow(false)
    val isFetchingStations: StateFlow<Boolean> = _isFetchingStations.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var sleepTimerJob: Job? = null

    private val knownOemPackages = listOf(
        "com.android.fmradio",
        "com.mediatek.FMRadio",
        "com.fmradio",
        "com.android.fm",
        "com.quicinc.fmradio",
        "com.caf.fmradio",
        "com.android.fmradiomanager"
    )

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                val plugged = state == 1
                _isHeadsetPlugged.value = plugged
                Log.d(tag, "Headset plug state changed: plugged=$plugged")
            }
        }
    }

    init {
        detectHardwareChipAndOemApp()
        registerHeadsetReceiver()
        fetchOnlineStations(null, null)
    }

    private fun registerHeadsetReceiver() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context.registerReceiver(headsetReceiver, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(headsetReceiver)
        } catch (_: Exception) {}
        stopAudio()
    }

    fun detectHardwareChipAndOemApp() {
        scope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            var foundOem: String? = null

            for (pkg in knownOemPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    foundOem = pkg
                    break
                } catch (_: PackageManager.NameNotFoundException) {}
            }
            _detectedOemPackage.value = foundOem

            var chipFound = false
            var chipDetail = "Software Tuner Active"

            try {
                val serviceManagerClass = Class.forName("android.os.ServiceManager")
                val getServiceMethod: Method = serviceManagerClass.getMethod("getService", String::class.java)

                val fmServiceBinder = getServiceMethod.invoke(null, "fm_radio") as? IBinder
                    ?: getServiceMethod.invoke(null, "FmRadio") as? IBinder

                if (fmServiceBinder != null) {
                    chipFound = true
                    chipDetail = "MediaTek/AOSP FM Chip Service detected"
                }
            } catch (e: Exception) {
                Log.d(tag, "ServiceManager probe notice: ${e.message}")
            }

            if (!chipFound) {
                val radioNode = File("/dev/radio0")
                val fmNode = File("/dev/fm")
                if (radioNode.exists() || fmNode.exists()) {
                    chipFound = true
                    chipDetail = "Direct kernel device /dev/radio0 detected"
                }
            }

            if (foundOem != null && !chipFound) {
                chipDetail = "OEM FM Package installed ($foundOem)"
            }

            _hardwareChipStatus.value = chipDetail
        }
    }

    fun launchOemRadioApp(): Boolean {
        val pkg = _detectedOemPackage.value ?: return false
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: Intent().apply {
            setPackage(pkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch OEM FM radio: ${e.message}")
            false
        }
    }

    fun sendDirectChipHardwareCommand(freqMhz: Float, powerOn: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                val intent = Intent(if (powerOn) "com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERUP"
                else "com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERDOWN")
                intent.putExtra("frequency", (freqMhz * 10).toInt())
                context.sendBroadcast(intent)

                if (powerOn) {
                    val tuneIntent = Intent("com.android.fmradio.FMRadioService.ACTION_TOFMSERVICE_TUNE")
                    tuneIntent.putExtra("frequency", (freqMhz * 10).toInt())
                    context.sendBroadcast(tuneIntent)
                }
                _isChipActive.value = powerOn
            } catch (e: Exception) {
                Log.w(tag, "Broadcast to chip service: ${e.message}")
            }
        }
    }

    fun tuneFrequency(newFreq: Float) {
        val clamped = ((newFreq * 10).toInt() / 10f).coerceIn(87.5f, 108.0f)
        _frequency.value = clamped

        val matchingPreset = _onlineStations.value.find { Math.abs(it.frequencyMhz - clamped) < 0.15f }
            ?: _presets.value.find { Math.abs(it.frequencyMhz - clamped) < 0.15f }

        _currentStationName.value = matchingPreset?.name ?: String.format("%.1f FM - Radio Station", clamped)
        _currentStationSource.value = matchingPreset?.source ?: "Direct Frequency"

        if (_isChipActive.value) {
            sendDirectChipHardwareCommand(clamped, powerOn = true)
        }

        if (_isPlaying.value && matchingPreset != null) {
            playStation(matchingPreset)
        }
    }

    fun stepFrequency(step: Float) {
        tuneFrequency(_frequency.value + step)
    }

    fun playStation(station: RadioStation) {
        tuneFrequency(station.frequencyMhz)
        _currentStationName.value = station.name
        _currentStationSource.value = station.source

        scope.launch(Dispatchers.IO) {
            try {
                stopAudioInternal()

                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(station.streamUrl)
                    setVolume(_volume.value, _volume.value)
                    setOnPreparedListener {
                        it.start()
                        _isPlaying.value = true
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
                        _isPlaying.value = false
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = mp
            } catch (e: Exception) {
                Log.e(tag, "Error starting playback: ${e.message}")
                _isPlaying.value = false
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopAudio()
        } else {
            val matching = _onlineStations.value.find { Math.abs(it.frequencyMhz - _frequency.value) < 0.15f }
                ?: _presets.value.firstOrNull()
                ?: directoryService.defaultCatalog.first()
            playStation(matching)
        }
    }

    private fun stopAudioInternal() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (_: Exception) {}
    }

    fun stopAudio() {
        stopAudioInternal()
        _isPlaying.value = false
        if (_isChipActive.value) {
            sendDirectChipHardwareCommand(_frequency.value, powerOn = false)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (_: Exception) {}
    }

    fun setSleepTimer(minutes: Int) {
        _sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()

        if (minutes > 0) {
            sleepTimerJob = scope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60_000)
                    remaining -= 1
                    _sleepTimerMinutes.value = remaining
                }
                stopAudio()
            }
        }
    }

    /**
     * Grabs stations from Radiobee, CleverFerret, and open directory APIs
     */
    fun fetchOnlineStations(query: String?, genre: String?) {
        scope.launch {
            _isFetchingStations.value = true
            val stations = directoryService.fetchLiveStations(query, genre)
            _onlineStations.value = stations
            _isFetchingStations.value = false
        }
    }

    /**
     * Imports a custom stream URL provided by the user
     */
    fun importCustomStation(name: String, url: String, genre: String): RadioStation {
        val station = directoryService.parseCustomStation(name, url, genre)
        val current = _presets.value.toMutableList()
        current.add(0, station)
        _presets.value = current

        val onlineCurrent = _onlineStations.value.toMutableList()
        onlineCurrent.add(0, station)
        _onlineStations.value = onlineCurrent

        playStation(station)
        return station
    }

    /**
     * Adds an online station to quick presets
     */
    fun bookmarkStationToPresets(station: RadioStation) {
        val current = _presets.value.toMutableList()
        if (current.none { it.streamUrl == station.streamUrl }) {
            current.add(station.copy(isPreset = true))
            _presets.value = current
        }
    }
}
