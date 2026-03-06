package com.example.chelasmulti_playerpokerdice.game

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.chelasmulti_playerpokerdice.services.FakeGameService
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import androidx.test.core.app.ApplicationProvider

val context = ApplicationProvider.getApplicationContext<Context>()
val profileService = ProfileService(context)

private class FakeNetworkMonitor(private val hasNetwork: Boolean = true) : NetworkMonitor {
    override fun isNetworkAvailable(): Boolean = hasNetwork
    override fun observeNetworkConnectivity(): Flow<Boolean> = flow {
        emit(hasNetwork)
    }
}

class GameScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createFakeViewModel(): GameViewModel {
        val profileService = profileService
        val gameService = FakeGameService()
        val networkMonitor = FakeNetworkMonitor()

        return GameViewModel(gameService, profileService, "1" ,networkMonitor)
    }

    @Test
    fun clicking_rollDiceButton_triggersRollDice() {
        val viewModel = createFakeViewModel()

        composeTestRule.setContent {
            GamePage(
                modifier = Modifier,
                viewModel = viewModel,
                onNavigate = {}
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.currentStateFlow.value is GameScreenState.Playing
        }

        composeTestRule.onNodeWithTag(ROLL_DICE_BUTTON_TAG)
            .assertExists()
            .performClick()

        composeTestRule.runOnIdle {
            val state = viewModel.currentStateFlow.value as GameScreenState.Playing
            assert(state.game.rounds.rollsLeft < 3)
        }
    }

    @Test
    fun clicking_endTurnButton_triggersEndTurn() {
        val viewModel = createFakeViewModel()

        composeTestRule.setContent {
            GamePage(
                modifier = Modifier,
                viewModel = viewModel,
                onNavigate = {}
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.currentStateFlow.value is GameScreenState.Playing
        }

        composeTestRule.onNodeWithTag(END_TURN_BUTTON_TAG)
            .assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            val newState = viewModel.currentStateFlow.value
            assert(newState is GameScreenState.Playing || newState is GameScreenState.GameOver)
        }
    }

    @Test
    fun clicking_scoreboardButton_showsScoreboard() {
        val viewModel = createFakeViewModel()

        composeTestRule.setContent {
            GamePage(
                modifier = Modifier,
                viewModel = viewModel,
                onNavigate = {}
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.currentStateFlow.value is GameScreenState.Playing
        }

        composeTestRule.onNodeWithTag(SCOREBOARD_BUTTON_TAG)
            .assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SCOREBOARD_DISPLAY_TAG)
            .assertExists()
    }

    @Test
    fun clicking_quitButton_triggersNavigateBack() {
        var navigationIntent: GameScreenNavigationIntent? = null
        val viewModel = createFakeViewModel()

        composeTestRule.setContent {
            GamePage(
                modifier = Modifier,
                viewModel = viewModel,
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.currentStateFlow.value is GameScreenState.Playing
        }

        composeTestRule.onNodeWithTag(QUIT_BUTTON_TAG)
            .assertExists()
            .performClick()


        composeTestRule.waitForIdle()
        assert(navigationIntent == null)
    }
}