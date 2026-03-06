package com.example.chelasmulti_playerpokerdice.lobbyCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface LobbyCreationScreenState {
    data class Data(
        val lobbyName: String = "",
        val description: String = "",
        val players: Int = 2,
        val rounds: Int = 2,
        val isLobbyNameValid: Boolean = false,
        val isDescriptionValid: Boolean = false,
        val isRoundsValid: Boolean = true,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : LobbyCreationScreenState

    object NoNetwork : LobbyCreationScreenState
    data class Success(val lobby: Lobby) : LobbyCreationScreenState
    data class Error(val message: String) : LobbyCreationScreenState
}

class LobbyCreationViewModel(
    private val lobbyService: LobbyService,
    private val profileService: IProfileService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val currentState = MutableStateFlow<LobbyCreationScreenState>(LobbyCreationScreenState.Data())
    val currentStateFlow: StateFlow<LobbyCreationScreenState> = currentState

    private val snackBarMessage = MutableStateFlow("")
    val snackBarMessageFlow: StateFlow<String> = snackBarMessage

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    val maxLobbyNameChars = 52
    val maxDescriptionChars = 200

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.observeNetworkConnectivity()
                .collectLatest { isAvailable ->
                    _isNetworkAvailable.value = isAvailable
                    if (!isAvailable) {
                        currentState.value = LobbyCreationScreenState.NoNetwork
                    } else {
                        val state = currentState.value
                        if (state !is LobbyCreationScreenState.Data) {
                            currentState.value = LobbyCreationScreenState.Data()
                        }
                    }
                }
        }
    }

    private fun requireInternet(action: suspend () -> Unit) {
        if (!_isNetworkAvailable.value) {
            viewModelScope.launch {
                currentState.value = LobbyCreationScreenState.NoNetwork
                snackBarMessage.emit("No internet connection")
            }
        } else {
            viewModelScope.launch { action() }
        }
    }

    fun onLobbyNameChange(newName: String) {
        val state = currentState.value
        if (state is LobbyCreationScreenState.Data) {
            if (newName.length <= maxLobbyNameChars) {
                currentState.value = state.copy(
                    lobbyName = newName,
                    isLobbyNameValid = newName.isNotBlank()
                )
            } else {
                viewModelScope.launch { snackBarMessage.emit("O nome do lobby não pode exceder o limite de caracteres.") }
            }
        }
    }

    fun onDescriptionChange(newDesc: String) {
        val state = currentState.value
        if (state is LobbyCreationScreenState.Data) {
            if (newDesc.length <= maxDescriptionChars) {
                currentState.value = state.copy(
                    description = newDesc,
                    isDescriptionValid = newDesc.isNotBlank()
                )
            } else {
                viewModelScope.launch { snackBarMessage.emit("A descrição não pode exceder o limite de caracteres.") }
            }
        }
    }

    fun onPlayersChange(newPlayers: Int) {
        val state = currentState.value
        if (state is LobbyCreationScreenState.Data) {
            val newRounds = adjustRounds(state.rounds, newPlayers)
            currentState.value = state.copy(
                players = newPlayers,
                rounds = newRounds,
                isRoundsValid = isRoundsValid(newRounds, newPlayers)
            )
        }
    }

    fun onRoundsChange(newRounds: Int) {
        val state = currentState.value
        if (state is LobbyCreationScreenState.Data) {
            currentState.value = state.copy(
                rounds = newRounds,
                isRoundsValid = isRoundsValid(newRounds, state.players)
            )
        }
    }

    fun onCreateLobby() = requireInternet {
        val state = currentState.value as? LobbyCreationScreenState.Data ?: return@requireInternet
        currentState.value = state.copy(isLoading = true, errorMessage = null)

        try {
            val lobby = lobbyService.createLobby(
                name = state.lobbyName,
                description = state.description,
                size = state.players,
                rounds = state.rounds,
                owner = profileService.getUsername()
            )
            currentState.value = LobbyCreationScreenState.Success(lobby)
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Erro desconhecido"
            currentState.value = state.copy(isLoading = false, errorMessage = errorMessage)
            viewModelScope.launch {
                snackBarMessage.emit(errorMessage)
            }
        }
    }

    fun clearSnackBarMessage() {
        viewModelScope.launch { snackBarMessage.emit("") }
    }

    private fun isRoundsValid(rounds: Int, players: Int) = rounds % players == 0 && rounds <= 60
    private fun adjustRounds(rounds: Int, players: Int) = if (rounds % players != 0) players else rounds
}
