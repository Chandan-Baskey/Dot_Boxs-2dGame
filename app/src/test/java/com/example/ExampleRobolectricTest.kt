package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioPreferences
import com.example.audio.AudioSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dots and Boxes", appName)
  }

  @Test
  fun testAudioPreferencesPersistence() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = AudioPreferences(context)

    // Save custom audio settings
    val customSettings = AudioSettings(
      musicEnabled = false,
      sfxEnabled = true,
      musicVolume = 0.45f,
      sfxVolume = 0.95f
    )
    prefs.saveSettings(customSettings)

    // Load back and verify persistence
    val loaded = prefs.loadSettings()
    assertFalse(loaded.musicEnabled)
    assertTrue(loaded.sfxEnabled)
    assertEquals(0.45f, loaded.musicVolume, 0.001f)
    assertEquals(0.95f, loaded.sfxVolume, 0.001f)
  }
}

