package com.vectorlogic.blackjackcounter.domain.drill

import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.KOCountingSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DrillEngineTest {

    @Test
    fun `generateCards produces the requested length`() {
        DrillSize.entries.forEach { size ->
            val cards = DrillEngine.generateCards(size, random = Random(42))
            assertEquals(size.cardCount, cards.size)
        }
    }

    @Test
    fun `one deck drill is exactly one real deck`() {
        val cards = DrillEngine.generateCards(DrillSize.DECK, random = Random(7))
        CardRank.entries.forEach { rank ->
            assertEquals("count of $rank", 4, cards.count { it == rank })
        }
    }

    @Test
    fun `one deck drill nets to zero for Hi-Lo and plus four for KO`() {
        repeat(20) { seed ->
            val cards = DrillEngine.generateCards(DrillSize.DECK, random = Random(seed))
            assertEquals(0.0, DrillEngine.runningCountFor(HiLoCountingSystem, cards), 0.0001)
            assertEquals(4.0, DrillEngine.runningCountFor(KOCountingSystem, cards), 0.0001)
        }
    }

    @Test
    fun `partial drills never exceed the rank counts of the shoe they are dealt from`() {
        repeat(20) { seed ->
            val half = DrillEngine.generateCards(DrillSize.HALF_DECK, random = Random(seed))
            assertTrue(CardRank.entries.all { rank -> half.count { it == rank } <= 4 })

            val deckAndHalf = DrillEngine.generateCards(DrillSize.DECK_AND_A_HALF, random = Random(seed))
            assertTrue(CardRank.entries.all { rank -> deckAndHalf.count { it == rank } <= 8 })
        }
    }

    @Test
    fun `cards are shuffled rather than dealt in rank order`() {
        val cards = DrillEngine.generateCards(DrillSize.DECK, random = Random(3))
        assertNotEquals(DrillEngine.buildShoe(1), cards)
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
    fun `generateCards is deterministic given a seed`() {
        val seed = 1234L
        val first = DrillEngine.generateCards(DrillSize.HALF_DECK, random = Random(seed))
        val second = DrillEngine.generateCards(DrillSize.HALF_DECK, random = Random(seed))
        assertEquals(first, second)
    }
}
