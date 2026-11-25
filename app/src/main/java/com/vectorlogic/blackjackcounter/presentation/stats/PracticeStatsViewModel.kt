package com.vectorlogic.blackjackcounter.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vectorlogic.blackjackcounter.domain.drill.DrillResult
import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import com.vectorlogic.blackjackcounter.domain.persistence.PracticeStatsStore
import com.vectorlogic.blackjackcounter.domain.stats.DrillRecord
import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistory
import com.vectorlogic.blackjackcounter.domain.stats.QuizRecord
import com.vectorlogic.blackjackcounter.presentation.quiz.QuizRoundScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Drill and quiz results across sessions. Without a store it keeps them for this launch only. */
class PracticeStatsViewModel private constructor(
    private val store: PracticeStatsStore?,
    private val clock: () -> Long
) : ViewModel() {

    private val _history = MutableStateFlow(
        store?.let { runCatching { it.load() }.getOrNull() } ?: PracticeHistory()
    )
    val history: StateFlow<PracticeHistory> = _history.asStateFlow()

    fun recordDrill(systemId: String, speed: DrillSpeed, result: DrillResult) {
        update {
            it.withDrill(
                DrillRecord(
                    finishedAtMs = clock(),
                    systemId = systemId,
                    cardCount = result.cards.size,
                    speed = speed,
                    elapsedMs = result.elapsedMs,
                    actualCount = result.actualRunningCount,
                    reportedCount = result.userReportedCount
                )
            )
        }
    }

    fun recordQuiz(systemId: String, score: QuizRoundScore) {
        if (score.questionCount == 0) return
        update {
            it.withQuiz(
                QuizRecord(
                    finishedAtMs = clock(),
                    systemId = systemId,
                    questionCount = score.questionCount,
                    correctCount = score.correctCount,
                    missedHands = score.missedHands
                )
            )
        }
    }

    fun clear() = update { PracticeHistory() }

    private fun update(transform: (PracticeHistory) -> PracticeHistory) {
        val next = transform(_history.value)
        _history.value = next
        // Like the shoe, a storage failure only costs persistence, never the result on screen.
        runCatching { store?.save(next) }
    }

    companion object {
        fun provideFactory(
            store: PracticeStatsStore? = null,
            clock: () -> Long = System::currentTimeMillis
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PracticeStatsViewModel(store, clock) as T
        }
    }
}
