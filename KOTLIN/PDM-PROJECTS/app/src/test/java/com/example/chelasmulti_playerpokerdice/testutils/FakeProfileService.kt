package com.example.chelasmulti_playerpokerdice.testutils

import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake ProfileService for unit testing that doesn't require Android Context.
 * This allows tests to run on JVM without instrumentation.
 */
class FakeProfileService(
    initialUsername: String = "Player",
    initialGamesPlayed: Int = 0,
    initialGamesWon: Int = 0,
    initialHandFrequency: Map<HandRank, Int> = emptyMap()
) : IProfileService {
    private val _username = MutableStateFlow(initialUsername)
    private val _gamesPlayed = MutableStateFlow(initialGamesPlayed)
    private val _gamesWon = MutableStateFlow(initialGamesWon)
    private val _handFrequency = MutableStateFlow(
        initialHandFrequency.toMutableMap().apply {
            HandRank.entries.forEach { rank ->
                putIfAbsent(rank, 0)
            }
        }
    )

    override val usernameFlow: Flow<String> = _username
    override val gamesPlayedFlow: Flow<Int> = _gamesPlayed
    override val gamesWonFlow: Flow<Int> = _gamesWon
    override val handFrequencyFlow: Flow<Map<HandRank, Int>> = _handFrequency

    override suspend fun saveUserName(name: String) {
        _username.value = name
    }

    override suspend fun recordGameResult(didWin: Boolean) {
        _gamesPlayed.value++
        if (didWin) {
            _gamesWon.value++
        }
    }

    override suspend fun recordHand(hand: HandRank) {
        val current = _handFrequency.value.toMutableMap()
        current[hand] = (current[hand] ?: 0) + 1
        _handFrequency.value = current
    }

    override fun getUsername(): String = _username.value
}
