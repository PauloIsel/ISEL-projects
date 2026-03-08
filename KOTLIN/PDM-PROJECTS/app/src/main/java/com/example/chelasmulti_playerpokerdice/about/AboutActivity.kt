package com.example.chelasmulti_playerpokerdice.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.example.chelasmulti_playerpokerdice.ui.components.BaseAudioActivity
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme

class AboutActivity : BaseAudioActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addAudio("title-screen.wav")

        enableEdgeToEdge()
        setContent {
            ChelasMultiPlayerPokerDiceTheme {
                AboutPage(
                    modifier = Modifier,
                    onNavigate = { handleNavigation(it) }
                )
            }
        }
    }
    private fun handleNavigation(it: AboutScreenNavigationIntent) {
        when (it) {
            is AboutScreenNavigationIntent.Github -> navigateToURL(it.destination)
            is AboutScreenNavigationIntent.Back -> finish()
            is AboutScreenNavigationIntent.Email -> emailRedirect(it.emails)
        }
    }

    private fun navigateToURL(destination: String) {
        val webpage: Uri = destination.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }

    private fun emailRedirect(emails: List<String>) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            setData("mailto:".toUri())
            putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        }

        startActivity(intent)
    }
}