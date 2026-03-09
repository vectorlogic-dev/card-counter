package com.yourname.blackjackcounter.presentation.counter

import com.yourname.blackjackcounter.domain.model.CardRank
import org.junit.Assert.assertEquals
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
    fun `resetCount clears running and true count while keeping shoe position`() {
        val viewModel = CounterViewModel()

        repeat(5) {
            viewModel.onCardSeen(CardRank.FIVE)
        }
        val penetrationBeforeReset = viewModel.uiState.value.deckPenetrationPercent

        viewModel.resetCount()

        val updated = viewModel.uiState.value
        assertEquals(0.0, updated.runningCount, 0.0001)
        assertEquals(0.0, updated.trueCount, 0.0001)
        assertEquals(penetrationBeforeReset, updated.deckPenetrationPercent, 0.0001)
    }
}
