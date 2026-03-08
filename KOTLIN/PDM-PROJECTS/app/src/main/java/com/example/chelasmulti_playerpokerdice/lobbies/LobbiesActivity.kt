package com.example.chelasmulti_playerpokerdice.lobbies

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.lobby.LobbyActivity
import com.example.chelasmulti_playerpokerdice.lobbyCreation.LobbyCreationActivity
import com.example.chelasmulti_playerpokerdice.ui.components.AndroidNetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.components.viewModelInit

class LobbiesActivity : BaseAudioActivity() {

    val viewModel: LobbiesViewModel by viewModels {
        viewModelInit {
            LobbiesViewModel(
                lobbyService = (application as ChelasPokerDiceApplication).lobbyService,
                profileService = (application as ChelasPokerDiceApplication).profileService,
                networkMonitor = AndroidNetworkMonitor(this),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addAudio("lobby-screen.wav")
        enableEdgeToEdge()

        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                LobbiesPage (
                    modifier = Modifier,
                    viewModel = viewModel,
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }

    private fun handleNavigation(it: LobbiesScreenNavigationIntent) {
        when (it) {
            is LobbiesScreenNavigationIntent.Back -> finish()
            is LobbiesScreenNavigationIntent.NavigateToLobbyCreation -> {
                startActivity(Intent(this, LobbyCreationActivity::class.java))
            }
            is LobbiesScreenNavigationIntent.NavigateToLobbyDetails -> {
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("lobbyId", it.lobbyId)
                startActivity(intent)
            }
        }
    }
}
