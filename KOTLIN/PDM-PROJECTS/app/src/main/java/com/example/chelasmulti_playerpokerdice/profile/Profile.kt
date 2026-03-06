package com.example.chelasmulti_playerpokerdice.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultBottomBar
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultTopBar
import com.example.chelasmulti_playerpokerdice.ui.components.IconOnlyButton
import com.example.chelasmulti_playerpokerdice.ui.components.MainButton
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.localResponsiveDimensions

const val BACK_BUTTON_TAG = "profile_back_button"
const val SAVE_BUTTON_TAG = "profile_save_button"
const val RESET_BUTTON_TAG = "profile_reset_button"
const val NICKNAME_TEXTFIELD_TAG = "profile_nickname_textfield"

sealed class ProfileScreenNavigationIntent {
    class Back : ProfileScreenNavigationIntent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onNavigate: (ProfileScreenNavigationIntent) -> Unit = {}
) {
    val username by viewModel.editableName.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val gamesPlayed by viewModel.gamesPlayed.collectAsState()
    val gamesWon by viewModel.gamesWon.collectAsState()
    val handFreq by viewModel.handFrequency.collectAsState()

    ProvideResponsiveDimensions {
        val dimensions = localResponsiveDimensions()

        Scaffold(
            containerColor = colorResource(R.color.primary_background),
            topBar = {
                DefaultTopBar(
                    titleContent = {
                        IconOnlyButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onNavigate(ProfileScreenNavigationIntent.Back()) },
                            modifier = Modifier.testTag(BACK_BUTTON_TAG)
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
            }
        ) { innerPadding ->

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.primary_background))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Player Profile",
                                color = colorResource(R.color.primary_text)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            val initial = username.trim().ifEmpty { "P" }[0].uppercaseChar()
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 28.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { viewModel.updateName(it) },
                                label = {
                                    Text(
                                        text = "Nickname",
                                        color = colorResource(R.color.primary_text)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(NICKNAME_TEXTFIELD_TAG)
                            )

                            if (errorMessage.isNotBlank()) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MainButton(
                                    onClick = { viewModel.save() },
                                    modifier = Modifier
                                        .size(width = 140.dp, height = 44.dp)
                                        .testTag(SAVE_BUTTON_TAG),
                                ) {
                                    Text(text = "Save")
                                }

                                MainButton(
                                    onClick = { viewModel.resetName() },
                                    modifier = Modifier
                                        .size(width = 140.dp, height = 44.dp)
                                        .testTag(RESET_BUTTON_TAG),
                                ) {
                                    Text(text = "Reset")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.primary_background))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Statistics",
                                style = MaterialTheme.typography.titleSmall,
                                color = colorResource(R.color.primary_text)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Games played / won
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Games played", style = MaterialTheme.typography.labelLarge, color = colorResource(R.color.primary_text))
                                Text("$gamesPlayed", style = MaterialTheme.typography.bodyLarge, color = colorResource(R.color.primary_text))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Games won", style = MaterialTheme.typography.labelLarge, color = colorResource(R.color.primary_text))
                                Text("$gamesWon", style = MaterialTheme.typography.bodyLarge, color = colorResource(R.color.primary_text))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .alpha(0.06f)
                                    .background(colorResource(R.color.primary_text))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Hand frequencies",
                                style = MaterialTheme.typography.titleSmall,
                                color = colorResource(R.color.primary_text)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val ranks = listOf(
                                HandRank.FIVE_OF_A_KIND,
                                HandRank.FOUR_OF_A_KIND,
                                HandRank.FULL_HOUSE,
                                HandRank.STRAIGHT,
                                HandRank.THREE_OF_A_KIND,
                                HandRank.TWO_PAIR,
                                HandRank.PAIR,
                                HandRank.BUST
                            )

                            fun prettyName(rank: HandRank) = when (rank) {
                                HandRank.FIVE_OF_A_KIND -> "Five of a Kind"
                                HandRank.FOUR_OF_A_KIND -> "Four of a Kind"
                                HandRank.FULL_HOUSE -> "Full House"
                                HandRank.STRAIGHT -> "Straight"
                                HandRank.THREE_OF_A_KIND -> "Three of a Kind"
                                HandRank.TWO_PAIR -> "Two Pair"
                                HandRank.PAIR -> "Pair"
                                HandRank.BUST -> "Bust"
                            }

                            val leftColumn = ranks.filterIndexed { index, _ -> index % 2 == 0 }
                            val rightColumn = ranks.filterIndexed { index, _ -> index % 2 == 1 }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    leftColumn.forEach { r ->
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(prettyName(r), style = MaterialTheme.typography.bodySmall, color = colorResource(R.color.primary_text), modifier = Modifier.weight(1f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${handFreq[r] ?: 0}", style = MaterialTheme.typography.bodySmall, color = colorResource(R.color.primary_text), modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    rightColumn.forEach { r ->
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(prettyName(r), style = MaterialTheme.typography.bodySmall, color = colorResource(R.color.primary_text), modifier = Modifier.weight(1f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${handFreq[r] ?: 0}", style = MaterialTheme.typography.bodySmall, color = colorResource(R.color.primary_text), modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}





@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    ChelasMultiPlayerPokerDiceTheme {
        ProfilePage (modifier = Modifier, viewModel = ProfileViewModel(ProfileService(LocalContext.current)))
    }
}
