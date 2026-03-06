package com.example.chelasmulti_playerpokerdice.ui.components

import com.example.chelasmulti_playerpokerdice.services.*

fun isGameOver(game: Game): Boolean =
    game.rounds.numberOfRounds == -1

fun getWinners(game: Game): List<Player> {
    if (!isGameOver(game)) return emptyList()
    val pool = game.startingPlayers.filter { sp -> game.players.any { p -> p.name == sp.name } }
    if (pool.isEmpty()) return emptyList()
    val maxWins = pool.maxOfOrNull { it.roundWins } ?: 0
    return pool.filter { it.roundWins == maxWins }
}

fun clonePlayer(p: Player): Player {
    return Player(
        name = p.name,
        dice = p.dice.map { Dice(it.faceValue) }.toMutableList(),
        currentHandScore = p.currentHandScore,
        roundWins = p.roundWins,
        isConnected = p.isConnected,
        lastSeen = p.lastSeen
    )
}

fun syncStartingPlayersScores(game: Game) {
    game.players.forEach { current ->
        val starting = game.startingPlayers.find { it.name == current.name }
        starting?.roundWins = current.roundWins
    }
}