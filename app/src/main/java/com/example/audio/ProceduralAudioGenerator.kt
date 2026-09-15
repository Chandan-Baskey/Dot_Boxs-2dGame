package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural synthesis for clean, soft, minimalist tactile game sounds.
 * Avoids loud or flashy sounds, prioritizing subtle pen friction and light ticks.
 */
object ProceduralAudioGenerator {

    const val SAMPLE_RATE = 44100

    /**
     * Very subtle, soft pen stroke sound (~110ms).
     * Sounds like a gentle graphite or ballpoint glide on smooth paper.
     */
    fun generatePenStroke(): ShortArray {
        val durationSec = 0.11f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val rng = Random(1337)

        var lastFilter = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Gentle bell envelope
            val envelope = sin(PI * progress)

            // Low-pass filtered noise for paper friction
            val noise = rng.nextDouble(-1.0, 1.0)
            lastFilter = 0.72 * lastFilter + 0.28 * noise

            // Very subtle pen roller texture
            val t = i.toDouble() / SAMPLE_RATE
            val roller = sin(2.0 * PI * 1400.0 * t) * 0.1

            val sample = (lastFilter * 0.85 + roller) * envelope * 0.15
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Soft, clean box completion tick (~100ms).
     * A quiet, satisfying gentle wood/paper tap instead of a loud musical fanfare.
     */
    fun generateBoxCapture(): ShortArray {
        val durationSec = 0.10f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 35.0)
            val tone = sin(2.0 * PI * 540.0 * t) + 0.3 * sin(2.0 * PI * 1080.0 * t)
            val sample = tone * decay * 0.18
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Very light, clean micro-tick for buttons (~15ms).
     */
    fun generateButtonClick(): ShortArray {
        val durationSec = 0.015f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress) * (1.0 - progress)
            val tone = sin(2.0 * PI * 1200.0 * t) * decay * 0.14
            buffer[i] = (tone.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Soft, calm resolution chord for game win (~400ms).
     * Very soft and warm, completely non-flashy.
     */
    fun generateVictoryFanfare(): ShortArray {
        val durationSec = 0.40f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val f1 = 523.25 // C5
        val f2 = 659.25 // E5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 8.0)
            val tone = (sin(2.0 * PI * f1 * t) * 0.6 + sin(2.0 * PI * f2 * t) * 0.4)
            val sample = tone * decay * 0.16
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Soft, calm neutral resolution for tie game (~350ms).
     */
    fun generateDrawChime(): ShortArray {
        val durationSec = 0.35f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val f = 440.0 // A4

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 9.0)
            val tone = sin(2.0 * PI * f * t)
            val sample = tone * decay * 0.14
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Very soft, peaceful acoustic background ambiance (quiet and calm, low in mix).
     */
    fun generateAmbientMusicLoop(): ShortArray {
        val durationSec = 12.0f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val notes = listOf(
            Pair(0.0, 261.63),   // C4
            Pair(1.2, 329.63),   // E4
            Pair(2.4, 392.00),   // G4
            Pair(3.6, 523.25),   // C5
            Pair(4.8, 392.00),   // G4
            Pair(6.0, 329.63),   // E4
            Pair(7.2, 293.66),   // D4
            Pair(8.4, 261.63),   // C4
            Pair(9.6, 392.00),   // G4
            Pair(10.8, 329.63)   // E4
        )

        for ((startSec, freq) in notes) {
            val startSample = (startSec * SAMPLE_RATE).toInt()
            val noteDurationSamples = (SAMPLE_RATE * 1.5).toInt()

            for (j in 0 until noteDurationSamples) {
                val idx = startSample + j
                if (idx >= numSamples) break
                val t = j.toDouble() / SAMPLE_RATE
                val decay = exp(-t / 0.5)
                val tone = sin(2.0 * PI * freq * t) + 0.2 * sin(2.0 * PI * freq * 2.0 * t)
                val sample = tone * decay * 0.08
                val current = buffer[idx].toDouble() / Short.MAX_VALUE
                val mixed = (current + sample).coerceIn(-0.8, 0.8)
                buffer[idx] = (mixed * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }
}
