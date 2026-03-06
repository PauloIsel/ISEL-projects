package com.example.chelasmulti_playerpokerdice.lobby

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.game.GameActivity
import com.example.chelasmulti_playerpokerdice.ui.components.AndroidNetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.appServices.LobbyTaskService
import com.example.chelasmulti_playerpokerdice.appServices.GameTaskService
import com.example.chelasmulti_playerpokerdice.ui.components.viewModelInit
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class LobbyActivity : BaseAudioActivity() {
    val viewModel: LobbyViewModel by viewModels {
        viewModelInit {
            LobbyViewModel(
                lobbyService = (application as ChelasPokerDiceApplication).lobbyService,
                gameService = (application as ChelasPokerDiceApplication).gameService,
                profileService = (application as ChelasPokerDiceApplication).profileService,
                lobbyId = intent.getStringExtra("lobbyId")!!,
                networkMonitor = AndroidNetworkMonitor(this)
            )
        }
    }

    private var isStartingGame = false
    private var hasAbandonedLobby = false
    private var isPaused = false
    private var amHost: Boolean = false
    private var lobbyTaskServiceIntent: Intent? = null
    private var pauseJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addAudio("lobby-screen.wav")
        enableEdgeToEdge()

        lobbyTaskServiceIntent = Intent(this, LobbyTaskService::class.java).apply {
            putExtra("lobbyId", intent.getStringExtra("lobbyId"))
        }
        startService(lobbyTaskServiceIntent!!)

        lifecycleScope.launch {
            amHost = viewModel.isHost()
            if (amHost) {
                lobbyTaskServiceIntent?.let {
                    startService(it)
                }
            }
        }

        val backButtonCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!hasAbandonedLobby) {
                    hasAbandonedLobby = true
                    pauseJob?.cancel()
                    viewModel.onAbandonLobby()
                }
                handleNavigation(LobbyScreenNavigationIntent.Abandon)
            }
        }

        onBackPressedDispatcher.addCallback(this, backButtonCallBack)
        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                LobbyPage (
                    modifier = Modifier,
                    viewModel = viewModel,
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }

    private fun handleNavigation(it: LobbyScreenNavigationIntent) {
        when (it) {
            is LobbyScreenNavigationIntent.Abandon -> {
                hasAbandonedLobby = true
                viewModel.onAbandonLobby()
                finish()
            }

            is LobbyScreenNavigationIntent.StartGame -> {
                if (isStartingGame) return
                isStartingGame = true

                lifecycleScope.launch {
                    if (amHost) {
                        val gameTaskIntent = Intent(this@LobbyActivity, GameTaskService::class.java).apply {
                            putExtra(GameTaskService.EXTRA_GAME_ID, viewModel.getLobbyId())
                            putExtra(GameTaskService.EXTRA_PLAYER_NAME, viewModel.getPlayerName())
                        }
                        startService(gameTaskIntent)

                        val removeIntent = Intent(this@LobbyActivity, LobbyTaskService::class.java).apply {
                            action = LobbyTaskService.ACTION_REMOVE_LOBBY
                            putExtra(LobbyTaskService.EXTRA_LOBBY_ID, viewModel.getLobbyId())
                        }
                        startService(removeIntent)
                    }

                    finish()
                    val intent = Intent(this@LobbyActivity, GameActivity::class.java)
                    intent.putExtra("lobbyId", viewModel.getLobbyId())
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        if (!hasAbandonedLobby) {
            pauseJob?.cancel()
            pauseJob = lifecycleScope.launch {
                delay(10000)
                if (isPaused && !hasAbandonedLobby) {
                    hasAbandonedLobby = true
                    viewModel.onAbandonLobby()
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
        pauseJob?.cancel()
        pauseJob = null
    }
}
