package com.example.chelasmulti_playerpokerdice.lobbyCreation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.lobby.LobbyActivity
import com.example.chelasmulti_playerpokerdice.ui.components.AndroidNetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.components.viewModelInit
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme

class LobbyCreationActivity : BaseAudioActivity() {
    val viewModel: LobbyCreationViewModel by viewModels {
        viewModelInit {
            LobbyCreationViewModel(
                lobbyService = (application as ChelasPokerDiceApplication).lobbyService,
                profileService = (application as ChelasPokerDiceApplication).profileService,
                networkMonitor = AndroidNetworkMonitor(this)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addAudio("lobby-screen.wav")
        enableEdgeToEdge()
        
        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                LobbyCreationPage(
                    modifier = Modifier,
                    viewModel = viewModel,
                    onNavigate = { handleNavigation(it) },
                    onCreateLobby =  { viewModel.onCreateLobby() }
                )
            }
        }
    }

    private fun handleNavigation(it: LobbyCreationScreenNavigationIntent) {
        when (it) {
            is LobbyCreationScreenNavigationIntent.Back -> finish()
            is LobbyCreationScreenNavigationIntent.ToLobby -> {
                finish()
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("lobbyId", it.lobby.id)
                startActivity(intent)
            }
        }
    }
}
