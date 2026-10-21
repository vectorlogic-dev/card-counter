package com.vectorlogic.blackjackcounter.domain.drill

import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import kotlin.random.Random

object DrillEngine {

    private const val CARDS_PER_DECK = 52
    private const val SUITS_PER_DECK = 4

    /**
     * Deals [size] cards off the top of a freshly shuffled shoe of real 52-card decks,
     * so a one-deck drill is exactly one deck (balanced systems net to their IRC).
     */
    fun generateCards(size: DrillSize, random: Random = Random.Default): List<CardRank> {
        val decksNeeded = (size.cardCount + CARDS_PER_DECK - 1) / CARDS_PER_DECK
        return buildShoe(decksNeeded).shuffled(random).take(size.cardCount)
    }

    fun buildShoe(decks: Int): List<CardRank> =
        List(decks * SUITS_PER_DECK) { CardRank.entries }.flatten()

    fun runningCountFor(system: CountingSystem, cards: List<CardRank>): Double {
        return cards.fold(0.0) { acc, rank -> acc + system.getTagValue(rank) }
    }
}
