package com.example.chelasmulti_playerpokerdice.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.services.FakeGameService
import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.services.ProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.AndroidNetworkMonitor
import com.example.chelasmulti_playerpokerdice.ui.components.DisplayQuitDialog
import com.example.chelasmulti_playerpokerdice.ui.components.MainButton
import com.example.chelasmulti_playerpokerdice.ui.components.evaluateHand
import com.example.chelasmulti_playerpokerdice.ui.theme.ChelasMultiPlayerPokerDiceTheme
import com.example.chelasmulti_playerpokerdice.ui.theme.GameResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.ProvideResponsiveDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.calculateGameDimensions
import com.example.chelasmulti_playerpokerdice.ui.theme.rememberResponsiveDimensions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val ROLL_DICE_BUTTON_TAG = "roll_dice_button"
const val END_TURN_BUTTON_TAG = "end_turn_button"
const val SCOREBOARD_BUTTON_TAG = "scoreboard_button"
const val SCOREBOARD_DISPLAY_TAG = "scoreboard_display"
const val QUIT_BUTTON_TAG = "quit_button"

sealed class GameScreenNavigationIntent {
    class Back : GameScreenNavigationIntent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    onNavigate: (GameScreenNavigationIntent) -> Unit = {}
) {
    ProvideResponsiveDimensions {
        val dimensions = rememberResponsiveDimensions()
        val state = viewModel.currentStateFlow.collectAsState().value
        val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
        val errorMessage by viewModel.errorMessageFlow.collectAsState()
        val reconnectionTimeLeft by viewModel.reconnectionTimeLeft.collectAsState()
        var showScoreboard by remember { mutableStateOf(false) }

        var showRoundWinnerSnackBar by remember { mutableStateOf(false) }
        var roundWinnerName by remember { mutableStateOf("") }
        var showCombinedTieBreakerPopup by remember { mutableStateOf(false) }


        var canClickGameOver by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val gameOverModifier = if (state is GameScreenState.GameOver && canClickGameOver) {
            Modifier.clickable {
                canClickGameOver = false
                scope.launch {
                    viewModel.onLeaveGame()
                    onNavigate(GameScreenNavigationIntent.Back())
                }
            }
        } else Modifier

        val infiniteTransition = rememberInfiniteTransition()
        val showQuitDialog by viewModel.quitGameFlow.collectAsState()

        LaunchedEffect(state) {
            if (state is GameScreenState.GameOver) {
                canClickGameOver = false
                delay(2000)
                canClickGameOver = true
            }
        }

        val scale by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alphaAnimation"
        )

        LaunchedEffect(state) {
            if (state is GameScreenState.Error && state.message == "Game has ended") {
                onNavigate(GameScreenNavigationIntent.Back())
            }
        }

        LaunchedEffect(reconnectionTimeLeft) {
            if (reconnectionTimeLeft == 0) {
                delay(1500)
                onNavigate(GameScreenNavigationIntent.Back())
            }
        }

        LaunchedEffect(viewModel.roundWinnerEvent) {
            viewModel.roundWinnerEvent.collectLatest { evt ->
                roundWinnerName = evt.player.name
                val latestState = viewModel.currentStateFlow.value
                if (evt.showTieBreakerPopup && latestState is GameScreenState.Playing) {
                    try {
                        showRoundWinnerSnackBar = false
                        showCombinedTieBreakerPopup = true
                        delay(3500)
                    } finally {
                        showCombinedTieBreakerPopup = false
                    }
                } else {
                    try {
                        delay(150)
                        val later = viewModel.currentStateFlow.value
                        if (later !is GameScreenState.GameOver) {
                            showCombinedTieBreakerPopup = false
                            showRoundWinnerSnackBar = true
                            delay(2000)
                        }
                    } finally {
                        showRoundWinnerSnackBar = false
                    }
                }
            }
        }

        BackHandler {
            viewModel.onQuitGame()
        }

        Box(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            Image(
                painter = painterResource(R.drawable.poker_table),
                contentDescription = "Poker Table",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            DisplayQuitDialog(
                showDialog = showQuitDialog,
                onDismiss = {
                    viewModel.clearQuitGame()
                },
                onConfirmQuit = {
                    viewModel.clearQuitGame()
                    viewModel.onLeaveGame()
                    onNavigate(GameScreenNavigationIntent.Back())
                }
            )

            Column(
                modifier = modifier.fillMaxSize().then(gameOverModifier),
                verticalArrangement = if (state is GameScreenState.Playing) Arrangement.SpaceBetween else Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (state) {
                    is GameScreenState.Loading -> {
                        Text("Loading...", color = Color.White)
                    }

                    is GameScreenState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Error",
                                color = Color.Red,
                                fontSize = dimensions.titleFontSize,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                state.message,
                                color = Color.White,
                                fontSize = dimensions.smallFontSize,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is GameScreenState.NoNetwork -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 32.dp)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    "NO INTERNET CONNECTION",
                                    color = Color.Red,
                                    fontSize = dimensions.mediumFontSize * 2f,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = if (reconnectionTimeLeft >= 0) "RECONNECTING IN $reconnectionTimeLeft..." else "WAITING FOR CONNECTION...",
                                    color = Color.White,
                                    fontSize = dimensions.mediumFontSize,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    "You will be removed from the game if connection is not restored",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = dimensions.smallFontSize,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }

                    is GameScreenState.GameOver -> {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Game Over!\nWinner: ${state.winners.joinToString(", ") { it.name }}",
                                color = colorResource(R.color.accent_highlight),
                                fontSize = dimensions.titleFontSize * 0.7f,
                                lineHeight = (dimensions.titleFontSize.value * 0.7f).sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 32.dp)
                            )

                            ShowScoreBoard(
                                state = state,
                                modifier = Modifier
                            )

                            Box(
                                modifier = Modifier
                                    .height(64.dp)
                                    .padding(bottom = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (canClickGameOver) {
                                    Text(
                                        "click anywhere to return to the title screen",
                                        color = colorResource(R.color.accent_highlight).copy(scale),
                                        fontSize = dimensions.smallFontSize,
                                    )
                                }
                            }
                        }
                    }

                    is GameScreenState.Playing -> {
                        val tableDesign = viewModel.mapPlayerIndexes(state.game.players.size)

                        val positionToPlayerIndex = tableDesign.mapIndexed { playerIndex, tablePosition ->
                            tablePosition to playerIndex
                        }.toMap()

                        val windowInfo = LocalWindowInfo.current
                        val density = LocalDensity.current
                        val gameDimensions = with(density) {
                            val widthDp = windowInfo.containerSize.width.toDp()
                            val heightDp = windowInfo.containerSize.height.toDp()
                            calculateGameDimensions(widthDp, heightDp)
                        }

                        Box(modifier = modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                positionToPlayerIndex[5]?.let {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        PlayerCard(it, state, viewModel, gameDimensions, reversed = true)
                                    }
                                    Spacer(modifier = Modifier.width(gameDimensions.sideSpacing))
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(gameDimensions.verticalSpacing),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = gameDimensions.playerCardTopPadding)
                                            .height(gameDimensions.playerCardHeight),
                                        horizontalArrangement = Arrangement.spacedBy(gameDimensions.horizontalSpacing),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Spacer(Modifier.width(0.dp))
                                        positionToPlayerIndex[0]?.let { PlayerCard(it, state, viewModel, gameDimensions) }
                                        positionToPlayerIndex[1]?.let { PlayerCard(it, state, viewModel, gameDimensions) }
                                    }

                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DiceTable(
                                            state = state,
                                            viewModel = viewModel,
                                            gameDimensions = gameDimensions
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .padding(bottom = gameDimensions.playerCardBottomPadding)
                                            .height(gameDimensions.playerCardHeight),
                                        horizontalArrangement = Arrangement.spacedBy(gameDimensions.horizontalSpacing),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Spacer(Modifier.width(0.dp))
                                        positionToPlayerIndex[4]?.let { PlayerCard(it, state, viewModel, gameDimensions) }
                                        positionToPlayerIndex[3]?.let { PlayerCard(it, state, viewModel, gameDimensions) }
                                    }
                                }

                                positionToPlayerIndex[2]?.let {
                                    Spacer(modifier = Modifier.width(gameDimensions.sideSpacing))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        PlayerCard(it, state, viewModel, gameDimensions)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state is GameScreenState.Playing) {
                if (isNetworkAvailable) {
                    Row(modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp).align(Alignment.TopStart)) {
                        MainButton(
                            text = "Quit",
                            modifier = Modifier
                                .scale(0.8f)
                                .testTag(QUIT_BUTTON_TAG),
                            onClick = { viewModel.onQuitGame() },
                            colorContrast = true
                        )
                    }

                    Row(modifier = Modifier.padding(vertical = 20.dp).align(Alignment.TopEnd)) {
                        MainButton(
                            text = "Scoreboard",
                            modifier = Modifier
                                .scale(0.8f)
                                .testTag(SCOREBOARD_BUTTON_TAG),
                            onClick = { showScoreboard = true },
                            colorContrast = true
                        )
                    }
                }

                ShowScoreBoard(
                    showScoreboard = showScoreboard,
                    state = state,
                    onClose = { showScoreboard = false }
                )
            }

            if (showCombinedTieBreakerPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                Color(0xFFFF6B00).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 32.dp, vertical = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "TIE BREAKER ROUND!",
                                color = Color.White,
                                fontSize = dimensions.mediumFontSize,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Winner: $roundWinnerName",
                                color = Color.White,
                                fontSize = dimensions.mediumFontSize,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (!isNetworkAvailable && state is GameScreenState.Playing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                Color(0xFFDC3545).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠",
                                color = Color.White,
                                fontSize = dimensions.mediumFontSize
                            )
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty() && state is GameScreenState.Playing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                Color(0xFFDC3545).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.White,
                            fontSize = dimensions.smallFontSize * 0.9f,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


            if (showRoundWinnerSnackBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                Color(0xFF222222).copy(alpha = 0.55f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "$roundWinnerName wins the round!",
                            color = Color.White,
                            fontSize = dimensions.mediumFontSize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerCard(
    playerIndex: Int,
    state: GameScreenState.Playing,
    viewModel: GameViewModel,
    dimensions: GameResponsiveDimensions,
    reversed: Boolean = false
) {
    val game = state.game
    val rounds = game.rounds
    val player = game.players.getOrNull(playerIndex) ?: return

    val alpha = if (rounds.currentPlayerIndex == playerIndex) 1f else 0.3f

    var isConnected by remember { mutableStateOf(viewModel.isPlayerConnected(player)) }
    var secondsOffline by remember { mutableStateOf(0) }

    LaunchedEffect(player.lastSeen, player.isConnected) {
        while (true) {
            isConnected = viewModel.isPlayerConnected(player)
            secondsOffline = if (!isConnected) {
                ((System.currentTimeMillis() - player.lastSeen) / 1000).toInt()
            } else {
                0
            }
            delay(1000)
        }
    }

    val (displayedDiceValues, setDisplayedDiceValues) = remember {
        mutableStateOf(player.dice.map { it.faceValue })
    }
    val (displayedHandRank, setDisplayedHandRank) = remember {
        mutableStateOf<HandRank?>(null)
    }
    val (playerHasRolled, setPlayerHasRolled) = remember {
        mutableStateOf(player.dice.any { it.faceValue > 0 })
    }

    LaunchedEffect(player.dice, rounds.currentPlayerIndex, rounds.rollsLeft) {
        delay(250)
        val hasRolledAfterDelay = player.dice.any { it.faceValue > 0 }
        val newHandRank = if (hasRolledAfterDelay) evaluateHand(player) else null
        setDisplayedDiceValues(player.dice.map { it.faceValue })
        setDisplayedHandRank(newHandRank)
        setPlayerHasRolled(hasRolledAfterDelay)
    }
    val handRankText = displayedHandRank?.let {
        viewModel.handRankToString(it)
    }

    @Composable
    fun AvatarBox() {
        Box(
            modifier = Modifier
                .size(dimensions.avatarSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = alpha))
                    .border(2.dp, Color.White.copy(alpha = alpha), CircleShape)
            ) {
                Text(
                    text = player.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White.copy(alpha = alpha),
                    fontSize = (dimensions.avatarSize.value * 0.5f).sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            val colorVal = if (isConnected) 0xFF28A745 else 0xFFDC3545
            Box(
                modifier = Modifier
                    .size(dimensions.avatarSize * 0.3f)
                    .clip(CircleShape)
                    .background(Color(colorVal))
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }

    @Composable
    fun InfoColumn(alignment: Alignment.Horizontal) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .height(dimensions.avatarSize).width(dimensions.infoWidth),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = player.name,
                color = Color.White.copy(alpha = alpha),
                lineHeight = (dimensions.nameFontSize.value * 0.7f).sp,
                fontSize = dimensions.nameFontSize
            )

            if (!isConnected) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFDC3545).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚠ Offline ${secondsOffline}s",
                        color = Color(0xFFFFFFFF),
                        fontSize = dimensions.nameFontSize * 0.65f,
                        lineHeight = (dimensions.nameFontSize.value * 0.5f).sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(dimensions.diceSize)
            ) {
                if (playerHasRolled) {
                    displayedDiceValues.forEach { dieValue ->
                        Image(
                            painter = painterResource(id = viewModel.diceDrawableFromValue(dieValue)),
                            contentDescription = "Dice $dieValue",
                            modifier = Modifier
                                .size(dimensions.diceSize)
                                .clip(RectangleShape)
                                .alpha(alpha)
                        )
                    }
                }
            }

            handRankText?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = alpha),
                    fontSize = dimensions.handRankFontSize * 0.8f,
                    lineHeight = (dimensions.handRankFontSize.value * 1.2f).sp
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp).widthIn(max = dimensions.cardWidth)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(dimensions.avatarSize)
        ) {
            if (reversed) {
                InfoColumn(Alignment.End)
                AvatarBox()
            } else {
                AvatarBox()
                InfoColumn(Alignment.Start)
            }
        }
    }
}


@Composable
fun DiceTable(
    state: GameScreenState.Playing,
    viewModel: GameViewModel,
    gameDimensions: GameResponsiveDimensions
) {
    val game = state.game
    val rounds = game.rounds
    val currentPlayer = game.players[rounds.currentPlayerIndex]
    val currentHandRank = state.currentHandRank
    val isMyTurn = viewModel.getPlayerName() == currentPlayer.name

    val isAnimating by viewModel.isAnimatingFlow.collectAsState()

    var diceAnimating by remember { mutableStateOf(List(5) { false }) }
    var displayedDiceValues by remember { mutableStateOf(currentPlayer.dice.map { it.faceValue }) }
    var displayedHandRank by remember { mutableStateOf(currentHandRank) }

    val currentDiceValues = currentPlayer.dice.map { it.faceValue }

    LaunchedEffect(rounds.currentPlayerIndex, rounds.rollsLeft, currentDiceValues) {
        diceAnimating = if (state.selectedDice.any { it }) {
            state.selectedDice.map { !it }
        } else { List(5) { true } }

        delay(250)

        displayedDiceValues = currentDiceValues
        displayedHandRank = currentHandRank

        delay(350)

        diceAnimating = List(5) { false }
    }

    val isDiceAnimating = remember(diceAnimating, isAnimating) { diceAnimating.any { it } || isAnimating }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayedDiceValues.forEachIndexed { idx, dieValue ->
                val animating = diceAnimating.getOrElse(idx) { false }
                val rotation by animateFloatAsState(
                    targetValue = if (animating) 360f else 0f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    label = "diceRotation"
                )

                Image(
                    painter = painterResource(id = viewModel.diceDrawableFromValue(dieValue)),
                    contentDescription = "Dice $dieValue",
                    modifier = Modifier
                        .size(gameDimensions.tableDiceSize)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12 * density
                        }
                        .let { m ->
                            if (state.selectedDice.getOrElse(idx) { false }) {
                                m.border(2.dp, Color.Yellow, RectangleShape)
                            } else m
                        }
                        .clickable(enabled = isMyTurn) { viewModel.onToggleDiceSelection(idx) }
                )

                Spacer(modifier = Modifier.width(gameDimensions.tableDiceSpacing))
            }
        }

        displayedHandRank?.let {
            Text(
                viewModel.handRankToString(it),
                color = Color.White,
                fontSize = gameDimensions.tableHandRankFontSize,
                modifier = Modifier.padding(top = gameDimensions.tableTopPadding)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(gameDimensions.tableRowSpacing)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rolls left: ${rounds.rollsLeft}", color = Color.White, fontSize = gameDimensions.tableTextFontSize)
                MainButton(
                    text = "Roll Dice",
                    modifier = Modifier.scale(gameDimensions.tableButtonScale).testTag(ROLL_DICE_BUTTON_TAG),
                    onClick = { viewModel.onRollDice() },
                    colorContrast = true,
                    enabled = isMyTurn && !isDiceAnimating && rounds.rollsLeft > 0
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Round ${rounds.roundNumber} of ${rounds.numberOfRounds}", color = Color.White, fontSize = gameDimensions.tableTextFontSize)
                MainButton(
                    text = "End Turn",
                    modifier = Modifier.scale(gameDimensions.tableButtonScale).testTag(END_TURN_BUTTON_TAG),
                    onClick = { viewModel.endTurn() },
                    colorContrast = true,
                    enabled = isMyTurn && !isDiceAnimating
                )
            }
        }
    }
}

@Composable
fun ShowScoreBoard(
    modifier: Modifier = Modifier,
    showScoreboard: Boolean = true,
    state: GameScreenState,
    onClose: () -> Unit = {}
) {
    if (!showScoreboard) return
    if (state !is GameScreenState.Playing && state !is GameScreenState.GameOver) return

    val dimensions = rememberResponsiveDimensions()
    val players = when (state) {
        is GameScreenState.Playing -> state.game.players
        is GameScreenState.GameOver -> state.allPlayers
        else -> return
    }

    val content = @Composable {
        Card(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .testTag(SCOREBOARD_DISPLAY_TAG),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF222222).copy(alpha = 0.55f),
                contentColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (state is GameScreenState.GameOver) {
                    Text(
                        text = "Scoreboard",
                        fontSize = dimensions.mediumFontSize,
                        color = colorResource(R.color.accent_highlight),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                players.sortedByDescending { it.roundWins }.forEach { player ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = player.name,
                            color = colorResource(R.color.accent_highlight),
                            fontSize = dimensions.regularFontSize
                        )
                        Text(
                            text = "${player.roundWins}",
                            color = Color(0xFF4CAF50),
                            fontSize = dimensions.regularFontSize,
                        )
                    }
                }
            }
        }
    }

    if (state is GameScreenState.Playing) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )

            Box(
                modifier = modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
            ) {
                content()
            }
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=2400dp,height=1080dp,dpi=401")
@Composable
fun GamePreview() {
    ChelasMultiPlayerPokerDiceTheme {
        GamePage(
            modifier = Modifier,
            viewModel = GameViewModel(FakeGameService(), ProfileService(LocalContext.current), "", networkMonitor = AndroidNetworkMonitor(LocalContext.current)
        ))
    }
}