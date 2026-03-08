package com.example.chelasmulti_playerpokerdice.lobbyCreation

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
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

class LobbyCreationScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clicking_backButton_triggersNavigateToLobbiesIntent() {
        var navigationIntent: LobbyCreationScreenNavigationIntent? = null

        val fakeViewModel = LobbyCreationViewModel(FakeLobbyService(), profileService, FakeNetworkMonitor())

        composeTestRule.setContent {
            LobbyCreationPage(
                modifier = Modifier,
                viewModel = fakeViewModel,
                onNavigate = { navigationIntent = it },
                onCreateLobby = {}
            )
        }

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()
        assert(navigationIntent is LobbyCreationScreenNavigationIntent.Back)
    }
}