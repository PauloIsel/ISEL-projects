package com.example.chelasmulti_playerpokerdice.services

import kotlinx.coroutines.flow.Flow
import kotlin.collections.MutableList


data class Dice(
    var faceValue: Int = 0,
)

enum class HandRank(val score: Int) {
    FIVE_OF_A_KIND(50),
    FOUR_OF_A_KIND(40),
    FULL_HOUSE(30),
    STRAIGHT(25),
    THREE_OF_A_KIND(20),
    TWO_PAIR(15),
    PAIR(10),
    BUST(5)
}

enum class TableDesign(val players: List<Int>) {
    TwoPlayers(listOf(2, 5)),
    ThreePlayers(listOf(0, 2, 5)),
    FourPlayers(listOf(0, 2, 3, 5)),
    FivePlayers(listOf(0, 1, 2, 3, 5)),
    SixPlayers(listOf(0, 1, 2, 3, 4, 5))
}

data class Player(
    var name: String,
    var dice: MutableList<Dice> = MutableList(5) { Dice() },
    var currentHandScore: Int = 0,
    var roundWins: Int = 0,
    var isConnected: Boolean = true,                //Para conectividade Depois
    var lastSeen: Long = System.currentTimeMillis()
)

data class Round(
    var numberOfRounds: Int,
    var roundNumber: Int = 1,
    var currentPlayerIndex: Int = 0,
    var rollsLeft: Int = 3,
    var playersPlayedThisRound: Int = 0
)

data class Game(
    val id: String = "",
    var players: MutableList<Player> = mutableListOf(),
    val startingPlayers: MutableList<Player> = mutableListOf(),
    var rounds: Round = Round(0),
    var selectedDice: List<Boolean> = mutableListOf(),
    var lastRoundWinner: Player? = null,
    var isTieBreaker: Boolean = false
)

interface GameService {
    fun observeGame(gameId: String): Flow<Game>
    suspend fun initializeGame(lobby: Lobby): String
    suspend fun updateGameState(game: Game)
    suspend fun removeGame(gameId: String)
    suspend fun createGameFromLobbyTransaction(lobbyId: String): String?
    suspend fun updatePlayerHeartbeat(gameId: String, playerName: String)
    suspend fun removePlayerFromGameTransaction(gameId: String, playerName: String)
}