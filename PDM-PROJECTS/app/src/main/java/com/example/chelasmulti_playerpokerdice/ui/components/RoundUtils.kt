package com.example.chelasmulti_playerpokerdice.ui.components

import com.example.chelasmulti_playerpokerdice.services.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun getCurrentPlayer(game: Game): Player? =
    if (game.players.isEmpty()) null
    else game.players[game.rounds.currentPlayerIndex % game.players.size]


fun moveToNextPlayer(
    game: Game,
    scope: CoroutineScope,
    compareHands: (Player, Player) -> Int,
    recordLocalHand: (Player) -> Unit,
    onWinnerEvent: (Player) -> Unit
) {
    val rounds = game.rounds
    val players = game.players

    rounds.playersPlayedThisRound++

    recordPlayerHandIfNeeded(players, rounds, recordLocalHand)

    if (isRoundComplete(players, rounds)) {
        handleRoundEnd(
            game,
            scope,
            compareHands,
            onWinnerEvent
        )
    } else {
        advanceToNextPlayer(game)
    }
}


private fun recordPlayerHandIfNeeded(
    players: List<Player>,
    rounds: Round,
    recordLocalHand: (Player) -> Unit
) {
    val finished = players.getOrNull(rounds.currentPlayerIndex)
    if (finished != null) {
        recordLocalHand(finished)
    }
}

private fun isRoundComplete(players: List<Player>, rounds: Round): Boolean {
    return rounds.playersPlayedThisRound >= players.size
}

private fun handleRoundEnd(
    game: Game,
    scope: CoroutineScope,
    compareHands: (Player, Player) -> Int,
    onWinnerEvent: (Player) -> Unit
) {
    val winner = determineRoundWinner(game.players, compareHands)
    processWinner(game, scope, winner, onWinnerEvent)

    val rounds = game.rounds

    if (rounds.roundNumber < rounds.numberOfRounds) {
        advanceToNextRound(game)
    } else {
        handleTieBreaker(game)
    }
}

private fun determineRoundWinner(
    players: List<Player>,
    compare: (Player, Player) -> Int
): Player? {
    return players.maxWithOrNull(compare)
}

private fun processWinner(
    game: Game,
    scope: CoroutineScope,
    winner: Player?,
    onWinnerEvent: (Player) -> Unit
) {
    winner?.let {
        it.roundWins++
        game.lastRoundWinner = it
        scope.launch { onWinnerEvent(it) }
    }
}

private fun advanceToNextRound(game: Game) {
    val rounds = game.rounds
    val players = game.players

    rounds.roundNumber++
    rounds.playersPlayedThisRound = 0
    rounds.currentPlayerIndex = (rounds.roundNumber - 1) % players.size
    rounds.rollsLeft = 3
    clearAll(players)
}

private fun advanceToNextPlayer(game: Game) {
    val rounds = game.rounds
    val players = game.players

    rounds.currentPlayerIndex = (rounds.currentPlayerIndex + 1) % players.size
    rounds.rollsLeft = 3
}

fun handleTieBreaker(game: Game) {
    val maxWins = game.players.maxOfOrNull { it.roundWins } ?: 0
    val tied = game.players.filter { it.roundWins == maxWins }

    if (tied.size > 1) {
        startTieBreaker(game, tied)
    } else {
        endGameImmediately(game)
    }
}

private fun startTieBreaker(game: Game, tiedPlayers: List<Player>) {
    game.isTieBreaker = true
    game.players = tiedPlayers.toMutableList()

    val nextRound = game.rounds.numberOfRounds + 1
    game.rounds = Round(
        numberOfRounds = nextRound,
        roundNumber = nextRound
    )

    clearAll(game.players)
    rollAll(getCurrentPlayer(game)!!)
    game.rounds.rollsLeft--
}

private fun endGameImmediately(game: Game) {
    game.rounds.numberOfRounds = -1
}