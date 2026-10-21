package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.TrueCountScaling
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterStrategyEngineTest {

    private val noSurrender = CounterSessionState()
    private val lateSurrender = CounterSessionState(tableRules = TableRules(surrenderRule = SurrenderRule.LATE))

    @Test
    fun `stand deviations do not override surrender`() {
        listOf(0, 4, 5, 10).forEach { tc ->
            assertEquals("Rh", hardCell(lateSurrender, 16, 10, tc).action)
            assertEquals("Rh", hardCell(lateSurrender, 16, 9, tc).action)
            assertEquals("Rh", hardCell(lateSurrender, 15, 10, tc).action)
            assertFalse(hardCell(lateSurrender, 16, 10, tc).isDeviation)
        }
    }

    @Test
    fun `stand deviations still apply without surrender`() {
        assertEquals("H", hardCell(noSurrender, 16, 10, -1).action)
        assertEquals("S", hardCell(noSurrender, 16, 10, 0).action)
        assertEquals("H", hardCell(noSurrender, 15, 10, 3).action)
        assertEquals("S", hardCell(noSurrender, 15, 10, 4).action)
        assertEquals("H", hardCell(noSurrender, 16, 9, 4).action)
        assertEquals("S", hardCell(noSurrender, 16, 9, 5).action)
        assertTrue(hardCell(noSurrender, 16, 9, 5).isDeviation)
    }

    @Test
    fun `fifteen vs nine has no plus two stand index`() {
        (2..7).forEach { tc ->
            assertEquals("H", hardCell(noSurrender, 15, 9, tc).action)
        }
    }

    @Test
    fun `fab 4 surrender indices apply when surrender is offered`() {
        assertEquals("H", hardCell(lateSurrender, 14, 10, 2).action)
        assertEquals("Rh", hardCell(lateSurrender, 14, 10, 3).action)
        assertTrue(hardCell(lateSurrender, 14, 10, 3).isDeviation)

        assertEquals("H", hardCell(lateSurrender, 15, 9, 1).action)
        assertEquals("Rh", hardCell(lateSurrender, 15, 9, 2).action)

        assertEquals("H", hardCell(lateSurrender, 15, 11, 0).action)
        assertEquals("Rh", hardCell(lateSurrender, 15, 11, 1).action)
        assertEquals("Rh", hardCell(lateSurrender, 15, 11, 5).action)

        // Basic strategy surrenders 15 vs 10; below the index, hit instead.
        assertEquals("H", hardCell(lateSurrender, 15, 10, -1).action)
        assertTrue(hardCell(lateSurrender, 15, 10, -1).isDeviation)
    }

    @Test
    fun `fab 4 surrender indices are ignored without surrender`() {
        assertEquals("H", hardCell(noSurrender, 14, 10, 5).action)
        assertEquals("H", hardCell(noSurrender, 15, 9, 5).action)
    }

    @Test
    fun `illustrious 18 negative indices hit below the index`() {
        val cases = listOf(
            Triple(12, 4, 0),
            Triple(12, 5, -2),
            Triple(12, 6, -1),
            Triple(13, 2, -1),
            Triple(13, 3, -2)
        )
        cases.forEach { (hand, dealer, index) ->
            assertEquals("$hand vs $dealer at index", "S", hardCell(noSurrender, hand, dealer, index).action)
            val below = hardCell(noSurrender, hand, dealer, index - 1)
            assertEquals("$hand vs $dealer below index", "H", below.action)
            assertTrue(below.isDeviation)
        }
    }

    @Test
    fun `level two systems use scaled indices in the chart`() {
        val zenScale = TrueCountScaling.hiLoEquivalentScale(ZenCountSystem)

        // Hi-Lo 10 vs 10 double at +4 becomes Zen +7.
        assertEquals("H", hardCell(noSurrender, 10, 10, 6, zenScale).action)
        assertEquals("D", hardCell(noSurrender, 10, 10, 7, zenScale).action)
        // Hi-Lo 16 vs 9 stand at +5 becomes Zen +9.
        assertEquals("H", hardCell(noSurrender, 16, 9, 8, zenScale).action)
        assertEquals("S", hardCell(noSurrender, 16, 9, 9, zenScale).action)
        // Hi-Lo 12 vs 5 hit below -2 becomes Zen -4.
        assertEquals("S", hardCell(noSurrender, 12, 5, -4, zenScale).action)
        assertEquals("H", hardCell(noSurrender, 12, 5, -5, zenScale).action)
    }

    @Test
    fun `deviation rows show converted thresholds that match the chart`() {
        val zenScale = TrueCountScaling.hiLoEquivalentScale(ZenCountSystem)
        val rows = rows(noSurrender, zenScale)

        assertEquals("Take at TC +5", label(rows, "Insurance vs A"))
        assertEquals("Stand at TC 0+", label(rows, "16 vs 10"))
        assertEquals("Stand at TC +7", label(rows, "15 vs 10"))
        assertEquals("Double at TC +7", label(rows, "10 vs 10"))
        assertEquals("Stand at TC +9", label(rows, "16 vs 9"))
        assertEquals("Split at TC +9", label(rows, "10,10 vs 5"))
        assertEquals("Hit below TC -4", label(rows, "12 vs 5"))
        assertEquals("Hit below TC 0", label(rows, "12 vs 4"))

        val hiOptRows = rows(noSurrender, TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem))
        assertEquals("Double at TC +7", label(hiOptRows, "10 vs 10"))
        assertEquals("Stand at TC +3", label(hiOptRows, "12 vs 3"))
    }

    @Test
    fun `hi lo deviation rows keep hi lo indices and cover the illustrious 18`() {
        val rows = rows(noSurrender, 1.0)

        assertEquals("Take at TC +3", label(rows, "Insurance vs A"))
        assertEquals("Stand at TC +4", label(rows, "15 vs 10"))
        assertEquals("Stand at TC +2", label(rows, "12 vs 3"))
        assertEquals("Double at TC +1", label(rows, "11 vs A"))
        assertEquals("Hit below TC -1", label(rows, "13 vs 2"))
        assertEquals("Hit below TC -2", label(rows, "13 vs 3"))
        assertNull(rows.firstOrNull { it.playLabel == "15 vs 9" })
        assertNull(rows.firstOrNull { it.playLabel == "14 vs 10" })
    }

    @Test
    fun `deviation rows show surrender instead of stand indices when surrender is offered`() {
        val rows = rows(lateSurrender, 1.0)

        assertEquals("Surrender (any TC)", label(rows, "16 vs 10"))
        assertEquals("Surrender (any TC)", label(rows, "16 vs 9"))
        assertEquals(listOf("Surrender at TC 0+"), rows.filter { it.playLabel == "15 vs 10" }.map { it.indexLabel })
        assertEquals("Surrender at TC +3", label(rows, "14 vs 10"))
        assertEquals("Surrender at TC +2", label(rows, "15 vs 9"))
        assertEquals(listOf("Surrender at TC +1"), rows.filter { it.playLabel == "15 vs A" }.map { it.indexLabel })
    }

    private fun hardCell(
        state: CounterSessionState,
        hand: Int,
        dealer: Int,
        decisionTrueCount: Int,
        scale: Double = 1.0
    ): StrategyTableCell {
        val chart = CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            trueCount = decisionTrueCount.toDouble(),
            supportsTrueCount = true,
            decisionTrueCount = decisionTrueCount,
            trueCountScale = scale
        )
        val row = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == hand.toString() }
        return row.cells[dealer - 2]
    }

    private fun rows(state: CounterSessionState, scale: Double): List<StrategyDeviationRow> {
        return CounterStrategyEngine.buildStrategyDeviationRows(
            state = state,
            usesTrueCount = true,
            keyCount = null,
            pivot = null,
            trueCountScale = scale
        )
    }

    private fun label(rows: List<StrategyDeviationRow>, play: String): String {
        return rows.first { it.playLabel == play }.indexLabel
    }
}
