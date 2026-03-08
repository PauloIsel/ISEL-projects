package com.example.chelasmulti_playerpokerdice.services

import kotlinx.coroutines.flow.Flow

data class Lobby(
    val id: String,
    val name: String,
    val description: String,
    val size: Int,
    val host: String,
    val isHost: Boolean,
    val rounds: Int,
    val playerList: MutableList<String>,
    val messageList: MutableList<ChatMessage>,
    val gameStarted: Boolean = false,
    val isStarting: Boolean = false
)

data class ChatMessage(
    val player: String,
    val message: String
)

interface LobbyService {
    fun getLobbies(): Flow<List<Lobby>>
    suspend fun getLobby(lobbyId: String): Lobby?
    fun observeLobby(lobbyId: String): Flow<Lobby>
    suspend fun removeLobby(lobbyId: String)
    suspend fun changeLobbyHost(lobbyId: String): String?
    suspend fun sendMessage(lobbyId: String, player: String, message: String)
    suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby
    suspend fun joinLobby(lobbyId: String, player: String)
    suspend fun abandonLobby(lobbyId: String, player: String)
    suspend fun updateLobby(lobbyId: String, updates: Map<String, Any>)
}