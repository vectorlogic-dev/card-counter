package com.yourname.blackjackcounter.presentation.counter

import com.yourname.blackjackcounter.domain.model.CardRank
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
    fun `onCardSeen ace increments ace side count and expected aces`() {
        val viewModel = CounterViewModel()

        viewModel.onCardSeen(CardRank.ACE)

        val updated = viewModel.uiState.value
        assertEquals(1, updated.acesSeen)
        assertEquals(23.9, updated.acesExpected, 0.0001)
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
        assertTrue(updated.strategyDeviationRows.isEmpty())
    }

    @Test
    fun `hi lo exposes common 16 vs 10 deviation`() {
        val viewModel = CounterViewModel()

        viewModel.setCountingSystem("hi_lo")

        val updated = viewModel.uiState.value
        assertTrue(updated.strategyDeviationRows.any { it.playLabel == "16 vs 10" && it.indexLabel.contains("TC 0+") })
    }
}
