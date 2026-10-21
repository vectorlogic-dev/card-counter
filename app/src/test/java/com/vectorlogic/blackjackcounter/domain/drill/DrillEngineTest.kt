package com.vectorlogic.blackjackcounter.domain.drill

import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class DrillEngineTest {

    @Test
    fun `generateCards produces the requested length`() {
        val cards = DrillEngine.generateCards(DrillSize.DECK, random = Random(42))
        assertEquals(DrillSize.DECK.cardCount, cards.size)
    }

    @Test
    fun `running count for a known sequence matches Hi-Lo tag sum`() {
        val cards = listOf(
            CardRank.TWO, CardRank.THREE, CardRank.FOUR, CardRank.FIVE, CardRank.SIX,
            CardRank.TEN, CardRank.ACE
        )
        // Hi-Lo: 2-6 = +1 (5 cards), 10/A = -1 (2 cards), net = +3
        val count = DrillEngine.runningCountFor(HiLoCountingSystem, cards)
        assertEquals(3.0, count, 0.0001)
    }

    @Test
    fun `running count is deterministic given a seed`() {
        val seed = 1234L
        val first = DrillEngine.generateCards(DrillSize.HALF_DECK, random = Random(seed))
        val second = DrillEngine.generateCards(DrillSize.HALF_DECK, random = Random(seed))
        assertEquals(first, second)
    }
}
