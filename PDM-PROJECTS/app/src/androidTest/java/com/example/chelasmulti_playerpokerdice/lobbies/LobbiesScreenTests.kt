package com.example.chelasmulti_playerpokerdice.lobbies

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.chelasmulti_playerpokerdice.about.BACK_BUTTON_TAG
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

class LobbiesScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeService = FakeLobbyService()

    private fun createFakeViewModel(): LobbiesViewModel {
        return LobbiesViewModel(fakeService, profileService, FakeNetworkMonitor())
    }

    @Test
    fun clicking_backButton_triggersNavigateToMainIntent() {
        var navigationIntent: LobbiesScreenNavigationIntent? = null
        val fakeViewModel = createFakeViewModel()

        composeTestRule.setContent {
            LobbiesPage(
                modifier = Modifier,
                viewModel = fakeViewModel,
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()
        assert(navigationIntent is LobbiesScreenNavigationIntent.Back)
    }

    @Test
    fun clicking_createLobbyButton_triggersNavigateToCreateLobby() {
        var navigationIntent: LobbiesScreenNavigationIntent? = null
        val fakeViewModel = createFakeViewModel()

        composeTestRule.setContent {
            LobbiesPage(
                modifier = Modifier,
                viewModel = fakeViewModel,
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(CREATE_BUTTON_TAG).performClick()
        assert(navigationIntent == LobbiesScreenNavigationIntent.NavigateToLobbyCreation)
    }

    @Test
    fun clicking_joinButton_triggersNavigateToLobbyDetails() {
        var navigationIntent: LobbiesScreenNavigationIntent? = null
        val fakeViewModel = createFakeViewModel()

        composeTestRule.setContent {
            LobbiesPage(
                modifier = Modifier,
                viewModel = fakeViewModel,
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag(LOBBY_BUTTON_TAG)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag(LOBBY_BUTTON_TAG)
            .onFirst()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            navigationIntent != null
        }

        assert(navigationIntent is LobbiesScreenNavigationIntent.NavigateToLobbyDetails)
    }
}
