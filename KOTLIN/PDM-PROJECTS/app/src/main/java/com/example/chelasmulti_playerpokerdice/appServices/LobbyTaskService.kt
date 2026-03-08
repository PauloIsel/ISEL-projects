package com.example.chelasmulti_playerpokerdice.appServices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.chelasmulti_playerpokerdice.ChelasPokerDiceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LobbyTaskService : Service() {
    companion object {
        const val ACTION_REMOVE_LOBBY = "REMOVE_LOBBY"
        const val EXTRA_LOBBY_ID = "lobbyId"
    }

    private var lobbyId: String? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lobbyId = intent?.getStringExtra(EXTRA_LOBBY_ID) ?: lobbyId

        when (intent?.action) {
            ACTION_REMOVE_LOBBY -> {
                val idToUse = intent.getStringExtra(EXTRA_LOBBY_ID) ?: lobbyId
                if (idToUse != null) {
                    scope.launch {
                        try {
                            val app = application as ChelasPokerDiceApplication
                            val lobbyService = app.lobbyService
                            lobbyService.removeLobby(idToUse)
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
        val idToUse = rootIntent?.getStringExtra(EXTRA_LOBBY_ID) ?: lobbyId
        scope.launch {
            try {
                val app = application as ChelasPokerDiceApplication
                val lobbyService = app.lobbyService
                val profileService = app.profileService

                if (idToUse != null) {
                    val playerName = profileService.getUsername()
                    try {
                        removePlayerFromGame(idToUse, playerName)
                    } catch (_: Exception) { }
                    try {
                        lobbyService.abandonLobby(idToUse, playerName)
                    } catch (_: Exception) {}
                }
            } catch (_: Throwable) {}
            finally {
                stopSelf()
            }
        }
    }

    private suspend fun removePlayerFromGame(gameId: String, playerName: String): Boolean {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val gameRef = db.collection("game_board").document(gameId)

        return try {
            val result = db.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)
                if (!snapshot.exists()) return@runTransaction false

                val rawPlayers = snapshot.get("players") as? List<*> ?: emptyList<Any>()
                val playerMaps = rawPlayers.filterIsInstance<Map<*, *>>().toMutableList()

                val playerExists = playerMaps.any { (it["name"] as? String) == playerName }
                if (!playerExists) return@runTransaction false

                val newPlayers = playerMaps.filter { (it["name"] as? String) != playerName }

                when (newPlayers.size) {
                    0 -> {
                        // Last player - delete game
                        transaction.delete(gameRef)
                    }
                    1 -> {
                        // One player left - mark as winner
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

                true
            }.await()
            result ?: false
        } catch (_: Exception) { false }
    }


    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}