package com.example.audio

import android.content.Context
import android.content.SharedPreferences

data class AudioSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.8f
)

enum class SfxType {
    PEN_STROKE,
    BOX_COMPLETED,
    BUTTON_CLICK,
    GAME_WIN,
    GAME_DRAW
}

class AudioPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun loadSettings(): AudioSettings {
        return AudioSettings(
            musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true),
            sfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true),
            musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, 0.7f),
            sfxVolume = prefs.getFloat(KEY_SFX_VOLUME, 0.8f)
        )
    }

    fun saveSettings(settings: AudioSettings) {
        prefs.edit()
            .putBoolean(KEY_MUSIC_ENABLED, settings.musicEnabled)
            .putBoolean(KEY_SFX_ENABLED, settings.sfxEnabled)
            .putFloat(KEY_MUSIC_VOLUME, settings.musicVolume)
            .putFloat(KEY_SFX_VOLUME, settings.sfxVolume)
            .apply()
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
    }

    fun setSfxEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SFX_ENABLED, enabled).apply()
    }

    fun setMusicVolume(volume: Float) {
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, volume.coerceIn(0f, 1f)).apply()
    }

    fun setSfxVolume(volume: Float) {
        prefs.edit().putFloat(KEY_SFX_VOLUME, volume.coerceIn(0f, 1f)).apply()
    }

    companion object {
        private const val PREFS_NAME = "dots_and_boxes_audio_prefs"
        private const val KEY_MUSIC_ENABLED = "key_music_enabled"
        private const val KEY_SFX_ENABLED = "key_sfx_enabled"
        private const val KEY_MUSIC_VOLUME = "key_music_volume"
        private const val KEY_SFX_VOLUME = "key_sfx_volume"
    }
}
