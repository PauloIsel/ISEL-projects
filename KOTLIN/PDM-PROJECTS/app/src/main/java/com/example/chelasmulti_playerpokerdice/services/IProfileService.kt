package com.example.chelasmulti_playerpokerdice.services

import kotlinx.coroutines.flow.Flow

interface IProfileService {
    val usernameFlow: Flow<String>
    val gamesPlayedFlow: Flow<Int>
    val gamesWonFlow: Flow<Int>
    val handFrequencyFlow: Flow<Map<HandRank, Int>>

    suspend fun saveUserName(name: String)
    suspend fun recordGameResult(didWin: Boolean)
    suspend fun recordHand(hand: HandRank)
    fun getUsername(): String
}

