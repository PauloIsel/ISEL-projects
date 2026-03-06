package com.example.chelasmulti_playerpokerdice.title

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chelasmulti_playerpokerdice.about.AboutActivity
import com.example.chelasmulti_playerpokerdice.lobbies.LobbiesActivity
import com.example.chelasmulti_playerpokerdice.profile.ProfileActivity
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme


class MainActivity : BaseAudioActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        addAudio("title-screen.wav")

        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                TitleScreen(
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }

    private fun handleNavigation(intent: TitleScreenNavigationIntent) {
        when (intent) {
            TitleScreenNavigationIntent.NavigateToAbout -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }

            TitleScreenNavigationIntent.NavigateToProfile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            TitleScreenNavigationIntent.NavigateToLobbies -> {
                startActivity(Intent(this, LobbiesActivity::class.java))
            }
        }
    }
}
