package com.example.cascadestudy.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.example.cascadestudy.R

// Helper class for playing custom audio effects for study timer events
open class SoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    // Plays the audio effect for the start of a study interval
    open fun playStartStudySound() {
        playSound(R.raw.start_study_interval)
    }

    // Plays the audio effect for completing a study interval and starting rest
    open fun playFinishStudySound() {
        playSound(R.raw.finish_study_interval)
    }

    // Plays the audio effect for completing an entire study session
    open fun playFinishSessionSound() {
        playSound(R.raw.finish_session)
    }

    // Initializes and starts audio playback for the specified raw audio resource ID
    private fun playSound(@RawRes soundResId: Int) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Releases any active MediaPlayer instances to free system resources
    open fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
