package com.vectorlogic.blackjackcounter.domain.model
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

        val baseHouseEdge = CounterCalculations.estimateHouseEdgePercent(
            tableRules = baseRules,
            totalDecks = 6
        )
        val charlieHouseEdge = CounterCalculations.estimateHouseEdgePercent(
            tableRules = charlieRules,
            totalDecks = 6
        )

        assertTrue(charlieHouseEdge < baseHouseEdge)
    }

    @Test
    fun `calculateEstimatedPlayerEdgePercent increases with higher true count`() {
        val rules = TableRules()

        val lowCountEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 0.0,
            usesTrueCount = true,
            tableRules = rules,
            totalDecks = 6
        )
        val highCountEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 4.0,
            usesTrueCount = true,
            tableRules = rules,
            totalDecks = 6
        )

        assertTrue(highCountEdge > lowCountEdge)
    }

    @Test
    fun `calculateEstimatedPlayerEdgePercent uses per system edge gain when provided`() {
        val rules = TableRules()
        val hiLoEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 4.0,
            usesTrueCount = true,
            tableRules = rules,
            totalDecks = 6,
            edgeGainPercentPerTrueCount = 0.50
        )
        val hiOptEdge = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = 4.0,
            usesTrueCount = true,
            tableRules = rules,
            totalDecks = 6,
            edgeGainPercentPerTrueCount = 0.46
        )

        assertTrue(hiLoEdge > hiOptEdge)
        assertEquals(0.2, hiLoEdge - hiOptEdge, 0.05)
    }

    @Test
    fun `calculateRecommendedBetUnits scales with true count`() {
        val ramp = listOf(
            CountRampStep(minCountInclusive = 1, units = 2),
            CountRampStep(minCountInclusive = 2, units = 4),
            CountRampStep(minCountInclusive = 3, units = 8),
            CountRampStep(minCountInclusive = 4, units = 12)
        )
        assertEquals(1, CounterCalculations.calculateRecommendedBetUnits(0.9, ramp = ramp))
        assertEquals(2, CounterCalculations.calculateRecommendedBetUnits(1.2, ramp = ramp))
        assertEquals(4, CounterCalculations.calculateRecommendedBetUnits(2.7, ramp = ramp))
        assertEquals(8, CounterCalculations.calculateRecommendedBetUnits(3.2, ramp = ramp))
        assertEquals(12, CounterCalculations.calculateRecommendedBetUnits(4.0, ramp = ramp))
    }

    @Test
    fun `calculateRecommendedBetUnits scales with running count for unbalanced systems`() {
        val ramp = listOf(
            CountRampStep(minCountInclusive = -3, units = 2),
            CountRampStep(minCountInclusive = 4, units = 4),
            CountRampStep(minCountInclusive = 8, units = 8),
            CountRampStep(minCountInclusive = 12, units = 12)
        )
        assertEquals(1, CounterCalculations.calculateRecommendedBetUnits(-3.1, ramp = ramp))
        assertEquals(2, CounterCalculations.calculateRecommendedBetUnits(-3.0, ramp = ramp))
        assertEquals(4, CounterCalculations.calculateRecommendedBetUnits(4.5, ramp = ramp))
        assertEquals(8, CounterCalculations.calculateRecommendedBetUnits(8.0, ramp = ramp))
        assertEquals(12, CounterCalculations.calculateRecommendedBetUnits(12.0, ramp = ramp))
    }

    @Test
    fun `integerizeTrueCount supports truncate floor and round`() {
        assertEquals(3, CounterCalculations.integerizeTrueCount(3.9, TrueCountIntegerization.TRUNCATE))
        assertEquals(-3, CounterCalculations.integerizeTrueCount(-3.9, TrueCountIntegerization.TRUNCATE))
        assertEquals(3, CounterCalculations.integerizeTrueCount(3.9, TrueCountIntegerization.FLOOR))
        assertEquals(-4, CounterCalculations.integerizeTrueCount(-3.1, TrueCountIntegerization.FLOOR))
        assertEquals(4, CounterCalculations.integerizeTrueCount(3.6, TrueCountIntegerization.ROUND))
        assertEquals(-4, CounterCalculations.integerizeTrueCount(-3.6, TrueCountIntegerization.ROUND))
    }

    @Test
    fun `estimateHouseEdgePercent improves with fewer decks under same rules`() {
        val rules = TableRules()
        val oneDeck = CounterCalculations.estimateHouseEdgePercent(
            tableRules = rules,
            totalDecks = 1
        )
        val eightDeck = CounterCalculations.estimateHouseEdgePercent(
            tableRules = rules,
            totalDecks = 8
        )

        assertTrue(oneDeck < eightDeck)
    }
}
