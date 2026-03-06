package com.example.chelasmulti_playerpokerdice.game

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.appServices.GameTaskService
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.components.viewModelInit
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import kotlinx.coroutines.launch

class GameActivity : BaseAudioActivity(){
    val viewModel: GameViewModel by viewModels {
        viewModelInit {
            GameViewModel(
                gameService = (application as ChelasPokerDiceApplication).gameService,
                profileService = (application as ChelasPokerDiceApplication).profileService,
                lobbyId = intent.getStringExtra("lobbyId")!!,
                networkMonitor = (application as ChelasPokerDiceApplication).networkMonitor
            )
        }
    }

    private var gameTaskServiceIntent: Intent? = null
    private var hasLeftGame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioManager.cleanup()
        addAudio("partida.wav")

        val lobbyId = intent.getStringExtra("lobbyId")
        val playerName = viewModel.getPlayerName()

        gameTaskServiceIntent = Intent(this, GameTaskService::class.java).apply {
            putExtra(GameTaskService.EXTRA_GAME_ID, lobbyId)
            putExtra(GameTaskService.EXTRA_PLAYER_NAME, playerName)
        }
        startService(gameTaskServiceIntent!!)

        lifecycleScope.launch {
            viewModel.tieBreakerStartedFlow.collect { isTieBreaker ->
                if (isTieBreaker) {
                    addAudio("rondafinaldesempate.wav")
                }
            }
        }

        val backButtonCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onQuitGame()
            }
        }

        onBackPressedDispatcher.addCallback(this, backButtonCallBack)
        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                GamePage (
                    modifier = Modifier,
                    viewModel = viewModel,
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }

    private fun handleNavigation(it: GameScreenNavigationIntent) {
        when (it) {
            is GameScreenNavigationIntent.Back -> {
                hasLeftGame = true
                gameTaskServiceIntent?.let { intent ->
                    stopService(intent)
                }
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasLeftGame) {
            gameTaskServiceIntent?.let { intent ->
                stopService(intent)
            }
        }
    }
}
