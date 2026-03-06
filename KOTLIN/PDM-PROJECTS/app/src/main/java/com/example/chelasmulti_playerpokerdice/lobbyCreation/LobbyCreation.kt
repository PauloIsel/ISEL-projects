package com.example.chelasmulti_playerpokerdice.lobbyCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.about.BACK_BUTTON_TAG
import com.example.chelasmulti_playerpokerdice.services.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultTopBar
import com.example.chelasmulti_playerpokerdice.ui.components.IconOnlyButton
import com.example.chelasmulti_playerpokerdice.ui.components.MainButton
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.localResponsiveDimensions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class LobbyCreationScreenNavigationIntent {
    object Back : LobbyCreationScreenNavigationIntent()
    data class ToLobby(val lobby: Lobby) : LobbyCreationScreenNavigationIntent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyCreationPage(
    modifier: Modifier = Modifier,
    viewModel: LobbyCreationViewModel,
    onNavigate: (LobbyCreationScreenNavigationIntent) -> Unit = {},
    onCreateLobby: () -> Unit = {}
) {

    val state = viewModel.currentStateFlow.collectAsState().value
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val maxRounds = 60

    LaunchedEffect(null) {
        viewModel.snackBarMessageFlow.collect { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.clearSnackBarMessage()
            }
        }
    }

    ProvideResponsiveDimensions {
        val dimensions = localResponsiveDimensions()

        Scaffold(
            containerColor = colorResource(R.color.primary_background),
            topBar = {
                DefaultTopBar(
                    titleContent = {
                        IconOnlyButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onNavigate(LobbyCreationScreenNavigationIntent.Back) },
                            modifier = Modifier.testTag(BACK_BUTTON_TAG)
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            contentWindowInsets = WindowInsets.ime
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state) {
                    is LobbyCreationScreenState.Data -> {
                        OutlinedTextField(
                            value = state.lobbyName,
                            onValueChange = { viewModel.onLobbyNameChange(it)},
                            label = { Text(stringResource(R.string.lobbycreation_lobby_name)) },
                            isError = !state.isLobbyNameValid,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onDescriptionChange(it) },
                            label = { Text(stringResource(R.string.lobbycreation_lobby_description)) },
                            isError = !state.isDescriptionValid,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            maxLines = 6
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.lobbycreation_players),
                                color = colorResource(R.color.primary_text),
                                fontSize = dimensions.mediumFontSize,
                                modifier = Modifier.width(80.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            DropdownMenuBox(
                                value = state.players,
                                options = (2..6).toList(),
                                onValueChange = { viewModel.onPlayersChange(it); if (state.rounds % it != 0) viewModel.onRoundsChange(it) }
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.lobbycreation_rounds),
                                color = colorResource(R.color.primary_text),
                                fontSize = dimensions.mediumFontSize,
                                modifier = Modifier.width(80.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            DropdownMenuBox(
                                value = state.rounds,
                                options = (state.players..maxRounds step state.players).toList(),
                                onValueChange = { viewModel.onRoundsChange(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MainButton(
                                text = stringResource(R.string.lobbycreation_create_lobby),
                                enabled = state.isLobbyNameValid && state.isDescriptionValid && isNetworkAvailable,
                                onClick = { onCreateLobby() },
                            )
                        }
                    }

                    is LobbyCreationScreenState.NoNetwork -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No network connection.\nPlease check your connectivity.",
                                color = colorResource(R.color.primary_text),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    is LobbyCreationScreenState.Success -> {
                        LaunchedEffect(state) {
                            onNavigate(LobbyCreationScreenNavigationIntent.ToLobby(state.lobby))
                        }
                    }

                    is LobbyCreationScreenState.Error -> {
                        LaunchedEffect(state) {
                            snackBarHostState.showSnackbar(state.message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(value: Int, options: List<Int>, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.width(IntrinsicSize.Max)) {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonColors(
                containerColor = colorResource(R.color.secondary_background),
                contentColor = colorResource(R.color.primary_text),
                disabledContainerColor = colorResource(R.color.secondary_background),
                disabledContentColor = colorResource(R.color.primary_text),
            )
        ) {
            Text("$value")
        }
        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp))
        {
            options.forEach {
                DropdownMenuItem(
                    text = { Text("$it") },
                    onClick = { onValueChange(it); expanded = false }
                )
            }
        }
    }
}

private class FakeNetworkMonitor : NetworkMonitor {
    override fun isNetworkAvailable(): Boolean = true
    override fun observeNetworkConnectivity(): Flow<Boolean> = flow {
        emit(true)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LobbyCreationPreview() {
    ChelasMultiPlayerPokerDiceTheme {
        LobbyCreationPage (modifier = Modifier,
            viewModel = LobbyCreationViewModel(
                lobbyService = FakeLobbyService(),
                profileService = ProfileService(LocalContext.current),
                networkMonitor = FakeNetworkMonitor()
            )
        )
    }
}