package com.example.chelasmulti_playerpokerdice.ui.components

import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.services.Player

fun evaluateHand(player: Player): HandRank {
    val values = player.dice.map { it.faceValue }.sorted()
    val counts = values.groupingBy { it }.eachCount().values.sortedDescending()

    return when {
        counts.first() == 5 -> HandRank.FIVE_OF_A_KIND
        counts.first() == 4 -> HandRank.FOUR_OF_A_KIND
        counts.size == 2 && counts[0] == 3 && counts[1] == 2 -> HandRank.FULL_HOUSE
        isStraight(values) -> HandRank.STRAIGHT
        counts.first() == 3 -> HandRank.THREE_OF_A_KIND
        counts.size >= 2 && counts[0] == 2 && counts[1] == 2 -> HandRank.TWO_PAIR
        counts.first() == 2 -> HandRank.PAIR
        else -> HandRank.BUST
    }
}

private fun isStraight(values: List<Int>): Boolean {
    return values == listOf(1,2,3,4,5) || values == listOf(2,3,4,5,6)
}

fun compareHands(p1: Player, p2: Player): Int {
    val r1 = evaluateHand(p1)
    val r2 = evaluateHand(p2)

    if (r1.score != r2.score) return r1.score - r2.score

    val v1 = p1.dice.map { it.faceValue }.sortedDescending()
    val v2 = p2.dice.map { it.faceValue }.sortedDescending()

    val c1 = v1.groupingBy { it }.eachCount().toList()
        .sortedByDescending { it.second * 10 + it.first }
    val c2 = v2.groupingBy { it }.eachCount().toList()
        .sortedByDescending { it.second * 10 + it.first }

    for (i in c1.indices) {
        if (i >= c2.size) break
        val diff = c1[i].first - c2[i].first
        if (diff != 0) return diff
    }

    for (i in v1.indices) {
        val diff = v1[i] - v2[i]
        if (diff != 0) return diff
    }

    return 0
}