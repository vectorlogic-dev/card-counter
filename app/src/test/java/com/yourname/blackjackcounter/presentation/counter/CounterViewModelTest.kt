package com.yourname.blackjackcounter.presentation.counter

import com.yourname.blackjackcounter.domain.model.CardRank
import com.yourname.blackjackcounter.domain.model.DealerPeekRule
import com.yourname.blackjackcounter.domain.model.DealerSoft17Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterViewModelTest {

    @Test
    fun `onCardSeen updates running count true count and penetration`() {
        val viewModel = CounterViewModel()
        val initialDecksRemaining = viewModel.uiState.value.decksRemaining

        viewModel.onCardSeen(CardRank.TWO)

        val updated = viewModel.uiState.value
        assertEquals(1.0, updated.runningCount, 0.0001)
        assertEquals(initialDecksRemaining - (1.0 / 52.0), updated.decksRemaining, 0.0001)
        assertEquals(0.2, updated.trueCount, 0.0001)
        assertEquals(0.3, updated.deckPenetrationPercent, 0.0001)
    }

    @Test
    fun `onCardSeen ace does not change ace side count on systems that do not need it`() {
        val viewModel = CounterViewModel()

        viewModel.onCardSeen(CardRank.ACE)

        val updated = viewModel.uiState.value
        assertEquals(0, updated.acesSeen)
        assertEquals(0.0, updated.acesExpected, 0.0001)
    }

    @Test
    fun `onCardSeen ace increments ace side count for omega ii`() {
        val viewModel = CounterViewModel()
        viewModel.setCountingSystem("omega_ii")

        viewModel.onCardSeen(CardRank.ACE)

        val updated = viewModel.uiState.value
        assertEquals(1, updated.acesSeen)
        assertEquals(0.1, updated.acesExpected, 0.0001)
    }

    @Test
    fun `setDecksRemaining clamps to minimum and true count uses clamped value`() {
        val viewModel = CounterViewModel()

        repeat(2) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(0.0)

        val updated = viewModel.uiState.value
        assertEquals(0.25, updated.decksRemaining, 0.0001)
        assertEquals(8.0, updated.trueCount, 0.0001)
    }

    @Test
    fun `resetCount clears running and shoe state`() {
        val viewModel = CounterViewModel()

        repeat(5) {
            viewModel.onCardSeen(CardRank.FIVE)
        }
        viewModel.onCardSeen(CardRank.ACE)

        viewModel.resetCount()

        val updated = viewModel.uiState.value
        assertEquals(0.0, updated.runningCount, 0.0001)
        assertEquals(0.0, updated.trueCount, 0.0001)
        assertEquals(0, updated.acesSeen)
        assertEquals(0.0, updated.deckPenetrationPercent, 0.0001)
        assertEquals(0, updated.cardsDrawn)
        assertEquals(updated.totalCards, updated.cardsLeft)
    }

    @Test
    fun `available systems includes reko and hi opt ii`() {
        val viewModel = CounterViewModel()
        val ids = viewModel.availableSystems.map { it.id }

        assertTrue(ids.contains("reko"))
        assertTrue(ids.contains("hi_opt_ii"))
    }

    @Test
    fun `hi opt ii enables ace side count guidance`() {
        val viewModel = CounterViewModel()

        viewModel.setCountingSystem("hi_opt_ii")

        val updated = viewModel.uiState.value
        assertTrue(updated.selectedSystemRecommendsAceSideCount)
        assertTrue(updated.selectedSystemUsesTrueCount)
    }

    @Test
    fun `ko hides true count strategy deviations`() {
        val viewModel = CounterViewModel()

        viewModel.setCountingSystem("ko")

        val updated = viewModel.uiState.value
        assertFalse(updated.selectedSystemUsesTrueCount)
        assertFalse(updated.canEstimateEdge)
        assertTrue(updated.strategyDeviationRows.isEmpty())
    }

    @Test
    fun `hi lo exposes common 16 vs 10 deviation`() {
        val viewModel = CounterViewModel()

        viewModel.setCountingSystem("hi_lo")

        val updated = viewModel.uiState.value
        assertTrue(updated.strategyDeviationRows.any { it.playLabel == "16 vs 10" && it.indexLabel.contains("TC 0+") })
    }

    @Test
    fun `table rules can be updated from menu settings`() {
        val viewModel = CounterViewModel()

        viewModel.setDealerSoft17Rule(DealerSoft17Rule.HIT)
        viewModel.setDealerPeekRule(DealerPeekRule.ACE_ONLY)
        viewModel.setMaxSplitHands(6)

        val updated = viewModel.uiState.value
        assertEquals(DealerSoft17Rule.HIT, updated.tableRules.dealerSoft17Rule)
        assertEquals(DealerPeekRule.ACE_ONLY, updated.tableRules.dealerPeekRule)
        assertEquals(6, updated.tableRules.maxSplitHands)
    }

    @Test
    fun `enabling six card charlie improves estimated edge`() {
        val viewModel = CounterViewModel()
        val edgeBefore = viewModel.uiState.value.estimatedPlayerEdgePercent

        viewModel.setSixCardCharlieAutoWin(true)

        val updated = viewModel.uiState.value
        assertTrue(updated.tableRules.sixCardCharlieAutoWin)
        assertTrue(updated.estimatedPlayerEdgePercent > edgeBefore)
    }

    @Test
    fun `split deviations include no-peek adjustments and ace split notes`() {
        val viewModel = CounterViewModel()
        viewModel.setDealerPeekRule(DealerPeekRule.ACE_ONLY)
        viewModel.setResplitAces(false)
        viewModel.setHitSplitAces(false)

        val sections = viewModel.getSplitDeviationSections(viewModel.uiState.value.tableRules)
        val rows = sections.flatMap { it.rows }

        assertTrue(rows.any { it.hand == "10 vs 10 (double deviation)" && it.action.contains("Avoid") })
        assertTrue(rows.any { it.hand == "A,A re-split" })
        assertTrue(rows.any { it.hand == "After splitting A,A" })
        assertTrue(rows.any { it.hand == "A,A vs 10" })
    }

    @Test
    fun `strategy chart applies count-based 10 vs 10 double deviation`() {
        val viewModel = CounterViewModel()
        repeat(4) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)

        val state = viewModel.uiState.value
        val chart = viewModel.buildStrategyChartModel(state)
        val hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        val dealerTenCell = hardTenRow.cells[8]

        assertTrue(dealerTenCell.action.startsWith("D"))
        assertTrue(dealerTenCell.isDeviation)
    }

    @Test
    fun `strategy chart avoids 10 vs 10 double and adjusts ace split on no-peek`() {
        val viewModel = CounterViewModel()
        viewModel.setDealerPeekRule(DealerPeekRule.ACE_ONLY)
        repeat(4) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)

        val state = viewModel.uiState.value
        val chart = viewModel.buildStrategyChartModel(state)
        val hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        val dealerTenCell = hardTenRow.cells[8]
        val pairsSection = chart.sections.first { it.title == "Pairs" }
        val acePairRow = pairsSection.rows.first { it.handLabel == "A,A" }
        val dealerAceCell = acePairRow.cells[9]

        assertEquals("H", dealerTenCell.action)
        assertEquals("P", dealerAceCell.action)
    }

    @Test
    fun `strategy chart hits split aces vs ace on no-peek for ace and ten`() {
        val viewModel = CounterViewModel()
        viewModel.setDealerPeekRule(DealerPeekRule.NO_PEEK)

        val state = viewModel.uiState.value
        val chart = viewModel.buildStrategyChartModel(state)
        val pairsSection = chart.sections.first { it.title == "Pairs" }
        val acePairRow = pairsSection.rows.first { it.handLabel == "A,A" }
        val dealerAceCell = acePairRow.cells[9]

        assertEquals("H", dealerAceCell.action)
    }
}
