package com.example.chelasmulti_playerpokerdice.audio

import android.content.Context
import android.media.MediaPlayer

class AudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioFile: String? = null
    private var activityCount = 0

    var audioEnabled: Boolean = true
    var audioVolume: Float = 1.0f
    var audioLooping: Boolean = true

    fun playAudio(audioFile: String) {
        if (currentAudioFile == audioFile && mediaPlayer?.isPlaying == true) {
            return
        }

        if (currentAudioFile != audioFile) {
            releaseAudio()
            currentAudioFile = audioFile
            initializeAndPlayAudio(audioFile)
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    private fun initializeAndPlayAudio(audioFile: String) {
        if (!audioEnabled) return

        try {
            if (mediaPlayer == null) {
                val afd = context.assets.openFd(audioFile)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    isLooping = audioLooping
                    setVolume(audioVolume, audioVolume)
                    start()
                }
                afd.close()
            } else if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioManager", "Error playing audio", e)
        }
    }

    fun pauseAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioManager", "Error pausing audio", e)
        }
    }

    private fun releaseAudio() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            android.util.Log.e("AudioManager", "Error releasing audio", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun onActivityStarted() {
        activityCount++
    }

    fun onActivityStopped() {
        activityCount--
        if (activityCount <= 0) {
            pauseAudio()
        }
    }

    fun cleanup() {
        releaseAudio()
        currentAudioFile = null
    }
}

