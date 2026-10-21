package com.vectorlogic.blackjackcounter.domain.drill

import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import kotlin.random.Random

object DrillEngine {

    fun generateCards(size: DrillSize, random: Random = Random.Default): List<CardRank> {
        val ranks = CardRank.entries
        return List(size.cardCount) { ranks[random.nextInt(ranks.size)] }
    }

    fun runningCountFor(system: CountingSystem, cards: List<CardRank>): Double {
        return cards.fold(0.0) { acc, rank -> acc + system.getTagValue(rank) }
    }
}
