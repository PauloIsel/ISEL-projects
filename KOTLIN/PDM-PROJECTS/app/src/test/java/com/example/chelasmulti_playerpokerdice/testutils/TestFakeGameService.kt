package com.example.chelasmulti_playerpokerdice.testutils

import com.example.chelasmulti_playerpokerdice.services.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Shared FakeGameService for tests. Keeps behavior similar to the per-test fakes.
 */
class FakeGameService : GameService {
    private val gameFlow = MutableStateFlow(
        Game(
            id = "test-lobby-id",
            players = mutableListOf(
                Player("player1", MutableList(5) { Dice(1) }, 0, 0),
                Player("player2", MutableList(5) { Dice(2) }, 0, 0)
            ),
            startingPlayers = mutableListOf(
                Player("player1", MutableList(5) { Dice(1) }, 0, 0),
                Player("player2", MutableList(5) { Dice(2) }, 0, 0)
            ),
            rounds = Round(
                numberOfRounds = 4,
                roundNumber = 1,
                currentPlayerIndex = 0,
                rollsLeft = 3,
                playersPlayedThisRound = 0
            ),
            isTieBreaker = false,
            lastRoundWinner = null
        )
    )

    override fun observeGame(gameId: String): Flow<Game> = gameFlow

    override suspend fun initializeGame(lobby: Lobby): String = "test-lobby-id"

    override suspend fun updateGameState(game: Game) {
        gameFlow.emit(game)
    }

    override suspend fun removeGame(gameId: String) {
        // no-op for tests
    }

    override suspend fun createGameFromLobbyTransaction(lobbyId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerHeartbeat(gameId: String, playerName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePlayerFromGameTransaction(
        gameId: String,
        playerName: String
    ) {
        TODO("Not yet implemented")
    }
}

class DelayedFakeGameService : GameService {
    private val gameFlow = MutableStateFlow<Game?>(null)

    override fun observeGame(gameId: String): Flow<Game> = gameFlow.filterNotNull()

    override suspend fun initializeGame(lobby: Lobby): String {
        delay(200)
        val game = Game(
            id = "test-lobby-id",
            players = mutableListOf(
                Player("player1", MutableList(5) { Dice(1) }, 0, 0),
                Player("player2", MutableList(5) { Dice(2) }, 0, 0)
            ),
            startingPlayers = mutableListOf(
                Player("player1", MutableList(5) { Dice(1) }, 0, 0),
                Player("player2", MutableList(5) { Dice(2) }, 0, 0)
            ),
            rounds = Round(
                numberOfRounds = 4,
                roundNumber = 1,
                currentPlayerIndex = 0,
                rollsLeft = 3,
                playersPlayedThisRound = 0
            ),
            isTieBreaker = false,
            lastRoundWinner = null
        )
        gameFlow.emit(game)
        return "test-lobby-id"
    }

    override suspend fun updateGameState(game: Game) {
        gameFlow.emit(game)
    }

    override suspend fun removeGame(gameId: String) {}

    override suspend fun createGameFromLobbyTransaction(lobbyId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerHeartbeat(gameId: String, playerName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePlayerFromGameTransaction(
        gameId: String,
        playerName: String
    ) {
        TODO("Not yet implemented")
    }
}

class GameOverFakeGameService : GameService {
    private val gameFlow = MutableStateFlow(
        Game(
            id = "test-lobby-id",
            players = mutableListOf(
                Player("player1", MutableList(5) { Dice(6) }, 50, 3),
                Player("player2", MutableList(5) { Dice(2) }, 40, 1)
            ),
            startingPlayers = mutableListOf(
                Player("player1", MutableList(5) { Dice(6) }, 50, 3),
                Player("player2", MutableList(5) { Dice(2) }, 40, 1)
            ),
            rounds = Round(
                numberOfRounds = 2,
                roundNumber = 2,
                currentPlayerIndex = 1,
                rollsLeft = 0,
                playersPlayedThisRound = 1
            ),
            isTieBreaker = false,
            lastRoundWinner = null
        )
    )

    override fun observeGame(gameId: String): Flow<Game> = gameFlow

    override suspend fun initializeGame(lobby: Lobby): String = "test-lobby-id"

    override suspend fun updateGameState(game: Game) {
        gameFlow.emit(game)
    }

    override suspend fun removeGame(gameId: String) {}

    override suspend fun createGameFromLobbyTransaction(lobbyId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerHeartbeat(gameId: String, playerName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePlayerFromGameTransaction(
        gameId: String,
        playerName: String
    ) {
        TODO("Not yet implemented")
    }
}
