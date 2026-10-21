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
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
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

        viewModel.setCountingSystem(HiOptIICountingSystem.id)

        val updated = viewModel.uiState.value
        assertTrue(updated.selectedSystemRecommendsAceSideCount)
        assertTrue(updated.selectedSystemUsesTrueCount)
    }

    @Test
    fun `available systems expose advanced systems without pricing metadata`() {
        val viewModel = createViewModel()
        val hiLo = viewModel.availableSystems.first { it.id == HiLoCountingSystem.id }
        val zen = viewModel.availableSystems.first { it.id == ZenCountSystem.id }
        val wong = viewModel.availableSystems.first { it.id == WongHalvesSystem.id }

        assertEquals(1, hiLo.level)
        assertEquals(2, zen.level)
        assertEquals(3, wong.level)
    }

    @Test
    fun `all systems can be selected freely`() {
        val viewModel = createViewModel()

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

        val chart = viewModel.buildStrategyChartModel()
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

        val chart = viewModel.buildStrategyChartModel()
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

        val chart = viewModel.buildStrategyChartModel()
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
    fun `changing total decks preserves cards drawn and updates decks remaining`() {
        val viewModel = createViewModel()
        repeat(10) {
            viewModel.onCardSeen(CardRank.TWO)
        }

        viewModel.setTotalDecks(4)

        val updated = viewModel.uiState.value
        assertEquals(4, updated.totalDecks)
        assertEquals(10, updated.cardsDrawn)
        assertEquals(208, updated.totalCards)
        assertEquals(198, updated.cardsLeft)
        assertEquals(198.0 / 52.0, updated.decksRemaining, 0.0001)
    }

    @Test
    fun `deviation trigger compares the displayed true count with the index`() {
        val viewModel = createViewModel()
        repeat(2) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(0.51)
        var chart = viewModel.buildStrategyChartModel()
        var hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        var dealerTenCell = hardTenRow.cells[8]
        assertEquals("H", dealerTenCell.action)
        assertFalse(dealerTenCell.isDeviation)

        viewModel.setDecksRemaining(0.49)
        chart = viewModel.buildStrategyChartModel()
        hardTenRow = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "10" }
        dealerTenCell = hardTenRow.cells[8]
        assertTrue(dealerTenCell.action.startsWith("D"))
        assertTrue(dealerTenCell.isDeviation)
    }

    @Test
    fun `undo restores only the penetration a clamped card actually consumed`() {
        val viewModel = createViewModel()
        viewModel.setDecksRemaining(0.26)

        viewModel.onCardSeen(CardRank.TWO) // partially clamped: 0.26 -> 0.25
        viewModel.onCardSeen(CardRank.TWO) // fully clamped: stays at 0.25
        assertEquals(0.25, viewModel.uiState.value.decksRemaining, 0.0001)

        viewModel.undoLastCard()
        assertEquals(0.25, viewModel.uiState.value.decksRemaining, 0.0001)

        viewModel.undoLastCard()
        assertEquals(0.26, viewModel.uiState.value.decksRemaining, 0.0001)
        assertEquals(0.0, viewModel.uiState.value.runningCount, 0.0001)
    }

    @Test
    fun `undo of an unclamped card restores one card of penetration`() {
        val viewModel = createViewModel()
        val initialDecksRemaining = viewModel.uiState.value.decksRemaining

        viewModel.onCardSeen(CardRank.KING)
        viewModel.undoLastCard()

        assertEquals(initialDecksRemaining, viewModel.uiState.value.decksRemaining, 0.0001)
    }

    @Test
    fun `zen bet ramp uses converted true count thresholds`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(ZenCountSystem.id)
        repeat(3) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)

        // Zen TC 3 is about Hi-Lo TC 1.7: 2 units (Zen threshold 1.8), not Hi-Lo's 8 units at TC 3.
        var updated = viewModel.uiState.value
        assertEquals(3.0, updated.betSizingCountValue, 0.0001)
        assertEquals(2, updated.recommendedBetUnits)

        repeat(2) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)
        // Zen TC 5 is about Hi-Lo TC 2.8: 4 units (8 units starts at Zen 5.4).
        updated = viewModel.uiState.value
        assertEquals(5.0, updated.betSizingCountValue, 0.0001)
        assertEquals(4, updated.recommendedBetUnits)
    }

    @Test
    fun `omega ii at true count 3 point 9 bets like hi lo at 2 point 2`() {
        val omega = createViewModel()
        omega.setCountingSystem(OmegaIICountingSystem.id)
        repeat(4) {
            omega.onCardSeen(CardRank.FIVE) // +2 each, RC 8
        }
        omega.setDecksRemaining(2.05) // 8 / 2.05 = 3.90

        val omegaState = omega.uiState.value
        assertEquals(3.9, omegaState.trueCount, 0.0001)
        assertEquals(3.9, omegaState.betSizingCountValue, 0.0001)
        // 3.9 / 1.789 = Hi-Lo 2.2: 4 units. Rounded native thresholds plus truncation gave 2.
        assertEquals(4, omegaState.recommendedBetUnits)

        val hiLo = createViewModel()
        repeat(2) {
            hiLo.onCardSeen(CardRank.FIVE)
        }
        hiLo.setDecksRemaining(0.87) // 2 / 0.87 = 2.30
        assertEquals(2.3, hiLo.uiState.value.trueCount, 0.0001)
        assertEquals(hiLo.uiState.value.recommendedBetUnits, omegaState.recommendedBetUnits)

        // Hi-Lo 12 vs 3 stand at +2 is Omega II +3.6, so it applies at 3.9 too.
        val twelve = omega.buildStrategyChartModel().sections
            .first { it.title == "Hard Totals" }.rows.first { it.handLabel == "12" }
        assertEquals("S", twelve.cells[1].action)
        assertTrue(twelve.cells[1].isDeviation)
        assertTrue(omegaState.strategyDeviationRows.any {
            it.playLabel == "12 vs 3" && it.indexLabel == "Stand at TC +3.6"
        })
    }

    @Test
    fun `omega ii bet ramp steps up exactly at the threshold it shows`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(OmegaIICountingSystem.id)
        repeat(4) {
            viewModel.onCardSeen(CardRank.FIVE)
        }
        viewModel.onCardSeen(CardRank.TWO) // RC 9

        viewModel.setDecksRemaining(2.5) // 3.6
        assertEquals(3.6, viewModel.uiState.value.betSizingCountValue, 0.0001)
        assertEquals(4, viewModel.uiState.value.recommendedBetUnits)

        viewModel.setDecksRemaining(2.54) // 3.54 shows as 3.5
        assertEquals(3.5, viewModel.uiState.value.betSizingCountValue, 0.0001)
        assertEquals(2, viewModel.uiState.value.recommendedBetUnits)
    }

    @Test
    fun `slightly negative true count does not trigger index zero plays`() {
        val viewModel = createViewModel()
        viewModel.onCardSeen(CardRank.KING) // RC -1
        viewModel.setDecksRemaining(1.11) // -0.90

        assertEquals(-0.9, viewModel.uiState.value.trueCount, 0.0001)
        var hard = viewModel.buildStrategyChartModel().sections.first { it.title == "Hard Totals" }
        assertEquals("H", hard.rows.first { it.handLabel == "16" }.cells[8].action)
        assertEquals("H", hard.rows.first { it.handLabel == "12" }.cells[2].action)

        viewModel.setSurrenderRule(SurrenderRule.LATE)
        hard = viewModel.buildStrategyChartModel().sections.first { it.title == "Hard Totals" }
        assertEquals("H", hard.rows.first { it.handLabel == "15" }.cells[8].action)

        viewModel.undoLastCard() // TC 0
        hard = viewModel.buildStrategyChartModel().sections.first { it.title == "Hard Totals" }
        assertEquals("Rh", hard.rows.first { it.handLabel == "15" }.cells[8].action)
        viewModel.setSurrenderRule(SurrenderRule.NONE)
        hard = viewModel.buildStrategyChartModel().sections.first { it.title == "Hard Totals" }
        assertEquals("S", hard.rows.first { it.handLabel == "16" }.cells[8].action)
        assertEquals("S", hard.rows.first { it.handLabel == "12" }.cells[2].action)
    }

    @Test
    fun `zen edge estimate is lower than an unscaled native true count would give`() {
        val hiLo = createViewModel()
        val zen = createViewModel()
        zen.setCountingSystem(ZenCountSystem.id)
        repeat(4) {
            hiLo.onCardSeen(CardRank.TWO)
            zen.onCardSeen(CardRank.FOUR)
        }
        hiLo.setDecksRemaining(1.0)
        zen.setDecksRemaining(1.0)

        // Hi-Lo TC 4 vs Zen TC 8 (~Hi-Lo 4.5): edges should be comparable, not doubled.
        val hiLoEdge = hiLo.uiState.value.estimatedPlayerEdgePercent
        val zenEdge = zen.uiState.value.estimatedPlayerEdgePercent
        assertEquals(8.0, zen.uiState.value.trueCount, 0.0001)
        assertEquals(hiLoEdge, zenEdge, 0.35)
    }

    @Test
    fun `cheat sheet deviation rows reflect the selected system`() {
        val viewModel = createViewModel()
        viewModel.setCountingSystem(OmegaIICountingSystem.id)

        val rows = viewModel.uiState.value.strategyDeviationRows
        assertTrue(rows.any { it.playLabel == "10 vs 10" && it.indexLabel == "Double at TC +7.2" })
        assertTrue(rows.any { it.playLabel == "Insurance vs A" && it.indexLabel == "Take at TC +5.4" })
    }

    @Test
    fun `surrender table keeps surrender on 16 vs 10 at positive counts`() {
        val viewModel = createViewModel()
        viewModel.setSurrenderRule(SurrenderRule.LATE)
        repeat(4) {
            viewModel.onCardSeen(CardRank.TWO)
        }
        viewModel.setDecksRemaining(1.0)

        val chart = viewModel.buildStrategyChartModel()
        val sixteen = chart.sections.first { it.title == "Hard Totals" }.rows.first { it.handLabel == "16" }
        assertEquals("Rh", sixteen.cells[8].action)
        assertTrue(viewModel.uiState.value.strategyDeviationRows.any {
            it.playLabel == "16 vs 10" && it.indexLabel == "Surrender (any TC)"
        })
    }

    private fun createViewModel(): CounterViewModel {
        return CounterViewModel.provideFactory().create(CounterViewModel::class.java)
    }
}
