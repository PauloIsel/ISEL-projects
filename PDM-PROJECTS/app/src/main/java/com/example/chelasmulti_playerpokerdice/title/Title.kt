package com.example.chelasmulti_playerpokerdice.title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultBottomBar
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultTopBar
import com.example.chelasmulti_playerpokerdice.ui.components.MainButton
import com.example.chelasmulti_playerpokerdice.ui.components.ThemedIconButton
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.rememberResponsiveDimensions


enum class TitleScreenNavigationIntent {
    NavigateToProfile,
    NavigateToAbout,
    NavigateToLobbies
}

const val PROFILE_BUTTON_TAG = "profile_button"
const val ABOUT_BUTTON_TAG = "about_button"
const val LOBBIES_BUTTON_TAG = "lobbies_button"
const val TITLE_TEXT_TAG = "title_text"

val HomeVideoFontFamily = FontFamily(
    Font(R.font.homevideo, FontWeight.Normal),
    Font(R.font.homevideobold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleScreen(
    modifier: Modifier = Modifier,
    onNavigate: (TitleScreenNavigationIntent) -> Unit = {},
    fontFamily: FontFamily = HomeVideoFontFamily
) {
    ProvideResponsiveDimensions {
        val dimensions = rememberResponsiveDimensions()
        Scaffold(
            containerColor = colorResource(R.color.primary_background),
            topBar = {
                DefaultTopBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    navigationIconContent = {
                        ThemedIconButton(
                            icon = Icons.Default.Person,
                            onClick = { onNavigate(TitleScreenNavigationIntent.NavigateToProfile) },
                            modifier = Modifier.testTag(PROFILE_BUTTON_TAG)
                        )
                    },
                    actionsContent = {
                        ThemedIconButton(
                            icon = Icons.Default.Info,
                            onClick = { onNavigate(TitleScreenNavigationIntent.NavigateToAbout) },
                            modifier = Modifier.testTag(ABOUT_BUTTON_TAG)
                        )
                    }
                )
            },
            bottomBar = {
                DefaultBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement  = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.app_version),
                        color = colorResource(R.color.primary_text),
                        textAlign = TextAlign.Center,
                        fontSize = dimensions.smallFontSize
                    )
                }
            },
        ) { innerPadding ->

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.widthIn(max = dimensions.screenWidthDp)
                    ) {
                        TitleText(
                            text = "Chelas\nMultiplayer\nPoker Dice",
                            fontFamily = fontFamily,
                            modifier = Modifier.testTag(tag = TITLE_TEXT_TAG),
                            fontSize = dimensions.titleFontSize
                        )
                    }

                    MainButton(
                        text = stringResource(R.string.lobbies),
                        onClick = { onNavigate(TitleScreenNavigationIntent.NavigateToLobbies) },
                        modifier = Modifier.testTag(tag = LOBBIES_BUTTON_TAG)
                    )
                }
            }
        }
    }
}

@Composable
fun TitleText(
    text: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        fontFamily = fontFamily,
        lineHeight = fontSize,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Preview(showBackground = false, showSystemUi = true)
@Composable
fun TitleScreenPreview() {
    TitleScreen()
}