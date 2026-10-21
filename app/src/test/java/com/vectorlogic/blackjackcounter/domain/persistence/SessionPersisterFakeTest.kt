package com.vectorlogic.blackjackcounter.domain.persistence

import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.presentation.counter.CounterViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

private class InMemoryPersister(var initial: CounterSessionState? = null) : SessionPersister {
    var lastSaved: CounterSessionState? = null
    override fun load(): CounterSessionState? = initial
    override fun save(state: CounterSessionState) { lastSaved = state }
}

class SessionPersisterFakeTest {

    @Test
    fun `ViewModel restores session state from persister on init`() {
        val saved = CounterSessionState(
            runningCount = 5.0,
            totalDecks = 6,
            decksRemaining = 4.5,
            acesSeen = 0,
            selectedCountingSystemId = HiLoCountingSystem.id,
            seenCards = listOf(
                SeenCard(CardRank.TWO, deltaApplied = 1.0, countedAce = false),
                SeenCard(CardRank.ACE, deltaApplied = -1.0, countedAce = false)
            ),
            tableRules = TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT)
        )
        val persister = InMemoryPersister(initial = saved)

        val vm = CounterViewModel.provideFactory(persister)
            .create(CounterViewModel::class.java)

        val state = vm.uiState.value
        assertEquals(5.0, state.runningCount, 0.0001)
        assertEquals(6, state.totalDecks)
        assertEquals(2, state.seenCards.size)
        assertEquals(DealerSoft17Rule.HIT, state.tableRules.dealerSoft17Rule)
    }

    @Test
    fun `ViewModel saves to persister after a card is seen`() {
        val persister = InMemoryPersister()
        val vm = CounterViewModel.provideFactory(persister)
            .create(CounterViewModel::class.java)

        vm.onCardSeen(CardRank.TWO)

        val saved = persister.lastSaved
        assertEquals(1.0, saved!!.runningCount, 0.0001)
        assertEquals(1, saved.seenCards.size)
        assertEquals(CardRank.TWO, saved.seenCards.first().rank)
    }
}
