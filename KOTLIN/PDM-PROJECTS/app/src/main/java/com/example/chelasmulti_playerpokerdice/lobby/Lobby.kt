package com.example.chelasmulti_playerpokerdice.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.services.FakeGameService
import com.example.chelasmulti_playerpokerdice.services.FakeLobbyService
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class LobbyScreenNavigationIntent {
    object Abandon : LobbyScreenNavigationIntent()
    object StartGame : LobbyScreenNavigationIntent()
}

const val START_LOBBY_BUTTON_TAG = "Start_Lobby_button"
const val ABANDON_BUTTON_TAG= "Abandon_button"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyPage(
    modifier: Modifier = Modifier,
    viewModel: LobbyViewModel,
    onNavigate: (LobbyScreenNavigationIntent) -> Unit = {}
) {
    val state = viewModel.currentStateFlow.collectAsState().value
    val snackBarHostState = remember { SnackbarHostState() }
    var chatMessage by remember { mutableStateOf("") }
    var showChatTextField by remember { mutableStateOf(false) }


    LaunchedEffect(null) {
        viewModel.gameReadyToJoin.collect { gameReady ->
            if (gameReady && viewModel.tryMarkNavigated()) {
                onNavigate(LobbyScreenNavigationIntent.StartGame)
            }
        }
    }

    LaunchedEffect(null) {
        viewModel.errorMessageFlow.collect { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.clearErrorMessage()
            }
        }
    }

    when (state) {
        is LobbyScreenState.Loading -> {
            Scaffold(
                containerColor = colorResource(R.color.primary_background)
            ) {
                Column(
                    modifier = modifier.fillMaxSize().padding(it),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is LobbyScreenState.NoNetwork -> {
            var remainingSeconds by remember { mutableStateOf(10) }

            LaunchedEffect(Unit) {
                repeat(10) {
                    delay(1000)
                    remainingSeconds--
                }
                onNavigate(LobbyScreenNavigationIntent.Abandon)
            }

            Scaffold(
                containerColor = colorResource(R.color.primary_background)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(it),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No network connection.\nPlease check your connectivity.",
                            color = colorResource(R.color.primary_text),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Leaving lobby in $remainingSeconds seconds...",
                            color = colorResource(R.color.accent_highlight),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        is LobbyScreenState.Data -> {
            val lobby = state.lobby
            ProvideResponsiveDimensions {
                val dimensions = rememberResponsiveDimensions()

                Scaffold(
                    containerColor = colorResource(R.color.primary_background),
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                    topBar = {
                        DefaultTopBar(
                            modifier = Modifier.padding(top = 16.dp, end = 16.dp),
                            titleContent = {
                                Text(
                                    lobby.name,
                                    textAlign = TextAlign.Center,
                                    fontSize = dimensions.largeFontSize,
                                    lineHeight = dimensions.largeFontSize * 1.2f,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    },
                    bottomBar = {
                        DefaultBottomBar(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MainButton(
                                text = "Abandon Lobby",
                                onClick = { onNavigate(LobbyScreenNavigationIntent.Abandon) },
                                enabled = !lobby.isStarting,
                                modifier = Modifier.testTag(ABANDON_BUTTON_TAG)
                            )
                            if (lobby.host == viewModel.getPlayerName()) {
                                MainButton(
                                    text = "Start Game",
                                    onClick = {
                                        viewModel.onStartGame()
                                    },
                                    enabled = !lobby.isStarting,
                                    modifier = Modifier.testTag(START_LOBBY_BUTTON_TAG)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = modifier
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                modifier = Modifier.weight(0.48f).fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                LobbyCard(
                                    text1 = "Players:[${lobby.playerList.size}/${lobby.size}]",
                                    text2 = lobby.playerList,
                                    text1FontSize = dimensions.mediumFontSize,
                                    text2FontSize = dimensions.regularFontSize,
                                    hostName = lobby.host
                                )
                                LobbyCard(
                                    text1 = "Rounds: ${lobby.rounds}",
                                    text1FontSize = dimensions.mediumFontSize
                                )
                            }
                            LobbyCard(
                                modifier = Modifier
                                    .weight(0.52f)
                                    .height(dimensions.chatCardHeight)
                                    .clickable(indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        enabled = !lobby.isStarting
                                    ) { showChatTextField = true },
                                text1 = "Chat:",
                                text2 = lobby.messageList.map { "${it.player}: ${it.message}" },
                                text1FontSize = dimensions.mediumFontSize,
                                text2FontSize = dimensions.smallFontSize,
                                isChat = true
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (showChatTextField) {
                            ChatTextField(
                                message = chatMessage,
                                onMessageChange = { chatMessage = it },
                                onSend = {
                                    if (chatMessage.isNotBlank()) {
                                        viewModel.onSendMessage(
                                            viewModel.getPlayerName(),
                                            chatMessage
                                        )
                                        chatMessage = ""
                                    }
                                    showChatTextField = false
                                },
                                onDismiss = { showChatTextField = false }
                            )
                        } else {
                            LobbyCard(
                                modifier = Modifier.fillMaxWidth(),
                                text1 = lobby.description,
                                text1FontSize = dimensions.mediumFontSize
                            )
                        }
                    }
                }
            }
        }
        is LobbyScreenState.Empty -> {
            Row(
                modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Lobby not found or has been removed.", color = colorResource(R.color.primary_text))
            }
        }
        is LobbyScreenState.Error -> {
            Row(
                modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(state.message, color = colorResource(R.color.primary_text))
            }
        }
    }
}

@Composable
fun LobbyCard(
    modifier: Modifier = Modifier,
    text1: String,
    text2: List<String> = listOf(),
    text1FontSize: TextUnit = 16.sp,
    text2FontSize: TextUnit = TextUnit.Unspecified,
    isChat: Boolean = false,
    hostName: String? = null
) {
    val dimensions = localResponsiveDimensions()
    val listState = rememberLazyListState()

    LaunchedEffect(text2.size) {
        if (text2.isNotEmpty()) {
            listState.animateScrollToItem(text2.size - 1)
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        colors = CardColors(
            containerColor = colorResource(R.color.secondary_background),
            contentColor = colorResource(R.color.primary_text),
            disabledContainerColor = colorResource(R.color.secondary_background),
            disabledContentColor = colorResource(R.color.primary_text),
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text1,
                textAlign = TextAlign.Justify,
                fontSize = text1FontSize
            )
            @Composable
            fun textList(item: String, isPlayerList: Boolean) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        textAlign = TextAlign.Justify,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = text2FontSize,
                        lineHeight = if(isChat) (text2FontSize.value * 1.6f).sp else TextUnit.Unspecified,
                        modifier = Modifier,
                        maxLines = if(isPlayerList) 1 else Int.MAX_VALUE,
                    )
                    if(isPlayerList && item == hostName) {
                        Icon(
                            painter = painterResource(R.drawable.crown),
                            contentDescription = null,
                            tint = colorResource(R.color.accent_highlight),
                            modifier = Modifier.size(dimensions.mediumIconSize)
                        )
                    }
                }
            }

            if(text2.isNotEmpty()) {
                if(isChat) {
                    Box(modifier = Modifier.height(dimensions.chatHeight).weight(0.8F)) {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(text2) { item -> textList(item, false)}
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.Black.copy(alpha = 0.32f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Click to text",
                            color = Color.White,
                            fontSize = dimensions.smallFontSize,
                        )
                    }
                } else {
                    text2.forEach { item -> textList(item, true) }
                }
            }
        }
    }
}

@Composable
fun ChatTextField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    TextField(
        value = message,
        onValueChange = onMessageChange,
        placeholder = { Text("Type a message...") },
        singleLine = true,
        leadingIcon = {
            IconOnlyButton(
                icon = Icons.Default.Clear,
                onClick = onDismiss,
                modifier = Modifier.scale(0.7f),
                containerColor = Color.Transparent,
                contentColor = colorResource(R.color.accent_highlight)
            )
        },
        trailingIcon = {
            IconOnlyButton(
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = onSend,
                modifier = Modifier.scale(0.7f),
                containerColor = Color.Transparent,
                contentColor = colorResource(R.color.accent_highlight)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp)
    )
}

private class FakeNetworkMonitor : NetworkMonitor {
    override fun isNetworkAvailable(): Boolean = true
    override fun observeNetworkConnectivity(): Flow<Boolean> = flow {
        emit(true)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LobbyPreview() {
    ChelasMultiPlayerPokerDiceTheme {
        val fakeLobbyService = FakeLobbyService()
        LobbyPage(modifier = Modifier, viewModel = LobbyViewModel(fakeLobbyService,
            FakeGameService(fakeLobbyService), ProfileService(LocalContext.current), FakeNetworkMonitor(), "1"))
    }
}