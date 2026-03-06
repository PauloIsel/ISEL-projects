package com.example.chelasmulti_playerpokerdice.lobbies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.about.BACK_BUTTON_TAG
import com.example.chelasmulti_playerpokerdice.services.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultBottomBar
import com.example.chelasmulti_playerpokerdice.ui.components.DefaultTopBar
import com.example.chelasmulti_playerpokerdice.ui.components.IconOnlyButton
import com.example.chelasmulti_playerpokerdice.ui.components.MainButton
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.localResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.rememberResponsiveDimensions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class LobbiesScreenNavigationIntent {
    object Back : LobbiesScreenNavigationIntent()
    object NavigateToLobbyCreation : LobbiesScreenNavigationIntent()
    data class NavigateToLobbyDetails(val lobbyId: String) : LobbiesScreenNavigationIntent()
}

const val LOBBY_BUTTON_TAG = "lobby_button"
const val CREATE_BUTTON_TAG= "create_lobby_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbiesPage(modifier: Modifier = Modifier,
                viewModel: LobbiesViewModel,
                onNavigate: (LobbiesScreenNavigationIntent) -> Unit = {},
) {
    val state = viewModel.currentStateFlow.collectAsState().value
    val isRefreshing = state is LobbiesScreenState.Loading
    val snackBarHostState = remember { SnackbarHostState() }
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

    LaunchedEffect(null) {
        viewModel.lobbySessionFlow.collect { lobbyId ->
            lobbyId.let {
                onNavigate(LobbiesScreenNavigationIntent.NavigateToLobbyDetails(it))
                viewModel.clearJoiningLobby()
            }
        }
    }

    LaunchedEffect(null) {
        viewModel.snackBarMessageFlow.collect { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.clearSnackBarMessage()
            }
        }
    }

    ProvideResponsiveDimensions {
        Scaffold(
            containerColor = colorResource(R.color.primary_background),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                DefaultTopBar(
                    titleContent = {
                        IconOnlyButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onNavigate(LobbiesScreenNavigationIntent.Back) },
                            modifier = Modifier.testTag(BACK_BUTTON_TAG)
                        )
                    }
                )
            },
            bottomBar = {
                DefaultBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    MainButton(
                        text = "Host Game",
                        onClick = { onNavigate(LobbiesScreenNavigationIntent.NavigateToLobbyCreation) },
                        enabled = isNetworkAvailable,
                        modifier = Modifier.testTag(CREATE_BUTTON_TAG)
                    )
                }
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshLobbies() },
                modifier = Modifier.padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.padding(4.dp)) }

                    when (state) {
                        is LobbiesScreenState.Loading -> {}

                        is LobbiesScreenState.Data -> {
                            items(state.lobbies) { lobby ->
                                LobbiesCard(lobby, viewModel)
                            }
                        }

                        is LobbiesScreenState.Empty -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("No lobbies available", color = colorResource(R.color.primary_text))
                                }
                            }
                        }

                        is LobbiesScreenState.NoNetwork -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No network connection.\nPlease check your connectivity.",
                                        color = colorResource(R.color.primary_text),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        is LobbiesScreenState.Error -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(state.message, color = colorResource(R.color.primary_text))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun LobbiesCard(lobby: Lobby, viewModel: LobbiesViewModel) {
    val dimensions = rememberResponsiveDimensions()

    OutlinedCard(
        Modifier.fillMaxWidth(),
        colors = CardColors(
            containerColor = colorResource(R.color.primary_text),
            contentColor = Color.Black,
            disabledContainerColor = colorResource(R.color.accent_highlight),
            disabledContentColor = Color.White
        ),
        border = BorderStroke(color = colorResource(R.color.secondary_background), width = 4.dp)
    ) {
        Column(
            Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                   horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.image),
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.largeIconSize)
                    )
                    Text(
                        lobby.name,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = dimensions.mediumFontSize,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val joiningLobbyId = viewModel.joiningLobbyFlow.collectAsState().value

                MainButton(
                    onClick = {
                        viewModel.onJoinLobby(lobby.id)
                    },
                    modifier = Modifier.testTag(LOBBY_BUTTON_TAG),
                    colorContrast = true,
                    enabled = joiningLobbyId == null || joiningLobbyId == lobby.id,
                    content = {
                        Box(
                            modifier = Modifier.size(width = dimensions.buttonWidth, height = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if(joiningLobbyId == lobby.id) {
                                CircularProgressIndicator(
                                    color = colorResource(R.color.primary_text),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Join", fontSize = dimensions.largeFontSize)
                            }
                        }
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                TextCard("Host", lobby.host)
                TextCard("Players", "${lobby.playerList.size}/${lobby.size}")
                TextCard("Rounds", "${lobby.rounds}")
            }
        }
    }
}

@Composable
fun TextCard(text1: String, text2: String) {
    val dim = localResponsiveDimensions()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(dim.cardWidth)
    ) {
        Text(
            text1,
            color = colorResource(R.color.secondary_text),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = dim.regularFontSize,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text2,
            color = colorResource(R.color.secondary_text),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = dim.regularFontSize,
            modifier = Modifier.fillMaxWidth()
        )
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
fun LobbiesPreview() {
    ChelasMultiPlayerPokerDiceTheme {
        LobbiesPage (modifier = Modifier,
            viewModel = LobbiesViewModel(FakeLobbyService(), profileService = ProfileService(LocalContext.current), networkMonitor = FakeNetworkMonitor()))
    }
}