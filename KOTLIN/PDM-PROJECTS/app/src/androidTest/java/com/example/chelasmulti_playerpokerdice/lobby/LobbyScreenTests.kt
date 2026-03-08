package com.example.chelasmulti_playerpokerdice.lobby

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.chelasmulti_playerpokerdice.services.FakeGameService
import com.example.chelasmulti_playerpokerdice.services.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import androidx.test.core.app.ApplicationProvider

val context = ApplicationProvider.getApplicationContext<Context>()
val profileService = ProfileService(context)

private class FakeNetworkMonitor : NetworkMonitor {
    override fun isNetworkAvailable(): Boolean = true
    override fun observeNetworkConnectivity(): Flow<Boolean> = flow {
        emit(true)
    }
}

class LobbyScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeService = FakeLobbyService()

    private val gameService = FakeGameService(fakeService)


    private fun createFakeViewModel(): LobbyViewModel {
        return LobbyViewModel(
            fakeService,
            gameService,
            profileService,
            FakeNetworkMonitor(),
            lobbyId = "1"
        )
    }

    @Test
    fun clicking_startGameButton_triggersNavigateToGame() {
        var navigationIntent: LobbyScreenNavigationIntent? = null
        val fakeService = FakeLobbyService()

        val currentUser = profileService.getUsername()
        fakeService.generateLobbies(0)
        val lobby = fakeService.generateLobbies(1, isHost = true, customHost = currentUser)[0]

        val viewModel = LobbyViewModel(fakeService, FakeGameService(fakeService), profileService, FakeNetworkMonitor(), lobby.id)

        composeTestRule.setContent {
            LobbyPage(
                onNavigate = { navigationIntent = it },
                viewModel = viewModel,
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithTag(START_LOBBY_BUTTON_TAG)
            .assertExists()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            navigationIntent is LobbyScreenNavigationIntent.StartGame
        }

        assert(navigationIntent is LobbyScreenNavigationIntent.StartGame)
    }

    @Test
    fun clicking_abandonButton_triggersNavigateToTitleScreen() {
        var navigationIntent: LobbyScreenNavigationIntent? = null
        val fakeViewModel = createFakeViewModel()
        composeTestRule.setContent {
            LobbyPage(
                onNavigate = { navigationIntent = it },
                viewModel = fakeViewModel,
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithTag(testTag = ABANDON_BUTTON_TAG).performClick()
        assert(navigationIntent == LobbyScreenNavigationIntent.Abandon)
    }
}