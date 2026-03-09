package com.yourname.blackjackcounter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterCalculationsTest {

    @Test
    fun `calculateTrueCount returns zero when system does not use true count`() {
        val result = CounterCalculations.calculateTrueCount(
            runningCount = 6.0,
            decksRemaining = 3.0,
            usesTrueCount = false,
            minDecksRemaining = 0.25
        )

        assertEquals(0.0, result, 0.0001)
    }

    @Test
    fun `calculateTrueCount uses minimum decks remaining safety floor`() {
        val result = CounterCalculations.calculateTrueCount(
            runningCount = 2.0,
            decksRemaining = 0.0,
            usesTrueCount = true,
            minDecksRemaining = 0.25
        )

        assertEquals(8.0, result, 0.0001)
    }

    @Test
    fun `calculateDeckPenetrationPercent rounds to one decimal place`() {
        val result = CounterCalculations.calculateDeckPenetrationPercent(
            totalDecks = 6,
            decksRemaining = 4.37
        )

        assertEquals(27.2, result, 0.0001)
    }

    @Test
    fun `calculateExpectedAces is proportional to cards drawn`() {
        val result = CounterCalculations.calculateExpectedAces(cardsDrawn = 26)

        assertEquals(2.0, result, 0.0001)
    }

    @Test
    fun `estimateHouseEdgePercent decreases when six card charlie is enabled`() {
        val baseRules = TableRules()
        val charlieRules = TableRules(sixCardCharlieAutoWin = true)

        val baseHouseEdge = CounterCalculations.estimateHouseEdgePercent(baseRules)
        val charlieHouseEdge = CounterCalculations.estimateHouseEdgePercent(charlieRules)

        assertTrue(charlieHouseEdge < baseHouseEdge)
    }

    @Test
    fun `calculateEstimatedPlayerEdgePercent increases with higher true count`() {
        val rules = TableRules()

        val lowCountEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 0.0,
            usesTrueCount = true,
            tableRules = rules
        )
        val highCountEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 4.0,
            usesTrueCount = true,
            tableRules = rules
        )

        assertTrue(highCountEdge > lowCountEdge)
    }
}
