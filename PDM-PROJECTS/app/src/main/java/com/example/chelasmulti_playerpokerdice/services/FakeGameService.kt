package com.example.chelasmulti_playerpokerdice.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGameService(private val lobbyService: LobbyService? = null) : GameService {
    private val gameFlow = MutableStateFlow(createInitialFakeGame())

    override fun observeGame(gameId: String): Flow<Game> {
        return gameFlow
    }

    override suspend fun initializeGame(lobby: Lobby): String {
        val players = lobby.playerList.map { playerName -> Player(name = playerName) }.toMutableList()
        val newGame = Game(
            id = lobby.id,
            players = players,
            startingPlayers = players.map { it.copy() }.toMutableList(),
            rounds = Round(lobby.rounds)
        )
        gameFlow.value = newGame
        return newGame.id
    }

    override suspend fun updateGameState(game: Game) {
        gameFlow.value = game
    }

    override suspend fun removeGame(gameId: String) {
        gameFlow.value = createInitialFakeGame()
    }

    override suspend fun createGameFromLobbyTransaction(lobbyId: String): String {
        lobbyService?.updateLobby(lobbyId, mapOf("isStarting" to true))
        return "game_$lobbyId"
    }

    override suspend fun updatePlayerHeartbeat(gameId: String, playerName: String) {}

    override suspend fun removePlayerFromGameTransaction(
        gameId: String,
        playerName: String
    ) {
        val currentGame = gameFlow.value
        val updatedPlayers = currentGame.players.filter { it.name != playerName }.toMutableList()
        gameFlow.value = currentGame.copy(players = updatedPlayers)
    }

    private fun createInitialFakeGame(): Game {
        val players = mutableListOf(Player(name = "Player 1"), Player(name = "Player 2"))
        return Game(
            id = "fake_game_id",
            players = players,
            startingPlayers = players.map { it.copy() }.toMutableList(),
            rounds = Round(numberOfRounds = 3)
        )
    }
}