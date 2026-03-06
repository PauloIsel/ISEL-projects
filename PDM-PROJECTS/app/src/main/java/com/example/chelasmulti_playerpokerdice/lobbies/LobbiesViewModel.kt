package com.example.chelasmulti_playerpokerdice.lobbies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface LobbiesScreenState {
    object Loading : LobbiesScreenState
    data class Data(val lobbies: List<Lobby>) : LobbiesScreenState
    object Empty : LobbiesScreenState
    object NoNetwork : LobbiesScreenState
    data class Error(val message: String) : LobbiesScreenState
}

class LobbiesViewModel(
    private val lobbyService: LobbyService,
    private val profileService: IProfileService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _lobbies = MutableStateFlow<List<Lobby>>(emptyList())
    val lobbies: StateFlow<List<Lobby>> = _lobbies.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val currentState = MutableStateFlow<LobbiesScreenState>(LobbiesScreenState.Loading)
    val currentStateFlow: StateFlow<LobbiesScreenState> get() = currentState.asStateFlow()

    private val snackBarMessage = MutableStateFlow("")
    val snackBarMessageFlow: StateFlow<String> get() = snackBarMessage.asStateFlow()

    private val joiningLobby = MutableStateFlow<String?>(null)
    val joiningLobbyFlow: StateFlow<String?> get() = joiningLobby.asStateFlow()

    private val lobbySession = MutableSharedFlow<String>()
    val lobbySessionFlow: MutableSharedFlow<String> get() = lobbySession

    init {
        observeNetworkAndLobbies()
    }

    private fun observeNetworkAndLobbies() {
        viewModelScope.launch {
            networkMonitor.observeNetworkConnectivity()
                .distinctUntilChanged()
                .collectLatest { isAvailable ->
                    _isNetworkAvailable.value = isAvailable

                    if (!isAvailable) {
                        _lobbies.value = emptyList()
                        currentState.emit(LobbiesScreenState.NoNetwork)
                    } else {
                        currentState.emit(LobbiesScreenState.Loading)
                        try {
                            lobbyService.getLobbies()
                                .collect { lobbies ->
                                    _lobbies.value = lobbies
                                    val state = if (lobbies.isEmpty()) LobbiesScreenState.Empty
                                    else LobbiesScreenState.Data(lobbies)
                                    currentState.emit(state)
                                }
                        } catch (e: Exception) {
                            currentState.emit(LobbiesScreenState.Error(e.message ?: "Unknown error"))
                        }
                    }
                }
        }
    }

    private fun requireInternet(action: suspend () -> Unit) {
        if (!_isNetworkAvailable.value) {
            viewModelScope.launch {
                _lobbies.value = emptyList()
                currentState.emit(LobbiesScreenState.NoNetwork)
                snackBarMessage.emit("No internet connection")
            }
        } else {
            viewModelScope.launch {
                try {
                    action()
                } catch (e: IllegalStateException) {
                    snackBarMessage.emit(e.message ?: "An error occurred")
                    joiningLobby.value = null
                } catch (e: IllegalArgumentException) {
                    snackBarMessage.emit(e.message ?: "An error occurred")
                    joiningLobby.value = null
                } catch (e: Exception) {
                    currentState.emit(LobbiesScreenState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    fun refreshLobbies() = requireInternet {
        currentState.emit(LobbiesScreenState.Loading)
        val lobbies = lobbyService.getLobbies().first()
        _lobbies.value = lobbies
        val state = if (lobbies.isEmpty()) LobbiesScreenState.Empty else LobbiesScreenState.Data(lobbies)
        currentState.emit(state)
    }

    fun onJoinLobby(lobbyId: String) = requireInternet {
        joiningLobby.value = lobbyId
        lobbyService.joinLobby(lobbyId, profileService.getUsername())
        lobbySession.emit(lobbyId)
    }

    fun clearSnackBarMessage() { snackBarMessage.value = "" }
    fun clearJoiningLobby() { joiningLobby.value = null }
}
