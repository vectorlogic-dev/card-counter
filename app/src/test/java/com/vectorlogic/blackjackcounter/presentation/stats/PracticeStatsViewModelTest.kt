package com.vectorlogic.blackjackcounter.presentation.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillResult
import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.persistence.PracticeStatsStore
import com.vectorlogic.blackjackcounter.domain.stats.DrillRecord
import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistory
import com.vectorlogic.blackjackcounter.domain.stats.QuizHand
import com.vectorlogic.blackjackcounter.domain.stats.QuizRecord
import com.vectorlogic.blackjackcounter.presentation.quiz.QuizRoundScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class InMemoryStatsStore(var saved: PracticeHistory = PracticeHistory()) : PracticeStatsStore {
    var saves = 0
    override fun load(): PracticeHistory = saved
    override fun save(history: PracticeHistory) {
        saves += 1
        saved = history
    }
}

private class BrokenStatsStore : PracticeStatsStore {
    override fun load(): PracticeHistory = error("disk gone")
    override fun save(history: PracticeHistory) = error("disk gone")
}

class PracticeStatsViewModelTest {

    private var now = 1_000L

    private fun viewModel(store: PracticeStatsStore?) =
        PracticeStatsViewModel.provideFactory(store) { now }.create(PracticeStatsViewModel::class.java)

    private val result = DrillResult(
        actualRunningCount = 2.0,
        userReportedCount = 3.0,
        cards = List(26) { CardRank.TWO },
        elapsedMs = 26_500,
        systemDisplayName = "Hi-Lo"
    )

    @Test
    fun `a finished drill is recorded with its setup and saved`() {
        val store = InMemoryStatsStore()
        val vm = viewModel(store)
        vm.recordDrill("hi_lo", DrillSpeed.FAST, result)

        val expected = DrillRecord(1_000L, "hi_lo", 26, DrillSpeed.FAST, 26_500, 2.0, 3.0)
        assertEquals(listOf(expected), vm.history.value.drills)
        assertEquals(vm.history.value, store.saved)
    }

    @Test
    fun `a quiz round is recorded and an empty one is ignored`() {
        val store = InMemoryStatsStore()
        val vm = viewModel(store)
        now = 2_000L
        vm.recordQuiz("hi_lo", QuizRoundScore(10, 9, listOf(QuizHand("16", "10"))))
        vm.recordQuiz("hi_lo", QuizRoundScore(0, 0, emptyList()))

        assertEquals(
            listOf(QuizRecord(2_000L, "hi_lo", 10, 9, listOf(QuizHand("16", "10")))),
            vm.history.value.quizzes
        )
        assertEquals(1, store.saves)
    }

    @Test
    fun `history is loaded at start and clear wipes it from the store`() {
        val saved = PracticeHistory(drills = listOf(DrillRecord(5, "ko", 52, DrillSpeed.SLOW, 80_000, 0.0, 0.0)))
        val store = InMemoryStatsStore(saved)
        val vm = viewModel(store)
        assertEquals(saved, vm.history.value)

        vm.clear()
        assertTrue(vm.history.value.isEmpty)
        assertTrue(store.saved.isEmpty)
    }

    @Test
    fun `a failing store doesn't lose results in memory`() {
        val vm = viewModel(BrokenStatsStore())
        vm.recordDrill("hi_lo", DrillSpeed.MEDIUM, result)
        assertEquals(1, vm.history.value.drills.size)
    }
}
