package com.example.chelasmulti_playerpokerdice.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultBottomBar
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultTopBar
import com.example.chelasmulti_playerpokerdice.ui.components.GameRulesSection
import com.example.chelasmulti_playerpokerdice.ui.components.IconOnlyButton
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.localResponsiveDimensions

sealed class AboutScreenNavigationIntent {
    data class Github(val destination: String) : AboutScreenNavigationIntent()
    data class Email(val emails: List<String>) : AboutScreenNavigationIntent()
    object Back : AboutScreenNavigationIntent()
}
const val BACK_BUTTON_TAG = "back_button"
const val EMAIL_BUTTON_TAG = "email_button"
const val GITHUB_BUTTON_TAG= "github_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(modifier: Modifier = Modifier, onNavigate: (AboutScreenNavigationIntent) -> Unit = {}) {
    ProvideResponsiveDimensions {
        val dimensions = localResponsiveDimensions()

        val textBoxModifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.secondary_background),
                shape = RoundedCornerShape(32.dp)
            )

        Scaffold(
            containerColor = colorResource(R.color.primary_background),
            topBar = {
                DefaultTopBar(
                    titleContent = {
                        IconOnlyButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onNavigate(AboutScreenNavigationIntent.Back) },
                            modifier = Modifier.testTag(tag = BACK_BUTTON_TAG)
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

            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = textBoxModifier
                            .weight(0.67f)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.about_developers),
                            fontSize = dimensions.largeFontSize,
                            color = colorResource(R.color.primary_text)
                        )

                        DevCard(
                            name = R.string.diogo_cardoso,
                            number = R.string._51474
                        )

                        DevCard(
                            name = R.string.daniel_santos,
                            number = R.string._51701
                        )

                        DevCard(
                            name = R.string.paulo_magalh_es,
                            number = R.string._51702
                        )
                    }

                    Column (
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.contact_us),
                            fontSize = dimensions.mediumFontSize,
                            color = colorResource(R.color.primary_text),
                        )
                        IconOnlyButton(
                            icon = Icons.Default.Email,
                            onClick = { onNavigate(AboutScreenNavigationIntent.Email(listOf("A51474@alunos.isel.pt",
                                "A51701@alunos.isel.pt", "A51702@alunos.isel.pt"))) },
                            modifier = Modifier.testTag(tag = EMAIL_BUTTON_TAG)
                        )
                        IconOnlyButton(
                            icon = ImageVector.vectorResource(R.drawable.github),
                            onClick = { onNavigate(AboutScreenNavigationIntent.Github("https://github.com/isel-leic-pdm/course-assignment-leirtg02")) },
                            modifier = Modifier.testTag(tag = GITHUB_BUTTON_TAG)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                GameRulesSection()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DevCard(modifier: Modifier = Modifier, name: Int, number: Int) {
    val dimensions = localResponsiveDimensions()
    Column {
        Text(
            text = stringResource(name),
            fontSize = dimensions.mediumFontSize,
            textAlign = TextAlign.Justify,
            color = colorResource(R.color.primary_text),
            modifier = modifier
        )
        Text(
            text = stringResource(number),
            fontSize = dimensions.regularFontSize,
            textAlign = TextAlign.Justify,
            lineHeight = dimensions.regularFontSize,
            color = colorResource(R.color.primary_text),
            modifier = modifier.padding(bottom = 8.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AboutPreview() {
    ChelasMultiPlayerPokerDiceTheme {
        AboutPage(modifier = Modifier)
    }
}