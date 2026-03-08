package com.example.chelasmulti_playerpokerdice.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class FakeLobbyService : LobbyService {
    private val lobbies = mutableListOf<Lobby>()
    private val lobbyFlows = mutableMapOf<String, MutableStateFlow<Lobby>>()

    init {
        generateLobbies(5)
    }

    override fun getLobbies(): Flow<List<Lobby>> = flow {
        delay(500)
        emit(lobbies)
    }

    override suspend fun getLobby(lobbyId: String): Lobby {
        return lobbies.find { it.id == lobbyId } ?: throw IllegalStateException("Lobby not found")
    }

    override fun observeLobby(lobbyId: String): Flow<Lobby> {
        return lobbyFlows.getOrPut(lobbyId) {
            MutableStateFlow(
                lobbies.find { it.id == lobbyId } ?:
                throw IllegalStateException("Lobby not found")
            )
        }
    }

    private fun updateLobbyFlow(lobbyId: String, lobby: Lobby) {
        lobbyFlows[lobbyId]?.value = lobby
    }

    override suspend fun createLobby(
        name: String,
        description: String,
        size: Int,
        rounds: Int,
        owner: String
    ): Lobby {
        delay(200)

        val lobbyExists = lobbies.any { it.name == name }
        if (lobbyExists) {
            throw IllegalStateException("Já existe um lobby com o nome '$name'")
        }

        val fakePlayers = listOf("Alice", "Bob", "Charlie", "Diana", "Eve").shuffled().take(size-1)
        val players = mutableListOf(owner)
        players.addAll(fakePlayers)

        val initialMessages = mutableListOf<ChatMessage>()
        for (player in players) {
            if(player == owner) {
                initialMessages.add(ChatMessage("System", "Lobby started by $player."))
            } else initialMessages.add(ChatMessage("System", "$player has joined the lobby."))
        }

        val lobby = Lobby(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            size = size,
            host = owner,
            rounds = rounds,
            playerList = players,
            isHost = true,
            messageList = initialMessages
        )

        lobbies.add(lobby)
        return lobby
    }

    override suspend fun joinLobby(lobbyId: String, player: String) {
        delay(200)

        val lobby = getLobby(lobbyId)

        if (lobby.playerList.size >= lobby.size) { throw IllegalStateException("Lobby is full") }
        if (!lobby.playerList.contains(player)) {
            val newPlayerList = lobby.playerList.toMutableList()
            newPlayerList.add(player)

            val updatedLobby = lobby.copy(playerList = newPlayerList)
            val index = lobbies.indexOf(lobby)
            lobbies[index] = updatedLobby
            updateLobbyFlow(lobbyId, updatedLobby)
            sendMessage(lobbyId, "System", "$player has joined the lobby.")
        } else {
            throw IllegalStateException("Player already in lobby")
        }
    }

    override suspend fun abandonLobby(lobbyId: String, player: String) {
        delay(200)

        val lobby = lobbies.find { it.id == lobbyId }
        lobby?.let {
            if (it.playerList.contains(player)) {
                val newPlayerList = it.playerList.toMutableList()
                newPlayerList.remove(player)

                if (newPlayerList.isEmpty()) {
                    removeLobby(lobbyId)
                } else {
                    val updatedLobby = it.copy(playerList = newPlayerList)

                    val index = lobbies.indexOf(it)
                    lobbies[index] = updatedLobby
                    updateLobbyFlow(lobbyId, updatedLobby)

                    if(it.host == player) {
                        changeLobbyHost(lobbyId)
                        sendMessage(lobbyId, "System", "The host has left the game. ${getLobby(lobbyId).host} is now the new host.")
                    } else {
                        sendMessage(lobbyId, "System", "$player has left the lobby.")
                    }
                }
            } else {
                throw IllegalStateException("Player not in lobby")
            }
        }
    }

    override suspend fun changeLobbyHost(lobbyId: String): String? {
        val lobby = getLobby(lobbyId)
        val newHost = lobby.playerList.find { it != getLobby(lobbyId).host }
        newHost?.let {
            val updatedLobby = lobby.copy(host = it)
            val index = lobbies.indexOf(lobby)
            lobbies[index] = updatedLobby
            updateLobbyFlow(lobby.id, updatedLobby)
        }
        return newHost
    }

    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        val lobby = lobbies.find { it.id == lobbyId } ?: return
        var updatedLobby = lobby

        updates.forEach { (key, value) ->
            updatedLobby = when (key) {
                "isStarting" -> updatedLobby.copy(isStarting = value as Boolean)
                "gameStarted" -> updatedLobby.copy(gameStarted = value as Boolean)
                else -> updatedLobby
            }
        }

        val index = lobbies.indexOf(lobby)
        if (index != -1) {
            lobbies[index] = updatedLobby
            updateLobbyFlow(lobbyId, updatedLobby)
        }
    }

    override suspend fun removeLobby(lobbyId: String) {
        val lobby = lobbies.find { it.id == lobbyId }
        lobby?.let {
            lobbies.remove(it)
        }
    }

    fun generateLobbies(amount: Int, isHost: Boolean = false, customHost: String? = null): List<Lobby> {
        val currentSize = lobbies.size
        val generated = List(amount) { index ->
            val lobbyId = currentSize + index
            val size = (3..6).random()
            val playerCount = (1..<size-1).random()
            val host = customHost ?: "Host$lobbyId"
            val rounds = size * (1..5).random()
            val players = mutableListOf(host)
            players.addAll(MutableList(playerCount) { "Player${it + 1}" })

            val initialMessages = mutableListOf<ChatMessage>()
            for (player in players) {
                if(player == host) {
                    initialMessages.add(ChatMessage("System", "Lobby started by $player."))
                } else initialMessages.add(ChatMessage("System", "$player has joined the lobby."))
            }

            Lobby(
                id = lobbyId.toString(),
                name = "Lobby$lobbyId - The Ultimate Poker Dice Championship",
                description = "This is lobby number $lobbyId, where players gather to compete in the thrilling game of Poker Dice. Join us for an exciting experience filled with strategy, luck, and fun!",
                size = size,
                host = host,
                rounds = rounds,
                playerList = players,
                isHost = isHost,
                messageList = initialMessages
            )
        }

        lobbies.addAll(generated)
        return generated
    }

    override suspend fun sendMessage(lobbyId: String, player: String, message: String) {
        val lobby = getLobby(lobbyId)
        lobby.let {
            val newMessageList = it.messageList.toMutableList()
            newMessageList.add(ChatMessage(player, message))

            val updatedLobby = it.copy(messageList = newMessageList)
            val index = lobbies.indexOf(it)
            lobbies[index] = updatedLobby
            updateLobbyFlow(lobbyId, updatedLobby)
        }
    }
}