package com.example.chelasmulti_playerpokerdice.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelasmulti_playerpokerdice.services.GameService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface LobbyScreenState {
    object Loading : LobbyScreenState
    data class Data(val lobby: Lobby) : LobbyScreenState
    object Empty : LobbyScreenState
    object NoNetwork : LobbyScreenState
    data class Error(val message: String) : LobbyScreenState
}

class LobbyViewModel(
    private val lobbyService: LobbyService,
    private val gameService: GameService,
    private val profileService: IProfileService,
    private val networkMonitor: NetworkMonitor,
    private val lobbyId: String
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val currentState = MutableStateFlow<LobbyScreenState>(LobbyScreenState.Loading)
    val currentStateFlow: StateFlow<LobbyScreenState> = currentState.asStateFlow()

    private val gameStarted = MutableStateFlow(false)
    val gameStartedFlow: StateFlow<Boolean> = gameStarted.asStateFlow()

    private val errorMessage = MutableStateFlow("")
    val errorMessageFlow: StateFlow<String> = errorMessage.asStateFlow()

    private val _hasNavigated = MutableStateFlow(false)

    private val _gameReadyToJoin = MutableStateFlow(false)
    val gameReadyToJoin: StateFlow<Boolean> = _gameReadyToJoin.asStateFlow()

    init {
        observeNetworkAndLobby()
        observeGameCreation()
    }

    private fun observeNetworkAndLobby() {
        viewModelScope.launch {
            networkMonitor.observeNetworkConnectivity()
                .distinctUntilChanged()
                .collectLatest { isAvailable ->
                    _isNetworkAvailable.value = isAvailable

                    if (!isAvailable) {
                        currentState.emit(LobbyScreenState.NoNetwork)
                    } else {
                        currentState.emit(LobbyScreenState.Loading)

                        lobbyService.observeLobby(lobbyId)
                            .map { lobby ->
                                lobby.gameStarted.takeIf { it != gameStarted.value }?.let {
                                    gameStarted.emit(it)
                                }
                                lobby
                            }
                            .catch { e ->
                                currentState.emit(LobbyScreenState.Error(e.message ?: "Unknown error"))
                            }
                            .collect { lobby ->
                                currentState.emit(LobbyScreenState.Data(lobby))
                            }
                    }
                }
        }
    }

    private fun observeGameCreation() {
        viewModelScope.launch {
            try {
                gameStartedFlow.collect { started ->
                    if (started && !_gameReadyToJoin.value) {
                        try {
                            gameService.observeGame(lobbyId).collect { game ->
                                if (game.id.isNotEmpty() && game.players.isNotEmpty()) {
                                    _gameReadyToJoin.emit(true)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun requireInternet(action: suspend () -> Unit) {
        if (!_isNetworkAvailable.value) {
            viewModelScope.launch { errorMessage.emit("No internet connection") }
        } else {
            viewModelScope.launch {
                try {
                    action()
                } catch (e: IllegalStateException) {
                    errorMessage.emit(e.message ?: "An error occurred")
                } catch (e: IllegalArgumentException) {
                    errorMessage.emit(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun onStartGame() = requireInternet {
        val player = profileService.getUsername()

        val lobby = lobbyService.getLobby(lobbyId)
        if (lobby?.host != player) throw IllegalStateException("Only the host can start the game")

        if (lobby.playerList.size < 2) {
            throw IllegalStateException("Need at least 2 players to start the game")
        }

        val createdGameId = try {
            gameService.createGameFromLobbyTransaction(lobbyId)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create game: ${e.message}")
        }

        if (createdGameId == null) {
            throw IllegalStateException("Failed to create game: returned null")
        }

        val countdownCompleted = sendMessagesDuringCountdown()

        if (!countdownCompleted) { return@requireInternet }

        val finalLobby = lobbyService.getLobby(lobbyId)
        if (finalLobby == null || finalLobby.playerList.size < 2 || !finalLobby.isStarting) {
            try {
                gameService.removeGame(lobbyId)
            } catch (_: Exception) {}
            return@requireInternet
        }

        lobbyService.updateLobby(lobbyId, mapOf("gameStarted" to true))
        lobbyService.removeLobby(lobbyId)
    }

    private suspend fun sendMessagesDuringCountdown(): Boolean {
        lobbyService.sendMessage(lobbyId, "System", "The game will start in...")
        delay(500)
        for (i in 3 downTo 1) {
            val currentLobby = lobbyService.getLobby(lobbyId)
            if (currentLobby == null || !currentLobby.isStarting) {
                return false
            }
            lobbyService.sendMessage(lobbyId, "System", i.toString())
            delay(1000)
        }

        val finalLobby = lobbyService.getLobby(lobbyId)
        if (finalLobby == null || !finalLobby.isStarting) { return false }

        lobbyService.sendMessage(lobbyId, "System", "Starting game!")
        delay(500)
        return true
    }

    fun onSendMessage(player: String, message: String) = requireInternet {
        lobbyService.sendMessage(lobbyId, player, message)
    }

    fun onAbandonLobby() = requireInternet {
        val playerName = profileService.getUsername()
        val lobby = lobbyService.getLobby(lobbyId)
        val wasStarting = lobby?.isStarting == true

        lobbyService.abandonLobby(lobbyId, playerName)

        if (wasStarting) {
            try {
                gameService.removeGame(lobbyId)
            } catch (_: Exception) { }

            try {
                val currentLobby = lobbyService.getLobby(lobbyId)
                if (currentLobby != null && !currentLobby.playerList.isEmpty()) {
                    lobbyService.updateLobby(lobbyId, mapOf("isStarting" to false))
                    lobbyService.sendMessage(lobbyId, "System", "Game start cancelled - a player left the lobby.")
                }
            } catch (_: Exception) { }
        } else {
            try {
                gameService.observeGame(lobbyId).collect { game ->
                    if (game.id.isNotEmpty() && game.players.any { it.name == playerName }) {
                        val updatedPlayers = game.players.filter { it.name != playerName }.toMutableList()

                        if (updatedPlayers.isEmpty()) {
                            gameService.removeGame(game.id)
                        } else {
                            val updatedGame = game.copy(players = updatedPlayers)
                            gameService.updateGameState(updatedGame)
                        }
                    }
                    return@collect
                }
            } catch (_: Exception) {}
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch { errorMessage.emit("") }
    }

    suspend fun isHost(): Boolean {
        return profileService.getUsername() == lobbyService.getLobby(lobbyId)?.host
    }

    fun getLobbyId(): String = lobbyId
    fun getPlayerName(): String = profileService.getUsername()

    fun tryMarkNavigated(): Boolean {
        return if (!_hasNavigated.value) {
            _hasNavigated.value = true
            true
        } else false
    }
}
