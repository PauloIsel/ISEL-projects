package com.example.chelasmulti_playerpokerdice.testutils

import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLobbyService : LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> = flow {
        delay(100)
        emit(listOf(
            Lobby(
                id = "lobby1",
                name = "Test Lobby",
                description = "Test Description",
                size = 4,
                host = "host1",
                isHost = false,
                rounds = 3,
                playerList = mutableListOf("player1", "player2"),
                messageList = mutableListOf(),
                gameStarted = false
            )
        ))
    }

    override suspend fun getLobby(lobbyId: String): Lobby {
        return Lobby(
            id = lobbyId,
            name = "Test Lobby",
            description = "Test",
            size = 4,
            host = "host1",
            isHost = false,
            rounds = 3,
            playerList = mutableListOf("player1", "player2"),
            messageList = mutableListOf(),
            gameStarted = false
        )
    }

    override fun observeLobby(lobbyId: String): Flow<Lobby> = flow {
        delay(100)
        emit(getLobby(lobbyId))
    }

    override suspend fun removeLobby(lobbyId: String) {}

    override suspend fun changeLobbyHost(lobbyId: String): String = "host2"

    override suspend fun sendMessage(lobbyId: String, player: String, message: String) {}

    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby {
        return getLobby("created_lobby")
    }

    override suspend fun joinLobby(lobbyId: String, player: String) {
        delay(50)
    }

    override suspend fun abandonLobby(lobbyId: String, player: String) {}

    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }
}

class EmptyFakeLobbyService : LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> = flow {
        delay(50)
        emit(emptyList())
    }

    override suspend fun getLobby(lobbyId: String): Lobby {
        return Lobby(
            id = lobbyId,
            name = "",
            description = "",
            size = 0,
            host = "",
            isHost = false,
            rounds = 0,
            playerList = mutableListOf(),
            messageList = mutableListOf(),
            gameStarted = false
        )
    }

    override fun observeLobby(lobbyId: String): Flow<Lobby> = flow { emit(getLobby(lobbyId)) }
    override suspend fun removeLobby(lobbyId: String) {}
    override suspend fun changeLobbyHost(lobbyId: String): String = ""
    override suspend fun sendMessage(lobbyId: String, player: String, message: String) {}
    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby = getLobby("created")
    override suspend fun joinLobby(lobbyId: String, player: String) {}
    override suspend fun abandonLobby(lobbyId: String, player: String) {}
    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }
}

class FailingFakeLobbyService : LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> = flow {
        delay(50)
        throw Exception("Failed to fetch lobbies")
    }

    override suspend fun getLobby(lobbyId: String): Lobby { throw Exception("No lobby") }
    override fun observeLobby(lobbyId: String): Flow<Lobby> = flow { throw Exception("No lobby") }
    override suspend fun removeLobby(lobbyId: String) {}
    override suspend fun changeLobbyHost(lobbyId: String): String { throw Exception("Fail") }
    override suspend fun sendMessage(lobbyId: String, player: String, message: String) { throw Exception("Fail") }
    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby { throw Exception("Fail") }
    override suspend fun joinLobby(lobbyId: String, player: String) { throw Exception("Fail") }
    override suspend fun abandonLobby(lobbyId: String, player: String) { throw Exception("Fail") }
    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }
}

class GameStartingFakeLobbyService : LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> = flow { emit(emptyList()) }
    override suspend fun getLobby(lobbyId: String): Lobby = FakeLobbyService().getLobby(lobbyId)
    override fun observeLobby(lobbyId: String): Flow<Lobby> = flow {
        val lobby = FakeLobbyService().getLobby(lobbyId)
        emit(lobby.copy(gameStarted = true))
    }
    override suspend fun removeLobby(lobbyId: String) {}
    override suspend fun changeLobbyHost(lobbyId: String): String = ""
    override suspend fun sendMessage(lobbyId: String, player: String, message: String) {}
    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby = FakeLobbyService().getLobby("created_lobby")
    override suspend fun joinLobby(lobbyId: String, player: String) {}
    override suspend fun abandonLobby(lobbyId: String, player: String) {}
    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }
}

