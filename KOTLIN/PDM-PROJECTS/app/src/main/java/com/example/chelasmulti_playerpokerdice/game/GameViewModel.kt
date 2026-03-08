package com.example.chelasmulti_playerpokerdice.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelasmulti_playerpokerdice.R
import com.example.chelasmulti_playerpokerdice.ui.components.*
import com.example.chelasmulti_playerpokerdice.services.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface GameScreenState {
    object Loading : GameScreenState

    data class Playing(
        val game: Game,
        val selectedDice: List<Boolean>,
        val currentHandRank: HandRank?
    ) : GameScreenState

    data class GameOver(
        val winners: List<Player>,
        val allPlayers: List<Player>
    ) : GameScreenState

    data class Error(val message: String) : GameScreenState

    object NoNetwork : GameScreenState
}

class GameViewModel(
    private val gameService: GameService,
    private val profileService: IProfileService,
    private val lobbyId: String,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    data class RoundWinnerEvent(val player: Player, val isTieBreaker: Boolean, val showTieBreakerPopup: Boolean)

    val quitGameFlow: StateFlow<Boolean> get() = _quitGame
    private val _quitGame = MutableStateFlow(false)

    val currentStateFlow: StateFlow<GameScreenState> get() = _currentState
    private val _currentState = MutableStateFlow<GameScreenState>(GameScreenState.Loading)

    val isAnimatingFlow: StateFlow<Boolean> get() = _isAnimating
    private val _isAnimating = MutableStateFlow(false)

    val tieBreakerStartedFlow: StateFlow<Boolean> get() = _tieBreakerStarted
    private val _tieBreakerStarted = MutableStateFlow(false)

    val isNetworkAvailable: StateFlow<Boolean> get() = _isNetworkAvailable
    private val _isNetworkAvailable = MutableStateFlow(true)

    val errorMessageFlow: StateFlow<String> get() = _errorMessage
    private val _errorMessage = MutableStateFlow("")

    val reconnectionTimeLeft: StateFlow<Int> get() = _reconnectionTimeLeft
    private val _reconnectionTimeLeft = MutableStateFlow(-1)

    private val _roundWinnerEvent = MutableSharedFlow<RoundWinnerEvent>()
    val roundWinnerEvent = _roundWinnerEvent.asSharedFlow()

    private var currentGame: Game? = null
    private var wasTieBreaker = false
    private var cachedGameOver: GameScreenState.GameOver? = null
    private var freezeGameOver: Boolean = false
    private var lastEmittedWinnerName: String? = null
    private var hasRecordedGameResult: Boolean = false
    private var isHandlingSelfDisconnection: Boolean = false
    private var isLeavingGame: Boolean = false
    private var gameStartTime: Long = System.currentTimeMillis()
    private var gameObserverJob: Job? = null
    private var disconnectionStartTime: Long? = null
    private var reconnectionTimerJob: Job? = null
    private var lastRecordedHandKey: String? = null

    companion object {
        private const val PLAYER_TIMEOUT_MS = 10_000L
        private const val HEARTBEAT_INTERVAL_MS = 2_000L
        private const val GAME_INITIALIZATION_GRACE_PERIOD = 3_000L
        private const val RECONNECTION_WINDOW_SECONDS = 10
    }

    init {
        observeNetwork()
        observeGame()
        startHeartbeat()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.observeNetworkConnectivity()
                .distinctUntilChanged()
                .collectLatest { available ->
                    _isNetworkAvailable.value = available

                    if (!available) {
                        if (disconnectionStartTime == null && !isLeavingGame) {
                            disconnectionStartTime = System.currentTimeMillis()
                            startReconnectionCountdown()
                        }

                        if (!isLeavingGame) {
                            _errorMessage.value = "No internet connection"
                            _currentState.value = GameScreenState.NoNetwork
                        }
                    } else {
                        if (!isLeavingGame) {
                            disconnectionStartTime = null
                            reconnectionTimerJob?.cancel()
                            _reconnectionTimeLeft.value = -1
                            _errorMessage.value = ""

                            if (_currentState.value is GameScreenState.NoNetwork) {
                                observeGame()
                            }
                        }
                    }
                }
        }
    }

    private fun startReconnectionCountdown() {
        reconnectionTimerJob?.cancel()
        reconnectionTimerJob = viewModelScope.launch {
            for (secondsElapsed in 0..RECONNECTION_WINDOW_SECONDS) {
                val timeLeft = RECONNECTION_WINDOW_SECONDS - secondsElapsed
                _reconnectionTimeLeft.value = timeLeft
                if (timeLeft <= 0) {
                    val game = currentGame
                    if (game != null) {
                        handleSelfDisconnection(game)
                    } else {
                        _errorMessage.value = "Disconnected for too long"
                        _currentState.emit(GameScreenState.Error("Connection lost"))
                    }
                    break
                }
                if (_isNetworkAvailable.value) break
                delay(1000)
            }
            _reconnectionTimeLeft.value = -1
        }
    }

    private fun startHeartbeat() {
        viewModelScope.launch {
            try {
                while (true) {
                    if (isLeavingGame) break
                    delay(HEARTBEAT_INTERVAL_MS)
                    if (_isNetworkAvailable.value) updatePlayerHeartbeat()
                }
            } catch (_: CancellationException) {
                // viewModelScope cancelled, expected
            } catch (e: Exception) {
                Log.e("GameViewModel", "Heartbeat failed: ${e.message}", e)
            }
        }
    }

    private fun updatePlayerHeartbeat() {
        if (isLeavingGame) return
        val game = currentGame ?: return
        val playerName = profileService.getUsername()
        viewModelScope.launch {
            try {
                gameService.updatePlayerHeartbeat(game.id, playerName)
            } catch (e: Exception) {
                Log.w("GameViewModel", "Failed heartbeat: ${e.message}")
            }
        }
    }

    private fun requireInternet(action: suspend () -> Unit) {
        if (!_isNetworkAvailable.value) {
            _errorMessage.value = "No internet connection. Please check your network."
            return
        }
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("GameViewModel", "Network action failed", e)
            }
        }
    }

    private fun observeGame() {
        gameObserverJob?.cancel()
        gameObserverJob = viewModelScope.launch {
            try {
                gameService.observeGame(lobbyId)
                    .collect { game ->
                        if (freezeGameOver || isLeavingGame) return@collect
                        processGameUpdate(game)
                    }
            } catch (e: Exception) {
                if (!isLeavingGame) {
                    _currentState.value = GameScreenState.Error("Failed to observe game: ${e.message}")
                }
            }
        }
    }

    private suspend fun processGameUpdate(game: Game) {
        if (isLeavingGame) return

        if (game.players.isEmpty()) {
            _currentState.emit(GameScreenState.Error("Game has ended"))
            return
        }

        currentGame = game

        updatePlayersConnectivityStatus(game)

        handleTieBreakerTransition(game)
        handleLastWinnerEvent(game)

        if (handleGameOverIfNeeded(game)) return

        emitPlayingState(game)
        autoRollIfNeeded(game)
    }

    private fun updatePlayersConnectivityStatus(game: Game) {
        val currentTime = System.currentTimeMillis()
        val timeSinceGameStart = currentTime - gameStartTime

        if (timeSinceGameStart < GAME_INITIALIZATION_GRACE_PERIOD) return

        var needsUpdate = false
        val myName = profileService.getUsername()

        val updatedPlayers = game.players.map { player ->
            val timeSinceLastSeen = currentTime - player.lastSeen
            val hasValidLastSeen = player.lastSeen in 1..currentTime && player.lastSeen >= (currentTime - 60_000L)

            val isCurrentlyConnected = if (hasValidLastSeen) {
                timeSinceLastSeen < PLAYER_TIMEOUT_MS
            } else true

            if (player.isConnected && !isCurrentlyConnected) {
                needsUpdate = true
                Log.d("GameViewModel", "Player ${player.name} marked as disconnected")
            }

            if (player.name == myName && hasValidLastSeen && timeSinceLastSeen >= PLAYER_TIMEOUT_MS && !isHandlingSelfDisconnection) {
                isHandlingSelfDisconnection = true
                viewModelScope.launch { handleSelfDisconnection(game) }
            }

            player.copy(isConnected = isCurrentlyConnected)
        }.toMutableList()

        if (needsUpdate) {
            viewModelScope.launch {
                try {
                    gameService.updateGameState(game.copy(players = updatedPlayers))
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Failed to update game state: ${e.message}", e)
                }
            }
        }

        removeDisconnectedPlayers(game)
    }

    private suspend fun handleSelfDisconnection(game: Game) {
        if (isLeavingGame) {
            isHandlingSelfDisconnection = false
            return
        }

        try {
            _errorMessage.value = "You have been disconnected from the game"

            if (!hasRecordedGameResult) {
                try {
                    profileService.recordGameResult(didWin = false)
                    hasRecordedGameResult = true
                    Log.d("GameViewModel", "Recorded loss due to self disconnection")
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Failed to record game result: ${e.message}", e)
                }
            }

            val myName = profileService.getUsername()
            gameService.removePlayerFromGameTransaction(game.id, myName)
        } catch (e: Exception) {
            if (!isLeavingGame) _currentState.emit(GameScreenState.Error("Disconnected: ${e.message}"))
        } finally {
            isHandlingSelfDisconnection = false
        }
    }

    //Demasiado grande
    private fun removeDisconnectedPlayers(game: Game) {
        if (isLeavingGame) return

        val currentTime = System.currentTimeMillis()
        val timeSinceGameStart = currentTime - gameStartTime

        if (timeSinceGameStart < GAME_INITIALIZATION_GRACE_PERIOD) return

        val myName = profileService.getUsername()

        val playersToRemove = game.players.filter { player ->
            val timeSinceLastSeen = currentTime - player.lastSeen
            val hasValidLastSeen = player.lastSeen in 1..currentTime && player.lastSeen >= (currentTime - 60_000L)

            hasValidLastSeen && timeSinceLastSeen >= PLAYER_TIMEOUT_MS && !player.isConnected && player.name != myName
        }

        if (playersToRemove.isEmpty()) return

        viewModelScope.launch {
            try {
                val remaining = game.players.filter { it !in playersToRemove }.toMutableList()

                if (remaining.isEmpty()) {
                    gameService.removeGame(game.id)
                    _currentState.emit(GameScreenState.Error("All players disconnected"))
                    return@launch
                }

                if (remaining.size == 1 && remaining[0].name == myName) {
                    Log.d("GameViewModel", "Last player standing - forcing game over")

                    val roundsRemaining = game.rounds.numberOfRounds - game.rounds.roundNumber + 1
                    val lastPlayer = remaining[0].copy(
                        roundWins = remaining[0].roundWins + roundsRemaining
                    )

                    val updatedStartingPlayers = game.startingPlayers.map { sp ->
                        if (sp.name == lastPlayer.name) sp.copy(roundWins = lastPlayer.roundWins) else sp
                    }.toMutableList()

                    val updatedGame = game.copy(
                        players = mutableListOf(lastPlayer),
                        startingPlayers = updatedStartingPlayers,
                        rounds = game.rounds.copy(numberOfRounds = -1)
                    )

                    gameService.updateGameState(updatedGame)
                    return@launch
                }

                val newPlayerIndex = if (game.rounds.currentPlayerIndex >= remaining.size) 0 else game.rounds.currentPlayerIndex
                val updatedGame = game.copy(players = remaining, rounds = game.rounds.copy(currentPlayerIndex = newPlayerIndex))
                gameService.updateGameState(updatedGame)

                if (!isLeavingGame) {
                    _errorMessage.value = playersToRemove.joinToString { it.name } + " disconnected"
                    delay(3000)
                    _errorMessage.value = ""
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Failed to remove disconnected players: ${e.message}", e)
            }
        }
    }

    private fun handleTieBreakerTransition(game: Game) {
        if (game.isTieBreaker && !wasTieBreaker) {
            _tieBreakerStarted.value = true
            wasTieBreaker = true
        } else if (!game.isTieBreaker) {
            wasTieBreaker = false
        }
    }

    private suspend fun handleLastWinnerEvent(game: Game) {
        val winner = game.lastRoundWinner ?: return
        if (winner.name != lastEmittedWinnerName) {
            lastEmittedWinnerName = winner.name
            val clone = clonePlayer(winner)
            val showTieBreaker = game.isTieBreaker && !isGameOver(game)
            safeEmit(_roundWinnerEvent, RoundWinnerEvent(clone, game.isTieBreaker, showTieBreaker))
        }
        game.lastRoundWinner = null
    }

    private suspend fun handleGameOverIfNeeded(game: Game): Boolean {
        if (!isGameOver(game)) return false

        if (freezeGameOver && cachedGameOver != null) {
            _currentState.emit(cachedGameOver!!)
            return true
        }

        freezeGameOver = true
        syncStartingPlayersScores(game)

        val winners = getWinners(game).map { clonePlayer(it) }
        val allPlayers = game.startingPlayers.map { clonePlayer(it) }

        recordGameResultOnce(winners)

        cachedGameOver = GameScreenState.GameOver(winners, allPlayers)
        _currentState.emit(cachedGameOver!!)

        return true
    }

    private suspend fun recordGameResultOnce(winners: List<Player>) {
        if (hasRecordedGameResult) return
        val name = profileService.getUsername()
        val didWin = winners.any { it.name == name }
        try {
            profileService.recordGameResult(didWin)
            hasRecordedGameResult = true
        } catch (e: Exception) {
            Log.w("GameViewModel", "Failed to record final result: ${e.message}")
        }
    }

    private suspend fun emitPlayingState(game: Game) {
        val player = getCurrentPlayer(game)
        val handRank = player?.let { if (it.dice.any { d -> d.faceValue > 0 }) evaluateHand(it) else null }
        val selectedDice = resolveSelectedDice(game)

        _currentState.emit(
            GameScreenState.Playing(
                game = game,
                selectedDice = selectedDice,
                currentHandRank = handRank
            )
        )
    }

    private fun resolveSelectedDice(game: Game): List<Boolean> {
        val ui = _currentState.value as? GameScreenState.Playing
        return when {
            game.selectedDice.isNotEmpty() -> game.selectedDice
            ui != null && ui.game.rounds.currentPlayerIndex == game.rounds.currentPlayerIndex && ui.game.rounds.roundNumber == game.rounds.roundNumber -> ui.selectedDice
            else -> List(5) { false }
        }
    }

    private fun autoRollIfNeeded(game: Game) {
        val player = getCurrentPlayer(game) ?: return
        if (player.dice.all { it.faceValue == 0 }) onRollDice()
    }

    fun onLeaveGame() = requireInternet {
        isLeavingGame = true
        reconnectionTimerJob?.cancel()
        gameObserverJob?.cancel()

        val playerName = profileService.getUsername()
        clearCachedGameOver(false)
        val game = currentGame ?: return@requireInternet

        if (_currentState.value is GameScreenState.GameOver) {
            viewModelScope.launch { runCatching { gameService.removePlayerFromGameTransaction(game.id, playerName) } }
            return@requireInternet
        }

        val remaining = game.players.filter { it.name != playerName }
        when (remaining.size) {
            0 -> viewModelScope.launch { runCatching { gameObserverJob?.cancel(); gameService.removeGame(game.id) } }
            1 -> handleLastPlayerExit(game, remaining)
            else -> publishGameState(game.copy(players = remaining.toMutableList()))
        }
    }

    private fun handleLastPlayerExit(game: Game, remaining: List<Player>) {
        viewModelScope.launch {
            try {
                if (remaining.isEmpty()) {
                    gameService.removeGame(game.id)
                    return@launch
                }

                val updatedGame = if (!isGameOver(game)) {
                    val roundsRemaining = game.rounds.numberOfRounds - game.rounds.roundNumber + 1
                    val lastPlayer = remaining[0].copy(roundWins = remaining[0].roundWins + roundsRemaining)

                    val updatedStartingPlayers = game.startingPlayers.map { sp -> if (sp.name == lastPlayer.name) sp.copy(roundWins = lastPlayer.roundWins) else sp }.toMutableList()

                    game.copy(players = mutableListOf(lastPlayer), startingPlayers = updatedStartingPlayers, rounds = game.rounds.copy(numberOfRounds = -1))
                } else game.copy(players = remaining.toMutableList())

                gameService.updateGameState(updatedGame)
            } catch (e: Exception) {
                Log.e("GameViewModel", "handleLastPlayerExit failed: ${e.message}", e)
            }
        }
    }

    fun onRollDice() = requireInternet {
        val ui = _currentState.value as? GameScreenState.Playing ?: return@requireInternet
        val game = currentGame ?: return@requireInternet

        if (game.rounds.rollsLeft <= 0) { endTurn(); return@requireInternet }

        val next = game.copy(
            players = game.players.map { it.copy(dice = it.dice.map { d -> Dice(d.faceValue) }.toMutableList()) }.toMutableList(),
            rounds = game.rounds.copy()
        )

        val current = getCurrentPlayer(next) ?: return@requireInternet
        val indices = if (game.rounds.rollsLeft == 3) null else ui.selectedDice.mapIndexedNotNull { i, s -> if (!s) i else null }
        if (game.rounds.rollsLeft == 3) rollAll(current) else if (indices?.isNotEmpty() == true) rollSome(current, indices)

        next.rounds.rollsLeft--
        publishGameState(next)
    }

    fun endTurn() = requireInternet {
        val game = currentGame ?: return@requireInternet
        if (_currentState.value !is GameScreenState.Playing) return@requireInternet
        if (_isAnimating.value) return@requireInternet

        val next = game.copy(players = game.players.map { it.copy() }.toMutableList(), rounds = game.rounds.copy(), selectedDice = emptyList())

        val currentPlayer = getCurrentPlayer(next)
        if (currentPlayer != null) {
            recordLocalHand(currentPlayer)
        }

        moveToNextPlayer(next, viewModelScope, ::compareHands, ::recordLocalHand) { w ->
            lastEmittedWinnerName = w.name
            viewModelScope.launch { safeEmit(_roundWinnerEvent, RoundWinnerEvent(clonePlayer(w), next.isTieBreaker, next.isTieBreaker && !isGameOver(next))) }
        }

        viewModelScope.launch {
            try {
                _isAnimating.value = true
                publishGameState(next)
                delay(600)
            } finally {
                _isAnimating.value = false
            }
        }
    }

    private fun publishGameState(game: Game) {
        viewModelScope.launch {
            try {
                gameService.updateGameState(game)
            } catch (e: Exception) {
                _currentState.emit(GameScreenState.Error("Failed to update game: ${e.message}"))
            }
        }
    }

    private fun recordLocalHand(player: Player) {
        val game = currentGame ?: return
        val roundNumber = game.rounds.roundNumber
        val playersPlayed = game.rounds.playersPlayedThisRound
        val diceValues = player.dice.joinToString(",") { it.faceValue.toString() }
        val handKey = "${player.name}#$roundNumber#$playersPlayed#$diceValues"

        if (lastRecordedHandKey == handKey) {
            return
        }

        lastRecordedHandKey = handKey

        val playerCopy = clonePlayer(player)
        val hasDice = playerCopy.dice.any { it.faceValue > 0 }

        viewModelScope.launch {
            try {
                val local = profileService.getUsername()
                if (playerCopy.name == local && hasDice) {
                    val hand = evaluateHand(playerCopy)
                    profileService.recordHand(hand)
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "recordLocalHand FAILED: ${e.message}", e)
            }
        }
    }

    fun onQuitGame() { _quitGame.value = true }
    fun clearQuitGame() { _quitGame.value = false }

    fun onToggleDiceSelection(index: Int) = requireInternet {
        val state = _currentState.value as? GameScreenState.Playing ?: return@requireInternet
        val newSelected = state.selectedDice.toMutableList()
        if (index !in newSelected.indices) return@requireInternet

        newSelected[index] = !newSelected[index]
        _currentState.value = state.copy(selectedDice = newSelected)

        val baseGame = currentGame ?: return@requireInternet
        val nextGameState = baseGame.copy(selectedDice = newSelected)
        publishGameState(nextGameState)
    }

    fun diceDrawableFromValue(value: Int): Int = when (value) {
        1 -> R.drawable.dice_six_faces_one
        2 -> R.drawable.dice_six_faces_two
        3 -> R.drawable.dice_six_faces_three
        4 -> R.drawable.dice_six_faces_four
        5 -> R.drawable.dice_six_faces_five
        6 -> R.drawable.dice_six_faces_six
        else -> R.drawable.dice_six_faces_one
    }

    fun mapPlayerIndexes(players: Int): List<Int> = when (players) {
        2 -> TableDesign.TwoPlayers.players
        3 -> TableDesign.ThreePlayers.players
        4 -> TableDesign.FourPlayers.players
        5 -> TableDesign.FivePlayers.players
        6 -> TableDesign.SixPlayers.players
        else -> emptyList()
    }

    fun handRankToString(rank: HandRank): String = when (rank) {
        HandRank.FIVE_OF_A_KIND -> "Five of a Kind"
        HandRank.FOUR_OF_A_KIND -> "Four of a Kind"
        HandRank.FULL_HOUSE -> "Full House"
        HandRank.STRAIGHT -> "Straight"
        HandRank.THREE_OF_A_KIND -> "Three of a Kind"
        HandRank.TWO_PAIR -> "Two Pair"
        HandRank.PAIR -> "Pair"
        HandRank.BUST -> "Bust"
    }

    fun getPlayerName(): String = profileService.getUsername()

    fun isPlayerConnected(player: Player): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - player.lastSeen) < PLAYER_TIMEOUT_MS
    }

    fun clearCachedGameOver(resetRecorded: Boolean = true) {
        cachedGameOver = null
        freezeGameOver = false
        if (resetRecorded) hasRecordedGameResult = false
    }

    suspend fun <T> safeEmit(flow: MutableSharedFlow<T>, value: T) {
        try { flow.emit(value) } catch (e: Exception) { Log.w("GameViewModel", "safeEmit failed: ${e.message}") }
    }
}