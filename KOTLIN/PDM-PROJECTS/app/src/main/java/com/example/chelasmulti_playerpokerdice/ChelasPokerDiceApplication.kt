package com.example.chelasmulti_playerpokerdice

import android.app.Application
import com.example.chelasmulti_playerpokerdice.audio.AudioManager
import com.example.chelasmulti_playerpokerdice.services.FireStoreLobbyService
import com.example.chelasmulti_playerpokerdice.services.FireStoreGameService
import com.example.chelasmulti_playerpokerdice.services.GameService
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.AndroidNetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ChelasPokerDiceApplication : Application() {
    val gameService: GameService by lazy {
        FireStoreGameService(firestore = Firebase.firestore)
    }

    val lobbyService: LobbyService by lazy {
        FireStoreLobbyService(firestore = Firebase.firestore)
    }

    val profileService: ProfileService by lazy { ProfileService(this) }
    val audioManager: AudioManager by lazy { AudioManager(this) }
    val networkMonitor: NetworkMonitor by lazy { AndroidNetworkMonitor(this) }

    override fun onTerminate() {
        super.onTerminate()
        audioManager.cleanup()
    }
}