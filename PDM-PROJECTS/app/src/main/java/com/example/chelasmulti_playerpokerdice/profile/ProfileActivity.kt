package com.example.chelasmulti_playerpokerdice.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme

class ProfileActivity : BaseAudioActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addAudio("title-screen.wav")

        enableEdgeToEdge()

        val profileService = (application as ChelasPokerDiceApplication).profileService
        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                ProfilePage (
                    modifier = Modifier,
                    viewModel = ProfileViewModel(profileService),
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }

    private fun handleNavigation(it: ProfileScreenNavigationIntent) {
        when (it) {
            is ProfileScreenNavigationIntent.Back -> finish()
        }
    }
}