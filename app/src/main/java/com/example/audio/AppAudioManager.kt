package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized Audio Manager for Dots and Boxes.
 *
 * HOW TO ADD OR REPLACE AUDIO FILES:
 * 1. Place your audio files (e.g. .mp3, .ogg, or .wav) in `res/raw/`.
 * 2. Name them according to the keys below:
 *    - `bgm` -> Background music loop
 *    - `pen_stroke` -> Pen line drawing sound
 *    - `box_completed` -> Box capture chime
 *    - `button_click` -> UI tap sound
 *    - `game_win` -> Victory fanfare
 *    - `game_draw` -> Tie game chime
 *
 * If no raw file is present, AppAudioManager seamlessly falls back to high-quality
 * procedural acoustic sound synthesis, ensuring the game is 100% functional offline immediately.
 */
class AppAudioManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = AudioPreferences(appContext)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _settings = MutableStateFlow(preferences.loadSettings())
    val settings: StateFlow<AudioSettings> = _settings.asStateFlow()

    // SoundPool for custom raw SFX files if provided
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSfxMap = ConcurrentHashMap<SfxType, Int>()

    // Procedural AudioTrack cache for fallback SFX
    private val proceduralTracks = ConcurrentHashMap<SfxType, AudioTrack>()

    // Background Music player
    private var customMediaPlayer: MediaPlayer? = null
    private var proceduralBgmTrack: AudioTrack? = null
    private var proceduralBgmJob: Job? = null
    private var isMusicPlaying = false

    init {
        initializeAudio()
    }

    private fun initializeAudio() {
        // 1. Check if user provided custom audio files in res/raw
        val sfxRawNames = mapOf(
            SfxType.PEN_STROKE to "pen_stroke",
            SfxType.BOX_COMPLETED to "box_completed",
            SfxType.BUTTON_CLICK to "button_click",
            SfxType.GAME_WIN to "game_win",
            SfxType.GAME_DRAW to "game_draw"
        )

        for ((type, rawName) in sfxRawNames) {
            val resId = appContext.resources.getIdentifier(rawName, "raw", appContext.packageName)
            if (resId != 0) {
                val soundId = soundPool.load(appContext, resId, 1)
                loadedSfxMap[type] = soundId
                Log.d(TAG, "Loaded custom raw SFX for $type with resId $resId")
            }
        }

        // 2. Pre-generate procedural fallback sound buffers in background
        scope.launch {
            prepareProceduralTracks()
        }
    }

    private fun prepareProceduralTracks() {
        for (type in SfxType.values()) {
            if (loadedSfxMap.containsKey(type)) continue

            val pcmData = when (type) {
                SfxType.PEN_STROKE -> ProceduralAudioGenerator.generatePenStroke()
                SfxType.BOX_COMPLETED -> ProceduralAudioGenerator.generateBoxCapture()
                SfxType.BUTTON_CLICK -> ProceduralAudioGenerator.generateButtonClick()
                SfxType.GAME_WIN -> ProceduralAudioGenerator.generateVictoryFanfare()
                SfxType.GAME_DRAW -> ProceduralAudioGenerator.generateDrawChime()
            }

            val track = createStaticTrack(pcmData)
            if (track != null) {
                proceduralTracks[type] = track
            }
        }
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack? {
        return try {
            val sampleRate = ProceduralAudioGenerator.SAMPLE_RATE
            val bufferSizeInBytes = pcmData.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeInBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track
        } catch (e: Exception) {
            Log.e(TAG, "Error building procedural track", e)
            null
        }
    }

    fun playSfx(type: SfxType) {
        val current = _settings.value
        if (!current.sfxEnabled || current.sfxVolume <= 0f) return

        // 1. Play custom loaded SoundPool sound if available
        val customSoundId = loadedSfxMap[type]
        if (customSoundId != null && customSoundId != 0) {
            soundPool.play(customSoundId, current.sfxVolume, current.sfxVolume, 1, 0, 1.0f)
            return
        }

        // 2. Otherwise play procedural audio track
        scope.launch {
            try {
                var track = proceduralTracks[type]
                if (track == null) {
                    val pcm = when (type) {
                        SfxType.PEN_STROKE -> ProceduralAudioGenerator.generatePenStroke()
                        SfxType.BOX_COMPLETED -> ProceduralAudioGenerator.generateBoxCapture()
                        SfxType.BUTTON_CLICK -> ProceduralAudioGenerator.generateButtonClick()
                        SfxType.GAME_WIN -> ProceduralAudioGenerator.generateVictoryFanfare()
                        SfxType.GAME_DRAW -> ProceduralAudioGenerator.generateDrawChime()
                    }
                    track = createStaticTrack(pcm)
                    if (track != null) {
                        proceduralTracks[type] = track
                    }
                }

                track?.let {
                    it.setVolume(current.sfxVolume)
                    it.stop()
                    it.reloadStaticData()
                    it.play()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing procedural sfx $type", e)
            }
        }
    }

    fun playMusic() {
        val current = _settings.value
        if (!current.musicEnabled || current.musicVolume <= 0f) {
            return
        }
        if (isMusicPlaying) return
        isMusicPlaying = true

        // 1. Check for custom raw/bgm
        val bgmResId = appContext.resources.getIdentifier("bgm", "raw", appContext.packageName)
        if (bgmResId != 0) {
            try {
                if (customMediaPlayer == null) {
                    customMediaPlayer = MediaPlayer.create(appContext, bgmResId).apply {
                        isLooping = true
                        setVolume(current.musicVolume, current.musicVolume)
                    }
                }
                customMediaPlayer?.start()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom BGM", e)
            }
        }

        // 2. Play ambient procedural music loop
        startProceduralBgm()
    }

    private fun startProceduralBgm() {
        proceduralBgmJob?.cancel()
        proceduralBgmJob = scope.launch {
            try {
                val pcmLoop = ProceduralAudioGenerator.generateAmbientMusicLoop()
                val sampleRate = ProceduralAudioGenerator.SAMPLE_RATE
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = minBufferSize.coerceAtLeast(pcmLoop.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                proceduralBgmTrack = track
                val currentVol = _settings.value.musicVolume
                track.setVolume(currentVol)
                track.play()

                while (isActive && isMusicPlaying) {
                    var written = 0
                    while (written < pcmLoop.size && isActive && isMusicPlaying) {
                        val toWrite = (pcmLoop.size - written).coerceAtMost(4096)
                        val res = track.write(pcmLoop, written, toWrite)
                        if (res > 0) {
                            written += res
                        } else {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in procedural BGM loop", e)
            }
        }
    }

    fun pauseMusic() {
        isMusicPlaying = false
        customMediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        proceduralBgmJob?.cancel()
        try {
            proceduralBgmTrack?.pause()
            proceduralBgmTrack?.flush()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun resumeMusic() {
        if (_settings.value.musicEnabled && !isMusicPlaying) {
            playMusic()
        }
    }

    fun stopMusic() {
        isMusicPlaying = false
        customMediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        customMediaPlayer = null

        proceduralBgmJob?.cancel()
        try {
            proceduralBgmTrack?.stop()
            proceduralBgmTrack?.release()
        } catch (e: Exception) {
            // Ignored
        }
        proceduralBgmTrack = null
    }

    // --- Settings & Volume Controls ---

    fun setMusicEnabled(enabled: Boolean) {
        val updated = _settings.value.copy(musicEnabled = enabled)
        _settings.value = updated
        preferences.saveSettings(updated)

        if (enabled) {
            playMusic()
        } else {
            pauseMusic()
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        val updated = _settings.value.copy(sfxEnabled = enabled)
        _settings.value = updated
        preferences.saveSettings(updated)
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val updated = _settings.value.copy(musicVolume = clamped)
        _settings.value = updated
        preferences.saveSettings(updated)

        customMediaPlayer?.setVolume(clamped, clamped)
        proceduralBgmTrack?.setVolume(clamped)

        if (clamped > 0f && updated.musicEnabled && !isMusicPlaying) {
            playMusic()
        } else if (clamped == 0f && isMusicPlaying) {
            pauseMusic()
        }
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val updated = _settings.value.copy(sfxVolume = clamped)
        _settings.value = updated
        preferences.saveSettings(updated)

        for (track in proceduralTracks.values) {
            track.setVolume(clamped)
        }
    }

    companion object {
        private const val TAG = "AppAudioManager"

        @Volatile
        private var instance: AppAudioManager? = null

        fun getInstance(context: Context): AppAudioManager {
            return instance ?: synchronized(this) {
                instance ?: AppAudioManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
