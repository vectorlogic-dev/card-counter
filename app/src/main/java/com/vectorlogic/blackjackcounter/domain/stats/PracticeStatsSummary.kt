package com.vectorlogic.blackjackcounter.domain.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import kotlin.math.roundToInt

/** Drills finished and how many were exact, for one slice of the history. */
data class ExactTally(val exact: Int, val total: Int) {
    /** Whole percent, or null when there is nothing to divide. */
    val percent: Int? get() = if (total == 0) null else (exact * 100.0 / total).roundToInt()
}

data class DrillSummary(
    val all: ExactTally,
    /** The last [RECENT_WINDOW] drills, to compare with [all]. */
    val recent: ExactTally,
    /** Mean miss on the drills that weren't exact; null when every drill was exact. */
    val averageMiss: Double?,
    /** Only speeds that have been drilled, slowest first. */
    val bySpeed: Map<DrillSpeed, ExactTally>,
    /** Exact or not for the last [STREAK_LENGTH] drills, oldest first. */
    val streak: List<Boolean>,
    /** Newest first. */
    val latest: List<DrillRecord>
)

data class QuizSummary(
    val rounds: Int,
    val answers: ExactTally,
    val bestRound: QuizRecord?,
    /** Hands missed most often, with how many times; most missed first. */
    val mostMissed: List<Pair<QuizHand, Int>>,
    /** Newest first. */
    val latest: List<QuizRecord>
)

const val RECENT_WINDOW = 10
const val STREAK_LENGTH = 20
const val LATEST_COUNT = 5
const val MOST_MISSED_COUNT = 3

fun PracticeHistory.drillSummary(): DrillSummary? {
    if (drills.isEmpty()) return null
    val misses = drills.filterNot { it.isExact }
    return DrillSummary(
        all = drills.tally(),
        recent = drills.takeLast(RECENT_WINDOW).tally(),
        averageMiss = misses.takeIf { it.isNotEmpty() }?.map { it.absoluteError }?.average(),
        bySpeed = DrillSpeed.entries
            .associateWith { speed -> drills.filter { it.speed == speed }.tally() }
            .filterValues { it.total > 0 },
        streak = drills.takeLast(STREAK_LENGTH).map { it.isExact },
        latest = drills.takeLast(LATEST_COUNT).asReversed()
    )
}

fun PracticeHistory.quizSummary(): QuizSummary? {
    if (quizzes.isEmpty()) return null
    return QuizSummary(
        rounds = quizzes.size,
        answers = ExactTally(
            exact = quizzes.sumOf { it.correctCount },
            total = quizzes.sumOf { it.questionCount }
        ),
        // The highest share right; the latest such round on a tie.
        bestRound = quizzes.maxWithOrNull(
            compareBy<QuizRecord> { it.correctCount.toDouble() / it.questionCount }
                .thenBy { it.finishedAtMs }
        ),
        mostMissed = quizzes.flatMap { it.missedHands }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(MOST_MISSED_COUNT)
            .map { it.key to it.value },
        latest = quizzes.takeLast(LATEST_COUNT).asReversed()
    )
}

private fun List<DrillRecord>.tally() = ExactTally(exact = count { it.isExact }, total = size)
