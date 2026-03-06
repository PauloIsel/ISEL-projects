package com.example.chelasmulti_playerpokerdice.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ProfileScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class MockProfileService(
        initialUsername: String = "TestPlayer",
        initialGamesPlayed: Int = 10,
        initialGamesWon: Int = 5,
        initialHandFrequency: Map<HandRank, Int> = mapOf(
            HandRank.FIVE_OF_A_KIND to 1,
            HandRank.FOUR_OF_A_KIND to 2,
            HandRank.FULL_HOUSE to 3,
            HandRank.STRAIGHT to 4,
            HandRank.THREE_OF_A_KIND to 5,
            HandRank.TWO_PAIR to 6,
            HandRank.PAIR to 7,
            HandRank.BUST to 8
        )
    ) : IProfileService {
        private val _usernameFlow = MutableStateFlow(initialUsername)
        private val _gamesPlayedFlow = MutableStateFlow(initialGamesPlayed)
        private val _gamesWonFlow = MutableStateFlow(initialGamesWon)
        private val _handFrequencyFlow = MutableStateFlow(initialHandFrequency)

        override val usernameFlow: Flow<String> = _usernameFlow
        override val gamesPlayedFlow: Flow<Int> = _gamesPlayedFlow
        override val gamesWonFlow: Flow<Int> = _gamesWonFlow
        override val handFrequencyFlow: Flow<Map<HandRank, Int>> = _handFrequencyFlow

        var savedUsername: String? = null

        override suspend fun saveUserName(name: String) {
            savedUsername = name
            _usernameFlow.value = name
        }

        override suspend fun recordGameResult(didWin: Boolean) {}

        override suspend fun recordHand(hand: HandRank) {}

        override fun getUsername(): String {
            return _usernameFlow.value
        }
    }

    @Test
    fun backButton_triggersBackNavigation() {
        var navigationIntent: ProfileScreenNavigationIntent? = null
        val mockService = MockProfileService()

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()
        assert(navigationIntent is ProfileScreenNavigationIntent.Back)
    }

    @Test
    fun profileScreen_displaysUsername() {
        val mockService = MockProfileService(initialUsername = "TESTPLAYER")

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithTag(NICKNAME_TEXTFIELD_TAG)
            .assertIsDisplayed()
            .assertTextContains("TESTPLAYER")
    }

    @Test
    fun profileScreen_displaysHandFrequencies() {
        val mockService = MockProfileService(
            initialHandFrequency = mapOf(
                HandRank.FIVE_OF_A_KIND to 2,
                HandRank.FOUR_OF_A_KIND to 4,
                HandRank.FULL_HOUSE to 6
            )
        )

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("Hand frequencies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Five of a Kind").assertIsDisplayed()
        composeTestRule.onNodeWithText("Four of a Kind").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full House").assertIsDisplayed()
    }

    @Test
    fun nicknameTextField_allowsTextInput() {
        val mockService = MockProfileService(initialUsername = "OLDNAME")

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithTag(NICKNAME_TEXTFIELD_TAG)
            .performTextClearance()
        composeTestRule.onNodeWithTag(NICKNAME_TEXTFIELD_TAG)
            .performTextInput("NEWNAME")

        composeTestRule.onNodeWithTag(NICKNAME_TEXTFIELD_TAG)
            .assertTextContains("NEWNAME")
    }

    @Test
    fun profileScreen_displaysAllHandRanks() {
        val mockService = MockProfileService()

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        val handRankNames = listOf(
            "Five of a Kind",
            "Four of a Kind",
            "Full House",
            "Straight",
            "Three of a Kind",
            "Two Pair",
            "Pair",
            "Bust"
        )

        handRankNames.forEach { handName ->
            composeTestRule.onNodeWithText(handName).assertIsDisplayed()
        }
    }

    @Test
    fun profileScreen_displaysInitialWithCorrectLetter() {
        val mockService = MockProfileService(initialUsername = "Alice")

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysCorrectVersionNumber() {
        val mockService = MockProfileService()

        composeTestRule.setContent {
            ProfilePage(
                viewModel = ProfileViewModel(mockService),
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("Chelas Multi-Player Poker Dice \n v1.0")
            .assertIsDisplayed()
    }
}

