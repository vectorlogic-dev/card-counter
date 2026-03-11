package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.KOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.OmegaIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.WongHalvesSystem
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CountingSystemId
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterViewModelTest {

    @Test
    fun `onCardSeen updates running count true count and penetration`() {
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()

        viewModel.onCardSeen(CardRank.ACE)

        val updated = viewModel.uiState.value
        assertEquals(0, updated.acesSeen)
        assertEquals(0.0, updated.acesExpected, 0.0001)
    }

    @Test
    fun `onCardSeen ace increments ace side count for omega ii`() {
        val viewModel = createViewModel()
        viewModel.setPurchasedSystemIds(setOf(OmegaIICountingSystem.id))
        viewModel.setCountingSystem(OmegaIICountingSystem.id)

        viewModel.onCardSeen(CardRank.ACE)

        val updated = viewModel.uiState.value
        assertEquals(1, updated.acesSeen)
        assertEquals(0.1, updated.acesExpected, 0.0001)
    }

    @Test
    fun `setDecksRemaining clamps to minimum and true count uses clamped value`() {
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()
        val ids = viewModel.availableSystems.map { it.id }

        assertTrue(ids.contains(CountingSystemId.REKO))
        assertTrue(ids.contains(CountingSystemId.HI_OPT_II))
    }

    @Test
    fun `hi opt ii enables ace side count guidance`() {
        val viewModel = createViewModel()
        viewModel.setPurchasedSystemIds(setOf(HiOptIICountingSystem.id))

        viewModel.setCountingSystem(HiOptIICountingSystem.id)

        val updated = viewModel.uiState.value
        assertTrue(updated.selectedSystemRecommendsAceSideCount)
        assertTrue(updated.selectedSystemUsesTrueCount)
    }

    @Test
    fun `level pricing metadata matches monetization tiers`() {
        val viewModel = createViewModel()
        val hiLo = viewModel.availableSystems.first { it.id == HiLoCountingSystem.id }
        val zen = viewModel.availableSystems.first { it.id == ZenCountSystem.id }
        val wong = viewModel.availableSystems.first { it.id == WongHalvesSystem.id }

        assertTrue(hiLo.isFree)
        assertFalse(zen.isFree)
        assertEquals("\$0.99", zen.defaultPriceLabel)
        assertEquals("\$1.99", wong.defaultPriceLabel)
    }

    @Test
    fun `locked paid systems require purchase before selection`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.setCountingSystem(ZenCountSystem.id))

        viewModel.setPurchasedSystemIds(setOf(ZenCountSystem.id))

        assertTrue(viewModel.setCountingSystem(ZenCountSystem.id))
        assertEquals(ZenCountSystem.id, viewModel.uiState.value.selectedCountingSystemId)
    }

    @Test
    fun `ko exposes running count key and pivot guidance`() {
        val viewModel = createViewModel()

        viewModel.setCountingSystem(KOCountingSystem.id)

        val updated = viewModel.uiState.value
        assertFalse(updated.selectedSystemUsesTrueCount)
        assertFalse(updated.canEstimateEdge)
        assertTrue(updated.strategyDeviationRows.any { it.playLabel.contains("Key Count") })
        assertTrue(updated.strategyDeviationRows.any { it.playLabel.contains("Pivot") })
    }

    @Test
    fun `hi lo exposes common 16 vs 10 deviation`() {
        val viewModel = createViewModel()

        viewModel.setCountingSystem(HiLoCountingSystem.id)

        val updated = viewModel.uiState.value
        assertTrue(updated.strategyDeviationRows.any { it.playLabel == "16 vs 10" && it.indexLabel.contains("TC 0+") })
    }

    @Test
    fun `table rules can be updated from menu settings`() {
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()
        val edgeBefore = viewModel.uiState.value.estimatedPlayerEdgePercent

        viewModel.setSixCardCharlieAutoWin(true)

        val updated = viewModel.uiState.value
        assertTrue(updated.tableRules.sixCardCharlieAutoWin)
        assertTrue(updated.estimatedPlayerEdgePercent > edgeBefore)
    }

    @Test
    fun `split deviations include no-peek adjustments and ace split notes`() {
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()
        viewModel.setDealerPeekRule(DealerPeekRule.NO_PEEK)

        val state = viewModel.uiState.value
        val chart = viewModel.buildStrategyChartModel(state)
        val pairsSection = chart.sections.first { it.title == "Pairs" }
        val acePairRow = pairsSection.rows.first { it.handLabel == "A,A" }
        val dealerAceCell = acePairRow.cells[9]

        assertEquals("H", dealerAceCell.action)
    }

    @Test
    fun `bet recommendation uses true count for balanced systems`() {
        val viewModel = createViewModel()
        repeat(3) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)

        val updated = viewModel.uiState.value
        assertEquals("TC", updated.betSizingCountLabel)
        assertEquals(3.0, updated.betSizingCountValue, 0.0001)
        assertEquals(8, updated.recommendedBetUnits)
    }

    @Test
    fun `bet recommendation uses running count for unbalanced systems`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(KOCountingSystem.id)
        repeat(5) {
            viewModel.onCardSeen(CardRank.TWO)
        }

        val updated = viewModel.uiState.value
        assertEquals("RC", updated.betSizingCountLabel)
        assertEquals(-15.0, updated.betSizingCountValue, 0.0001)
        assertEquals(1, updated.recommendedBetUnits)
    }

    @Test
    fun `resetCount restores deck aware initial running count for ko`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(KOCountingSystem.id)
        repeat(10) {
            viewModel.onCardSeen(CardRank.TWO)
        }

        viewModel.resetCount()

        val updated = viewModel.uiState.value
        assertEquals(-20.0, updated.runningCount, 0.0001)
        assertEquals(updated.totalDecks.toDouble(), updated.decksRemaining, 0.0001)
    }

    @Test
    fun `changing total decks recalculates ko initial running count`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(KOCountingSystem.id)
        viewModel.setTotalDecks(2)

        val updated = viewModel.uiState.value
        assertEquals(2, updated.totalDecks)
        assertEquals(-4.0, updated.runningCount, 0.0001)
    }

    @Test
    fun `deviation trigger uses integerized true count thresholds`() {
        val viewModel = createViewModel()
        repeat(2) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(0.51)
        var state = viewModel.uiState.value
        var chart = viewModel.buildStrategyChartModel(state)
        var hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        var dealerTenCell = hardTenRow.cells[8]
        assertEquals("H", dealerTenCell.action)
        assertFalse(dealerTenCell.isDeviation)

        viewModel.setDecksRemaining(0.49)
        state = viewModel.uiState.value
        chart = viewModel.buildStrategyChartModel(state)
        hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        dealerTenCell = hardTenRow.cells[8]
        assertTrue(dealerTenCell.action.startsWith("D"))
        assertTrue(dealerTenCell.isDeviation)
    }

    private fun createViewModel(): CounterViewModel {
        return CounterViewModel.provideFactory().create(CounterViewModel::class.java)
    }
}
