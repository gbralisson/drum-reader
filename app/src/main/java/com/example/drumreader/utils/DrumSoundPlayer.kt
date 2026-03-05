package com.example.drumreader.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log
import com.example.drumreader.R

class DrumSoundPlayer(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Mapping based on common drum notation (General MIDI / MusicXML mapping used)
        // Assuming the following files exist in res/raw:
        // kick, snare, hihat, ride, crash, tom_high, tom_mid, tom_low
        
        loadSound(context, "F4", "kick")
        loadSound(context, "C5", "snare")
        loadSound(context, "G5", "hihat")
        loadSound(context, "F5", "ride")
        loadSound(context, "A5", "crash")
        loadSound(context, "E5", "tom_high")
        loadSound(context, "D5", "tom_mid")
        loadSound(context, "A4", "tom_low")
    }

    private fun loadSound(context: Context, noteKey: String, fileName: String) {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            soundMap[noteKey] = soundPool.load(context, resId, 1)
        } else {
            Log.w("DrumSoundPlayer", "Resource raw/$fileName not found. Using beep fallback.")
        }
    }

    fun playNote(step: String, octave: Int) {
        val key = "${step.uppercase()}$octave"
        val soundId = soundMap[key]

        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        } else {
            // Fallback to different tones based on the drum part
            val tone = when (key) {
                "F4" -> ToneGenerator.TONE_PROP_BEEP // Kick
                "C5" -> ToneGenerator.TONE_PROP_BEEP2 // Snare
                "G5" -> ToneGenerator.TONE_CDMA_PIP // Hi-hat
                "A5" -> ToneGenerator.TONE_CDMA_HIGH_L // Crash
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            toneGenerator.startTone(tone, 50)
        }
    }

    fun release() {
        soundPool.release()
        toneGenerator.release()
    }
}
