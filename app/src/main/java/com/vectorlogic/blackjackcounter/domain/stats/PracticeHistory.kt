package com.vectorlogic.blackjackcounter.domain.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import kotlin.math.abs

/** One finished counting drill. */
data class DrillRecord(
    val finishedAtMs: Long,
    val systemId: String,
    val cardCount: Int,
    val speed: DrillSpeed,
    val elapsedMs: Long,
    val actualCount: Double,
    val reportedCount: Double
) {
    val isExact: Boolean get() = absoluteError < EXACT_TOLERANCE
    val absoluteError: Double get() = abs(actualCount - reportedCount)

    private companion object {
        const val EXACT_TOLERANCE = 0.0001
    }
}

/** A quiz hand by its chart notation: [hand] such as 16 or A,7 or 8,8, and the dealer's upcard. */
data class QuizHand(val hand: String, val dealer: String)

/** One finished round of the deviation quiz. A hand missed twice in a round is listed twice. */
data class QuizRecord(
    val finishedAtMs: Long,
    val systemId: String,
    val questionCount: Int,
    val correctCount: Int,
    val missedHands: List<QuizHand>
)

/** Every drill and quiz round kept on the device, oldest first. */
data class PracticeHistory(
    val drills: List<DrillRecord> = emptyList(),
    val quizzes: List<QuizRecord> = emptyList()
) {
    fun withDrill(record: DrillRecord): PracticeHistory =
        copy(drills = (drills + record).takeLast(MAX_RECORDS))

    fun withQuiz(record: QuizRecord): PracticeHistory =
        copy(quizzes = (quizzes + record).takeLast(MAX_RECORDS))

    val isEmpty: Boolean get() = drills.isEmpty() && quizzes.isEmpty()

    companion object {
        /** Per kind. Enough for months of practice and still a few dozen KB on disk. */
        const val MAX_RECORDS = 500
    }
}
