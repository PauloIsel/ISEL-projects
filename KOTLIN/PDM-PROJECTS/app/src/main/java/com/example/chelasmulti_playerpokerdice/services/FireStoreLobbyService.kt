package com.example.chelasmulti_playerpokerdice.services

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val LOBBY_BOARD_COLLECTION = "lobby_board"

class FireStoreLobbyService(
    private val firestore: FirebaseFirestore
): LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> {
        return firestore.collection(LOBBY_BOARD_COLLECTION)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.toLobby() }
            }
    }

    override suspend fun getLobby(lobbyId: String): Lobby? {
        val doc = firestore.collection(LOBBY_BOARD_COLLECTION)
            .document(lobbyId)
            .get()
            .await()

        return if(doc.exists()) doc.toLobby() else null
    }

    override fun observeLobby(lobbyId: String): Flow<Lobby> {
        return firestore.collection(LOBBY_BOARD_COLLECTION)
            .document(lobbyId)
            .snapshots()
            .map { it.toLobby() }
    }

    private suspend fun publishLobbyUpdate(lobbyId: String, updates: Map<String, Any>) {
        firestore.collection(LOBBY_BOARD_COLLECTION)
            .document(lobbyId)
            .update(updates)
            .await()
    }

    override suspend fun removeLobby(lobbyId: String) {
        firestore.collection(LOBBY_BOARD_COLLECTION)
            .document(lobbyId)
            .delete()
            .await()
    }

    override suspend fun changeLobbyHost(lobbyId: String): String? {
        val lobby = getLobby(lobbyId) ?: return null
        val newHost = lobby.playerList.find { it != lobby.host } ?: return null
        publishLobbyUpdate(lobbyId, mapOf("host" to newHost))
        return newHost
    }

    override suspend fun sendMessage(
        lobbyId: String,
        player: String,
        message: String
    ) {
        val msg = mapOf("player" to player, "message" to message)
        firestore.collection(LOBBY_BOARD_COLLECTION)
            .document(lobbyId)
            .update("messageList", FieldValue.arrayUnion(msg))
            .await()
    }

    override suspend fun createLobby(
        name: String,
        description: String,
        size: Int,
        rounds: Int,
        owner: String
    ): Lobby {
        val existing = firestore.collection(LOBBY_BOARD_COLLECTION)
            .whereEqualTo("name", name)
            .limit(1)
            .get()
            .await()

        if (existing.documents.isNotEmpty()) {
            throw IllegalStateException("Já existe um lobby com o nome '$name'")
        }

        return firestore.runTransaction { transaction ->
            val docRef = firestore.collection(LOBBY_BOARD_COLLECTION).document()
            val initialMessages = mutableListOf<ChatMessage>()

            val players = mutableListOf(owner)
            initialMessages.add(ChatMessage("System", "Lobby created by $owner."))

            val lobby = Lobby(
                id = docRef.id,
                name = name,
                description = description,
                size = size,
                host = owner,
                isHost = true,
                rounds = rounds,
                playerList = players,
                messageList = initialMessages,
                gameStarted = false,
            )

            transaction.set(docRef, lobby.toDocumentContent())

            lobby
        }.await()
    }

    override suspend fun joinLobby(lobbyId: String, player: String) {
        val cancelled = firestore.runTransaction<Boolean> { transaction ->
            val lobbyRef = firestore.collection(LOBBY_BOARD_COLLECTION).document(lobbyId)
            val lobbySnapshot = transaction.get(lobbyRef)

            if (!lobbySnapshot.exists()) {
                throw IllegalStateException("Lobby not found")
            }

            val currentPlayers = when (val rawPlayers = lobbySnapshot.get("playerList")) {
                null -> mutableListOf()
                is List<*> -> rawPlayers.filterIsInstance<String>().toMutableList()
                else -> mutableListOf()
            }

            val lobbySize = lobbySnapshot.getLong("size")?.toInt() ?: 0

            if (currentPlayers.size >= lobbySize) {
                throw IllegalStateException("Lobby is full")
            }

            if (currentPlayers.contains(player)) {
                throw IllegalStateException("Player already in lobby")
            }

            currentPlayers.add(player)

            val now = System.currentTimeMillis()
            val joinMsg = mapOf("player" to "System", "message" to "$player has joined the lobby.", "ts" to now)

            transaction.update(lobbyRef, "playerList", currentPlayers)
            transaction.update(lobbyRef, "messageList", FieldValue.arrayUnion(joinMsg))

            val wasStarting = lobbySnapshot.getBoolean("isStarting") == true
            val gameStarted = lobbySnapshot.getBoolean("gameStarted") == true

            return@runTransaction if (wasStarting && !gameStarted) {
                val gameRef = firestore.collection("game_board").document(lobbyId)
                try { transaction.delete(gameRef) } catch (_: Exception) { }
                transaction.update(lobbyRef, "isStarting", false)
                val cancelMsg = mapOf("player" to "System", "message" to "Game start cancelled - a player joined the lobby.", "ts" to now)
                transaction.update(lobbyRef, "messageList", FieldValue.arrayUnion(cancelMsg))
                true
            } else {
                false
            }
        }.await()

        if (cancelled) {
            val cancelMsg = mapOf("player" to "System", "message" to "Game start cancelled - a player joined the lobby.", "ts" to System.currentTimeMillis())
            firestore.collection(LOBBY_BOARD_COLLECTION).document(lobbyId)
                .update("messageList", FieldValue.arrayUnion(cancelMsg)).await()
        }
    }

    override suspend fun abandonLobby(lobbyId: String, player: String) {
        firestore.runTransaction { transaction ->
            val lobbyRef = firestore.collection(LOBBY_BOARD_COLLECTION).document(lobbyId)
            val lobbySnapshot = transaction.get(lobbyRef)

            if (!lobbySnapshot.exists()) return@runTransaction

            val currentPlayers = when (val rawPlayers = lobbySnapshot.get("playerList")) {
                null -> mutableListOf()
                is List<*> -> rawPlayers.toMutableList()
                else -> mutableListOf()
            }

            if (!currentPlayers.contains(player)) {
                throw IllegalStateException("Player not in lobby")
            }

            val wasStarting = lobbySnapshot.getBoolean("isStarting") == true
            val gameStarted = lobbySnapshot.getBoolean("gameStarted") == true
            val currentHost = lobbySnapshot.getString("host") ?: ""

            if (currentHost == player && wasStarting && !gameStarted) {
                val gameRef = firestore.collection("game_board").document(lobbyId)
                try { transaction.delete(gameRef) } catch (_: Exception) { }
                transaction.update(lobbyRef, "isStarting", false)
            }

            currentPlayers.remove(player)

            if (currentPlayers.isEmpty()) {
                try { transaction.delete(firestore.collection("game_board").document(lobbyId)) } catch (_: Exception) {}
                transaction.delete(lobbyRef)
                return@runTransaction
            } else {
                transaction.update(lobbyRef, "playerList", currentPlayers)

                if (currentHost == player) {
                    val newHost = currentPlayers.firstOrNull()
                    if (newHost != null) {
                        transaction.update(lobbyRef, "host", newHost)
                        val hostMsg = mapOf("player" to "System", "message" to "The host has left the game. $newHost is now the new host.", "ts" to System.currentTimeMillis())
                        transaction.update(lobbyRef, "messageList", FieldValue.arrayUnion(hostMsg))
                    }
                } else {
                    val leaveMsg = mapOf("player" to "System", "message" to "$player has left the lobby.", "ts" to System.currentTimeMillis())
                    transaction.update(lobbyRef, "messageList", FieldValue.arrayUnion(leaveMsg))
                }
             }
         }.await()
     }

    override suspend fun updateLobby(lobbyId: String, updates: Map<String, Any>) {
        publishLobbyUpdate(lobbyId, updates)
    }
}

private fun Lobby.toDocumentContent() = mapOf(
    "name" to name,
    "description" to description,
    "size" to size,
    "host" to host,
    "rounds" to rounds,
    "playerList" to playerList,
    "messageList" to messageList.map {
        mapOf("player" to it.player, "message" to it.message)
    },
    "gameStarted" to gameStarted,
    "isStarting" to isStarting
)

fun DocumentSnapshot.toLobby(): Lobby {
    val playerList: MutableList<String> =
        when (val rawPlayers = get("playerList")) {
            null -> mutableListOf()
            is List<*> -> rawPlayers.filterIsInstance<String>().toMutableList()
            else -> mutableListOf()
        }

    val messageList: MutableList<ChatMessage> =
        when (val rawMessages = get("messageList")) {
            null -> mutableListOf()
            is List<*> -> rawMessages.mapNotNull { item ->
                if (item is Map<*, *>) {
                    val p = item["player"]
                    val m = item["message"]
                    if (p is String && m is String) ChatMessage(p, m) else null
                } else null
            }.toMutableList()
            else -> mutableListOf()
        }

    return Lobby(
        id = id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        size = getLong("size")?.toInt() ?: 0,
        host = getString("host") ?: "",
        isHost = false,
        rounds = getLong("rounds")?.toInt() ?: 0,
        playerList = playerList,
        messageList = messageList,
        gameStarted = getBoolean("gameStarted") ?: false,
        isStarting = getBoolean("isStarting") ?: false
    )
}
