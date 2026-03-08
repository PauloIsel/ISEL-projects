package com.example.chelasmulti_playerpokerdice.appServices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GameTaskService : Service() {
    companion object {
        const val ACTION_CLEANUP_PLAYER = "CLEANUP_PLAYER"
        const val EXTRA_GAME_ID = "gameId"
        const val EXTRA_PLAYER_NAME = "playerName"
    }

    private var gameId: String? = null
    private var playerName: String? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        gameId = intent?.getStringExtra(EXTRA_GAME_ID) ?: gameId
        playerName = intent?.getStringExtra(EXTRA_PLAYER_NAME) ?: playerName

        when (intent?.action) {
            ACTION_CLEANUP_PLAYER -> {
                val idToUse = intent.getStringExtra(EXTRA_GAME_ID) ?: gameId
                val nameToUse = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: playerName

                if (idToUse != null && nameToUse != null) {
                    scope.launch {
                        try {
                            removePlayerFromGame(idToUse, nameToUse)
                        } catch (_: Throwable) {
                        } finally {
                            stopSelf()
                        }
                    }
                } else {
                    stopSelf()
                }
            }
            else -> {}
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val idToUse = rootIntent?.getStringExtra(EXTRA_GAME_ID) ?: gameId
        val nameToUse = rootIntent?.getStringExtra(EXTRA_PLAYER_NAME) ?: playerName

        scope.launch {
            try {
                if (idToUse != null && nameToUse != null) {
                    removePlayerFromGame(idToUse, nameToUse)
                }
            } catch (_: Throwable) {
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun removePlayerFromGame(gameId: String, playerName: String) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("game_board").document(gameId)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)
                if (!snapshot.exists()) { return@runTransaction null }

                val rawPlayers = snapshot.get("players") as? List<*> ?: emptyList<Any>()
                val playerMaps = rawPlayers.filterIsInstance<Map<*, *>>().toMutableList()

                // Remove any players matching playerName
                val newPlayers = playerMaps.filter { (it["name"] as? String) != playerName }

                if (newPlayers.size == playerMaps.size) {
                    android.util.Log.w("GameTaskService", "Player $playerName not found in game")
                    return@runTransaction null
                }
                when (newPlayers.size) {
                    0 -> {
                        transaction.delete(gameRef)
                    }
                    1 -> {
                        val remainingPlayer = newPlayers.first()
                        transaction.update(gameRef, mapOf(
                            "players" to newPlayers,
                            "rounds" to mapOf(
                                "numberOfRounds" to -1,
                                "roundNumber" to 1,
                                "currentPlayerIndex" to 0,
                                "rollsLeft" to 0,
                                "playersPlayedThisRound" to 0
                            ),
                            "lastRoundWinner" to remainingPlayer
                        ))
                    }
                    else -> {
                        transaction.update(gameRef, "players", newPlayers)
                    }
                }

                null
            }.await()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
