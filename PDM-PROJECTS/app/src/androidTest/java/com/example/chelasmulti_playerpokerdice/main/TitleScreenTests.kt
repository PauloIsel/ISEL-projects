package com.example.chelasmulti_playerpokerdice.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.chelasmulti_playerpokerdice.title.ABOUT_BUTTON_TAG
import com.example.chelasmulti_playerpokerdice.title.LOBBIES_BUTTON_TAG
import com.example.chelasmulti_playerpokerdice.title.PROFILE_BUTTON_TAG
import com.example.chelasmulti_playerpokerdice.title.TitleScreen
import com.example.chelasmulti_playerpokerdice.title.TitleScreenNavigationIntent
import org.junit.Rule
import org.junit.Test

class TitleScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clicking_profileButton_triggersNavigateToProfileIntent() {
        var navigationIntent: TitleScreenNavigationIntent? = null
        composeTestRule.setContent {
            TitleScreen(
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(testTag = PROFILE_BUTTON_TAG).performClick()
        assert(navigationIntent == TitleScreenNavigationIntent.NavigateToProfile)
    }

    @Test
    fun clicking_AboutButton_triggersNavigateToAbout() {
        var navigationIntent: TitleScreenNavigationIntent? = null
        composeTestRule.setContent {
            TitleScreen(
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(testTag = ABOUT_BUTTON_TAG).performClick()
        assert(navigationIntent == TitleScreenNavigationIntent.NavigateToAbout)
    }

    @Test
    fun clicking_LobbiesButton_triggersNavigateToLobbiesIntent() {
        var navigationIntent: TitleScreenNavigationIntent? = null
        composeTestRule.setContent {
            TitleScreen(
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(testTag = LOBBIES_BUTTON_TAG).performClick()
        assert(navigationIntent == TitleScreenNavigationIntent.NavigateToLobbies)
    }
}