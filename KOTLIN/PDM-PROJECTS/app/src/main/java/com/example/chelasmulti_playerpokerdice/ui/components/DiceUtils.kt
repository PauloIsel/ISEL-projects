package com.example.chelasmulti_playerpokerdice.ui.components

import com.example.chelasmulti_playerpokerdice.services.Dice
import com.example.chelasmulti_playerpokerdice.services.Player

fun rollAll(player: Player) {
    player.dice.forEach { it.faceValue = (1..6).random() }
}

fun rollSome(player: Player, indices: List<Int>) {
    indices.forEach { idx ->
        player.dice.getOrNull(idx)?.faceValue = (1..6).random()
    }
}

fun clearAll(players: List<Player>) {
    players.forEach { p ->
        p.dice = MutableList(5) { Dice() }
    }
}
