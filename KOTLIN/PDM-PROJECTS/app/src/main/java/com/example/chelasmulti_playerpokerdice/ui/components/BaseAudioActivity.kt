package com.example.chelasmulti_playerpokerdice.ui.components

import androidx.activity.ComponentActivity
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.audio.AudioManager

open class BaseAudioActivity : ComponentActivity() {
    protected val audioManager: AudioManager by lazy {
        (application as ChelasPokerDiceApplication).audioManager
    }

    private var currentAudioFile: String? = null

    protected fun addAudio(audioFile: String) {
        currentAudioFile = audioFile
        audioManager.playAudio(audioFile)
    }

    override fun onStart() {
        super.onStart()
        audioManager.onActivityStarted()
    }

    override fun onResume() {
        super.onResume()
        currentAudioFile?.let { audioManager.playAudio(it) }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        audioManager.onActivityStopped()
    }
}
