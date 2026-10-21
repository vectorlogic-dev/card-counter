package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.OmegaIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.TrueCountScaling
import com.vectorlogic.blackjackcounter.data.counting.WongHalvesSystem
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CounterCalculations
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
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
    private val h17 = CounterSessionState(tableRules = TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT))
    private val h17LateSurrender = CounterSessionState(
        tableRules = TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT, surrenderRule = SurrenderRule.LATE)
    )

    @Test
    fun `soft nineteen doubles against six only under h17`() {
        assertEquals("Ds", chartCell(h17, "Soft Totals", "A,8", 6, 0.0).action)
        assertFalse(chartCell(h17, "Soft Totals", "A,8", 6, 0.0).isDeviation)
        assertEquals("S", chartCell(h17, "Soft Totals", "A,8", 5, 0.0).action)
        assertEquals("S", chartCell(noSurrender, "Soft Totals", "A,8", 6, 0.0).action)

        val h17NineToEleven = CounterSessionState(
            tableRules = TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT, doubleDownRule = DoubleDownRule.NINE_TO_ELEVEN)
        )
        assertEquals("S", chartCell(h17NineToEleven, "Soft Totals", "A,8", 6, 0.0).action)
    }

    @Test
    fun `h17 with surrender gives up fifteen seventeen and eights against an ace`() {
        listOf(-1.0, 0.0, 3.0, 8.0).forEach { tc ->
            assertEquals("Rh", hardCell(h17LateSurrender, 15, 11, tc).action)
            assertEquals("Rh", hardCell(h17LateSurrender, 16, 11, tc).action)
            assertEquals("Rs", chartCell(h17LateSurrender, "Hard Totals", "17", 11, tc).action)
            assertEquals("Rp", chartCell(h17LateSurrender, "Pairs", "8,8", 11, tc).action)
            assertFalse(chartCell(h17LateSurrender, "Hard Totals", "17", 11, tc).isDeviation)
        }
        assertEquals("S", chartCell(h17LateSurrender, "Hard Totals", "18+", 11, 0.0).action)
        assertEquals("S", chartCell(h17LateSurrender, "Hard Totals", "17", 10, 0.0).action)
        assertEquals("P", chartCell(h17LateSurrender, "Pairs", "8,8", 10, 0.0).action)
    }

    @Test
    fun `h17 surrenders against an ace need surrender to be offered`() {
        assertEquals("H", hardCell(h17, 15, 11, 0).action)
        assertEquals("S", chartCell(h17, "Hard Totals", "17+", 11, 0.0).action)
        assertEquals("P", chartCell(h17, "Pairs", "8,8", 11, 0.0).action)
        assertEquals("H", hardCell(lateSurrender, 15, 11, 0).action)
        assertEquals("S", chartCell(lateSurrender, "Hard Totals", "17+", 11, 0.0).action)
        assertEquals("P", chartCell(lateSurrender, "Pairs", "8,8", 11, 0.0).action)
    }

    @Test
    fun `eights against an ace surrender else hit when the dealer does not peek`() {
        val noPeek = CounterSessionState(
            tableRules = TableRules(
                dealerSoft17Rule = DealerSoft17Rule.HIT,
                surrenderRule = SurrenderRule.LATE,
                dealerPeekRule = DealerPeekRule.NO_PEEK
            )
        )
        assertEquals("Rh", chartCell(noPeek, "Pairs", "8,8", 11, 0.0).action)
    }

    @Test
    fun `seventeen gets its own row only when it plays differently from eighteen`() {
        fun hardLabels(state: CounterSessionState) = chart(state, 0.0, 1.0).sections
            .first { it.title == "Hard Totals" }.rows.map { it.handLabel }

        assertEquals("17+", hardLabels(noSurrender).first())
        assertEquals("17+", hardLabels(lateSurrender).first())
        assertEquals("17+", hardLabels(h17).first())
        assertEquals(listOf("18+", "17", "16"), hardLabels(h17LateSurrender).take(3))
    }

    @Test
    fun `h17 fifteen vs ace surrender index is minus one`() {
        assertEquals("Rh", hardCell(h17LateSurrender, 15, 11, -1.0).action)
        assertEquals("H", hardCell(h17LateSurrender, 15, 11, -1.1).action)
        assertTrue(hardCell(h17LateSurrender, 15, 11, -1.1).isDeviation)

        assertEquals(listOf("Surrender at TC -1+"), rows(h17LateSurrender, 1.0).filter { it.playLabel == "15 vs A" }.map { it.indexLabel })
        assertEquals(listOf("Surrender at TC +1"), rows(lateSurrender, 1.0).filter { it.playLabel == "15 vs A" }.map { it.indexLabel })
    }

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

        // Hi-Lo 10 vs 10 double at +4 becomes Zen +7.2 (4 x 1.789 = 7.16).
        assertEquals("H", hardCell(noSurrender, 10, 10, 7.1, zenScale).action)
        assertEquals("D", hardCell(noSurrender, 10, 10, 7.2, zenScale).action)
        // Hi-Lo 16 vs 9 stand at +5 becomes Zen +8.9.
        assertEquals("H", hardCell(noSurrender, 16, 9, 8.8, zenScale).action)
        assertEquals("S", hardCell(noSurrender, 16, 9, 8.9, zenScale).action)
        // Hi-Lo 12 vs 5 hit below -2 becomes Zen -3.6.
        assertEquals("S", hardCell(noSurrender, 12, 5, -3.6, zenScale).action)
        assertEquals("H", hardCell(noSurrender, 12, 5, -3.7, zenScale).action)
    }

    @Test
    fun `deviation rows show converted thresholds that match the chart`() {
        val zenScale = TrueCountScaling.hiLoEquivalentScale(ZenCountSystem)
        val rows = rows(noSurrender, zenScale)

        assertEquals("Take at TC +5.4", label(rows, "Insurance vs A"))
        assertEquals("Stand at TC 0+", label(rows, "16 vs 10"))
        assertEquals("Stand at TC +7.2", label(rows, "15 vs 10"))
        assertEquals("Double at TC +7.2", label(rows, "10 vs 10"))
        assertEquals("Stand at TC +8.9", label(rows, "16 vs 9"))
        assertEquals("Split at TC +8.9", label(rows, "10,10 vs 5"))
        assertEquals("Hit below TC -3.6", label(rows, "12 vs 5"))
        assertEquals("Hit below TC 0", label(rows, "12 vs 4"))

        val hiOptRows = rows(noSurrender, TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem))
        assertEquals("Double at TC +6.7", label(hiOptRows, "10 vs 10"))
        assertEquals("Stand at TC +3.3", label(hiOptRows, "12 vs 3"))
    }

    @Test
    fun `every deviation label is exactly where the chart changes`() {
        val systems = listOf(
            HiLoCountingSystem,
            WongHalvesSystem,
            HiOptIICountingSystem,
            OmegaIICountingSystem,
            ZenCountSystem
        )
        val thresholdPattern = Regex("""TC ([+-]?\d+(?:\.\d)?)""")
        systems.forEach { system ->
            val scale = TrueCountScaling.hiLoEquivalentScale(system)
            listOf(noSurrender, lateSurrender, h17LateSurrender).forEach { state ->
                val indexRows = rows(state, scale).filter {
                    it.playLabel != "Insurance vs A" && thresholdPattern.containsMatchIn(it.indexLabel)
                }
                assertTrue(indexRows.size >= 15)
                indexRows.forEach { row ->
                    val threshold = thresholdPattern.find(row.indexLabel)!!.groupValues[1].toDouble()
                    val justBelow = CounterCalculations.roundToOneDecimal(threshold - 0.1)
                    val context = "${system.displayName} ${state.tableRules.surrenderRule} ${row.playLabel} (${row.indexLabel})"
                    assertTrue(
                        context,
                        cell(state, row.playLabel, threshold, scale).action !=
                            cell(state, row.playLabel, justBelow, scale).action
                    )
                }
            }
        }
    }

    @Test
    fun `index zero plays wait for a non negative true count`() {
        // A raw -0.9 must not count as 0 (truncation would make it 0).
        assertEquals("H", hardCell(noSurrender, 16, 10, -0.9).action)
        assertEquals("S", hardCell(noSurrender, 16, 10, 0.0).action)
        assertEquals("H", hardCell(noSurrender, 12, 4, -0.9).action)
        assertTrue(hardCell(noSurrender, 12, 4, -0.9).isDeviation)
        assertEquals("S", hardCell(noSurrender, 12, 4, 0.0).action)
        assertEquals("H", hardCell(lateSurrender, 15, 10, -0.9).action)
        assertEquals("Rh", hardCell(lateSurrender, 15, 10, 0.0).action)
        // Hi-Lo -1 indices: -1.0 is at the index, -1.1 is below it.
        assertEquals("S", hardCell(noSurrender, 13, 2, -1.0).action)
        assertEquals("H", hardCell(noSurrender, 13, 2, -1.1).action)
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

    @Test
    fun `highlighted rows match the play the chart makes`() {
        val ruleSets = listOf(
            TableRules(),
            TableRules(surrenderRule = SurrenderRule.LATE),
            TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT),
            TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT, surrenderRule = SurrenderRule.LATE),
            TableRules(dealerPeekRule = DealerPeekRule.ACE_ONLY),
            TableRules(dealerPeekRule = DealerPeekRule.NO_PEEK, dealerSoft17Rule = DealerSoft17Rule.HIT),
            TableRules(doubleDownRule = DoubleDownRule.TEN_TO_ELEVEN, surrenderRule = SurrenderRule.LATE)
        )
        val scales = listOf(
            1.0,
            TrueCountScaling.hiLoEquivalentScale(ZenCountSystem),
            TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem)
        )
        ruleSets.forEach { rules ->
            val state = CounterSessionState(tableRules = rules)
            scales.forEach { scale ->
                val triggeredRows = rows(state, scale).filter { it.trigger != null && it.playLabel != "Insurance vs A" }
                assertTrue(triggeredRows.isNotEmpty())
                // Thresholds are one-decimal native counts, so step in tenths to hit every boundary.
                (-120..120).map { it / 10.0 }.forEach { tc ->
                    triggeredRows.forEach { row ->
                        val cell = cellFor(state, row.playLabel, tc, scale)
                        val playPrefix = actionPrefix(row.indexLabel)
                        assertEquals(
                            "${row.playLabel} (${row.indexLabel}) at TC $tc, scale $scale, $rules",
                            cell.action.startsWith(playPrefix),
                            row.isActiveAt(tc)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `insurance row is active from its converted index`() {
        val insurance = rows(noSurrender, 1.0).first { it.playLabel == "Insurance vs A" }
        assertFalse(insurance.isActiveAt(2.0))
        assertTrue(insurance.isActiveAt(3.0))

        val zenInsurance = rows(noSurrender, TrueCountScaling.hiLoEquivalentScale(ZenCountSystem))
            .first { it.playLabel == "Insurance vs A" }
        // Hi-Lo +3 x Zen scale ~1.789 = 5.4 at one decimal.
        assertFalse(zenInsurance.isActiveAt(5.3))
        assertTrue(zenInsurance.isActiveAt(5.4))
    }

    @Test
    fun `doubles the table rules forbid are labelled and never highlighted`() {
        val noPeekOnTen = CounterSessionState(tableRules = TableRules(dealerPeekRule = DealerPeekRule.ACE_ONLY))
        val tenVsTen = rows(noPeekOnTen, 1.0).first { it.playLabel == "10 vs 10" }
        assertEquals("No double (rules)", tenVsTen.indexLabel)
        assertNull(tenVsTen.trigger)
        assertFalse(tenVsTen.isActiveAt(10.0))

        val tenToEleven = CounterSessionState(tableRules = TableRules(doubleDownRule = DoubleDownRule.TEN_TO_ELEVEN))
        val rows = rows(tenToEleven, 1.0)
        assertEquals("No double (rules)", label(rows, "9 vs 2"))
        assertEquals("No double (rules)", label(rows, "9 vs 7"))
        assertEquals("Double at TC +4", label(rows, "10 vs 10"))
    }

    @Test
    fun `eleven vs ace doubles at any count under h17`() {
        val h17 = CounterSessionState(tableRules = TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT))
        val row = rows(h17, 1.0).first { it.playLabel == "11 vs A" }
        assertEquals("Double (any TC)", row.indexLabel)
        assertNull(row.trigger)
        assertEquals("Double at TC +1", label(rows(noSurrender, 1.0), "11 vs A"))
    }

    @Test
    fun `running count reference rows are active once the count reaches them`() {
        val rows = CounterStrategyEngine.buildStrategyDeviationRows(
            state = noSurrender,
            usesTrueCount = false,
            keyCount = -4,
            pivot = 4,
            trueCountScale = 1.0
        )
        val keyCount = rows.first { it.playLabel.startsWith("Key Count") }
        val pivot = rows.first { it.playLabel.startsWith("Pivot") }
        assertFalse(keyCount.isActiveAt(-5.0))
        assertTrue(keyCount.isActiveAt(-4.0))
        assertFalse(pivot.isActiveAt(3.0))
        assertTrue(pivot.isActiveAt(4.0))
    }

    private fun cellFor(state: CounterSessionState, playLabel: String, decisionTrueCount: Double, scale: Double): StrategyTableCell {
        val (hand, dealer) = playLabel.split(" vs ")
        val dealerUpcard = if (dealer == "A") 11 else dealer.toInt()
        val section = if (hand.contains(",")) "Pairs" else "Hard Totals"
        val chart = CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            trueCount = decisionTrueCount,
            supportsTrueCount = true,
            decisionTrueCount = decisionTrueCount,
            trueCountScale = scale
        )
        return chart.sections.first { it.title == section }.rows.first { it.handLabel == hand }.cells[dealerUpcard - 2]
    }

    private fun chart(state: CounterSessionState, decisionTrueCount: Double, scale: Double): StrategyChartModel {
        return CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            trueCount = decisionTrueCount,
            supportsTrueCount = true,
            decisionTrueCount = decisionTrueCount,
            trueCountScale = scale
        )
    }

    private fun chartCell(
        state: CounterSessionState,
        section: String,
        handLabel: String,
        dealerUpcard: Int,
        decisionTrueCount: Double
    ): StrategyTableCell {
        return chart(state, decisionTrueCount, 1.0).sections.first { it.title == section }
            .rows.first { it.handLabel == handLabel }.cells[dealerUpcard - 2]
    }

    private fun actionPrefix(indexLabel: String): String = when (indexLabel.substringBefore(" ")) {
        "Stand" -> "S"
        "Hit" -> "H"
        "Split" -> "P"
        "Double" -> "D"
        "Surrender" -> "R"
        else -> error("Unexpected index label $indexLabel")
    }

    private fun hardCell(
        state: CounterSessionState,
        hand: Int,
        dealer: Int,
        decisionTrueCount: Int,
        scale: Double = 1.0
    ): StrategyTableCell = hardCell(state, hand, dealer, decisionTrueCount.toDouble(), scale)

    private fun hardCell(
        state: CounterSessionState,
        hand: Int,
        dealer: Int,
        decisionTrueCount: Double,
        scale: Double = 1.0
    ): StrategyTableCell = cell(state, "$hand vs $dealer", decisionTrueCount, scale)

    // playLabel as the deviation rows write it, e.g. "16 vs 10", "10,10 vs 5", "15 vs A".
    private fun cell(
        state: CounterSessionState,
        playLabel: String,
        decisionTrueCount: Double,
        scale: Double
    ): StrategyTableCell {
        val (hand, dealer) = playLabel.split(" vs ")
        val dealerUpcard = if (dealer == "A") 11 else dealer.toInt()
        val chart = CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            trueCount = decisionTrueCount,
            supportsTrueCount = true,
            decisionTrueCount = decisionTrueCount,
            trueCountScale = scale
        )
        val section = if ("," in hand) "Pairs" else "Hard Totals"
        val row = chart.sections.first { it.title == section }.rows.first { it.handLabel == hand }
        return row.cells[dealerUpcard - 2]
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
