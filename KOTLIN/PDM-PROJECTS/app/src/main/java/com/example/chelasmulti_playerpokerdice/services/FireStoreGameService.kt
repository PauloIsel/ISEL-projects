package com.example.chelasmulti_playerpokerdice.services

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val GAME_BOARD_COLLECTION = "game_board"

class FireStoreGameService(private val firestore: FirebaseFirestore): GameService {
    override fun observeGame(gameId: String): Flow<Game> {
        return firestore.collection(GAME_BOARD_COLLECTION)
            .document(gameId)
            .snapshots()
            .map { doc ->
                doc.toGame()
            }
    }

    override suspend fun initializeGame(lobby: Lobby): String {
        val docRef = firestore.collection(GAME_BOARD_COLLECTION).document(lobby.id)
        val currentTime = System.currentTimeMillis()
        val players = lobby.playerList.map { playerName ->
            Player(
                name = playerName,
                isConnected = true,
                lastSeen = currentTime
            )
        }.toMutableList()

        val game = Game(
            id = docRef.id,
            players = players,
            startingPlayers = players.map { it.copy() }.toMutableList(),
            rounds = Round(lobby.rounds)
        )

        updateGameState(game)
        return game.id
    }

    override suspend fun updateGameState(game: Game) {
        val ref = firestore.collection(GAME_BOARD_COLLECTION).document(game.id)

        val updateMap = mapOf(
            "players" to game.players.map { it.toMap() },
            "rounds" to game.rounds,
            "selectedDices" to game.selectedDice,
            "lastRoundWinner" to game.lastRoundWinner,
            "isTieBreaker" to game.isTieBreaker
        )

        ref.update(updateMap).await()
    }

    override suspend fun updatePlayerHeartbeat(gameId: String, playerName: String) {
        val gameRef = firestore.collection(GAME_BOARD_COLLECTION).document(gameId)
        try {
            firestore.runTransaction { tx ->
                val snap = tx.get(gameRef)
                if (!snap.exists()) return@runTransaction null

                @Suppress("UNCHECKED_CAST")
                val players = (snap.get("players") as? List<Map<String, Any>>)?.toMutableList()
                    ?: mutableListOf()

                val now = System.currentTimeMillis()
                var found = false

                val updatedPlayers = players.map { p ->
                    if ((p["name"] as? String) == playerName) {
                        found = true
                        p.toMutableMap().apply {
                            put("lastSeen", now)
                            put("isConnected", true)
                        }
                    } else {
                        p.toMutableMap()
                    }
                }.toMutableList()

                if (!found) {
                    Log.d("FireStoreGameService", "Heartbeat: player $playerName not found in game $gameId; skipping add")
                }

                tx.update(gameRef, "players", updatedPlayers)
                null
            }.await()
        } catch (e: Exception) {
            Log.e("FireStoreGameService", "Failed to update heartbeat: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removePlayerFromGameTransaction(gameId: String, playerName: String) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("game_board").document(gameId)

        try {
            db.runTransaction { tx ->
                val snap = tx.get(gameRef)

                @Suppress("UNCHECKED_CAST")
                val players = (snap.get("players") as? List<Map<String, Any>>)?.toMutableList()
                    ?: mutableListOf()

                val updated = players.filter { it["name"] != playerName }

                if (updated.isEmpty()) {
                    tx.delete(gameRef)
                } else {
                    tx.update(gameRef, "players", updated)
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FireStoreGameService", "Failed to remove player: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeGame(gameId: String) {
        firestore.collection(GAME_BOARD_COLLECTION)
            .document(gameId)
            .delete()
            .await()
    }

    override suspend fun createGameFromLobbyTransaction(lobbyId: String): String? {
        return try {
            firestore.runTransaction { transaction ->
                val lobbyRef = firestore.collection("lobby_board").document(lobbyId)
                val lobbySnapshot = transaction.get(lobbyRef)

                if (!lobbySnapshot.exists()) {
                    throw IllegalStateException("Lobby does not exist")
                }

                val gameStarted = lobbySnapshot.getBoolean("gameStarted") ?: false
                if (gameStarted) {
                    throw IllegalStateException("Game already started")
                }

                val gameRef = firestore.collection(GAME_BOARD_COLLECTION).document(lobbyId)
                val existingGame = transaction.get(gameRef)
                if (existingGame.exists()) {
                    throw IllegalStateException("Game already exists")
                }

                val lobby = lobbySnapshot.toLobby()

                if (lobby.playerList.isEmpty()) {
                    throw IllegalStateException("No players in lobby")
                }

                val currentTime = System.currentTimeMillis()
                val players = lobby.playerList.map { playerName ->
                    mapOf(
                        "name" to playerName,
                        "dice" to List(5) { mapOf("faceValue" to 0) },
                        "currentHandScore" to 0,
                        "roundWins" to 0,
                        "isConnected" to true,
                        "lastSeen" to currentTime
                    )
                }

                val roundsNum = if (lobby.rounds > 0) lobby.rounds else 3
                val gameData = mapOf(
                    "players" to players,
                    "startingPlayers" to players.map {
                        (it as Map<*, *>).toMutableMap().apply {
                            remove("dice"); remove("currentHandScore")
                        }
                    },
                    "rounds" to mapOf(
                        "numberOfRounds" to roundsNum,
                        "roundNumber" to 1,
                        "currentPlayerIndex" to 0,
                        "rollsLeft" to 3,
                        "playersPlayedThisRound" to 0
                    ),
                    "selectedDices" to emptyList<Boolean>(),
                    "lastRoundWinner" to null,
                    "isTieBreaker" to false
                )

                transaction.set(gameRef, gameData)
                transaction.update(lobbyRef, mapOf("isStarting" to true))

                lobbyId
            }.await()
        } catch (_: Exception) { null }
    }
}

private fun Player.toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "dice" to dice.map { mapOf("faceValue" to it.faceValue) },
            "currentHandScore" to currentHandScore,
            "roundWins" to roundWins,
            "isConnected" to isConnected,
            "lastSeen" to lastSeen
        )
    }

    private fun DocumentSnapshot.toGame(): Game {
        fun Any?.toIntOrZero(): Int = (this as? Long)?.toInt() ?: (this as? Int) ?: 0

        fun mapToPlayerList(playerData: Any?): MutableList<Player> {
            val playerList = (playerData as? List<*>) ?: return mutableListOf()

            return playerList.mapNotNull { item ->
                val playerMap = item as? Map<*, *> ?: return@mapNotNull null

                val name = playerMap["name"] as? String ?: "Unknown Player"
                val currentHandScore = playerMap["currentHandScore"].toIntOrZero()
                val roundWins = playerMap["roundWins"].toIntOrZero()
                val isConnected = playerMap["isConnected"] as? Boolean ?: true
                val lastSeen = playerMap["lastSeen"] as? Long ?: System.currentTimeMillis()
                val diceData = playerMap["dice"] as? List<*> ?: emptyList<Any>()

                val diceList: MutableList<Dice> = diceData.mapNotNull { dieItem ->
                    val dieMap = dieItem as? Map<*, *> ?: return@mapNotNull null
                    val faceValue = dieMap["faceValue"].toIntOrZero()
                    Dice(faceValue)
                }.toMutableList()

                while (diceList.size < 5) {
                    diceList.add(Dice())
                }

                Player(
                    name = name,
                    dice = diceList,
                    currentHandScore = currentHandScore,
                    roundWins = roundWins,
                    isConnected = isConnected,
                    lastSeen = lastSeen
                )
            }.toMutableList()
        }

        val roundsMap = get("rounds") as? Map<*, *>
        val rounds = if (roundsMap != null) {
            Round(
                numberOfRounds = roundsMap["numberOfRounds"].toIntOrZero(),
                roundNumber = roundsMap["roundNumber"].toIntOrZero(),
                currentPlayerIndex = roundsMap["currentPlayerIndex"].toIntOrZero(),
                rollsLeft = roundsMap["rollsLeft"].toIntOrZero(),
                playersPlayedThisRound = roundsMap["playersPlayedThisRound"].toIntOrZero()
            )
        } else {
            Round(numberOfRounds = 0)
        }
        val selectedDices = get("selectedDices") as? List<*> ?: emptyList<Any>()

        val lastRoundWinnerMap = get("lastRoundWinner") as? Map<*, *>
        val lastRoundWinner = lastRoundWinnerMap?.let { mapToPlayerList(listOf(it)).firstOrNull() }

        return Game(
            id = this.id,
            players = mapToPlayerList(get("players")),
            startingPlayers = mapToPlayerList(get("startingPlayers")),
            rounds = rounds,
            selectedDice = selectedDices.map { it == true },
            lastRoundWinner = lastRoundWinner,
            isTieBreaker = getBoolean("isTieBreaker") ?: false
        )
    }