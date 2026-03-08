package com.example.chelasmulti_playerpokerdice.about

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AboutScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clicking_backButton_triggersNavigateToMainIntent() {
        var navigationIntent: AboutScreenNavigationIntent? = null
        composeTestRule.setContent {
            AboutPage(
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(testTag = BACK_BUTTON_TAG).performClick()
        assert(navigationIntent is AboutScreenNavigationIntent.Back)
    }

    @Test
    fun clicking_githubButton_triggersNavigateToGithub() {
        var navigationIntent: AboutScreenNavigationIntent? = null
        composeTestRule.setContent {
            AboutPage(
                onNavigate = { navigationIntent = it }
            )
        }

        composeTestRule.onNodeWithTag(testTag = GITHUB_BUTTON_TAG).performClick()
        assert(navigationIntent == AboutScreenNavigationIntent.Github("https://github.com/isel-leic-pdm/course-assignment-leirtg02"))
    }

    @Test
    fun clicking_emailButton_triggersNavigateToEmail() {
        var navigationIntent: AboutScreenNavigationIntent? = null
        composeTestRule.setContent {
            AboutPage(
                onNavigate = { navigationIntent = it }
            )
        }

        val expected = AboutScreenNavigationIntent.Email(listOf("A51474@alunos.isel.pt", "A51701@alunos.isel.pt", "A51702@alunos.isel.pt"))

        composeTestRule.onNodeWithTag(testTag = EMAIL_BUTTON_TAG).performClick()
        assert(navigationIntent is AboutScreenNavigationIntent.Email && (navigationIntent as AboutScreenNavigationIntent.Email).emails.containsAll(expected.emails))
    }


}