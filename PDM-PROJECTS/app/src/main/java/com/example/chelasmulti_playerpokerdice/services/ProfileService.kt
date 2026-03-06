package com.example.chelasmulti_playerpokerdice.services

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.runBlocking
private val Context.profileDataStore by preferencesDataStore("profile_prefs")

class ProfileService(private val context: Context) : IProfileService {

    companion object Keys {
        val USERNAME = stringPreferencesKey("username")
        val GAMES_PLAYED = intPreferencesKey("games_played")
        val GAMES_WON = intPreferencesKey("games_won")
        fun handKey(rank: HandRank) = intPreferencesKey("hand_freq_${rank.name}")
    }

    override val usernameFlow: Flow<String> =
        context.profileDataStore.data.map { prefs ->
            prefs[USERNAME] ?: "Player"
        }

    override val gamesPlayedFlow: Flow<Int> =
        context.profileDataStore.data.map { prefs ->
            prefs[GAMES_PLAYED] ?: 0
        }

    override val gamesWonFlow: Flow<Int> =
        context.profileDataStore.data.map { prefs ->
            prefs[GAMES_WON] ?: 0
        }

    override val handFrequencyFlow: Flow<Map<HandRank, Int>> =
        context.profileDataStore.data.map { prefs ->
            HandRank.entries.associateWith { rank ->
                prefs[handKey(rank)] ?: 0
            }
        }

    override suspend fun saveUserName(name: String) {
        context.profileDataStore.edit { prefs ->
            prefs[USERNAME] = name
        }
    }

    override suspend fun recordGameResult(didWin: Boolean) {
        context.profileDataStore.edit { prefs ->
            prefs[GAMES_PLAYED] = (prefs[GAMES_PLAYED] ?: 0) + 1
            if (didWin) {
                prefs[GAMES_WON] = (prefs[GAMES_WON] ?: 0) + 1
            }
        }
    }

    override suspend fun recordHand(hand: HandRank) {
        context.profileDataStore.edit { prefs ->
            val key = handKey(hand)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    override fun getUsername(): String = runBlocking {
        usernameFlow.first()
    }
}