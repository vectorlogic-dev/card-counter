package com.vectorlogic.blackjackcounter.domain.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PracticeStatsSummaryTest {

    private fun drill(at: Long, exact: Boolean, speed: DrillSpeed = DrillSpeed.MEDIUM, miss: Double = 2.0) =
        DrillRecord(at, "hi_lo", 26, speed, 26_000, actualCount = 4.0, reportedCount = if (exact) 4.0 else 4.0 + miss)

    private fun round(at: Long, correct: Int, vararg missed: QuizHand) =
        QuizRecord(at, "hi_lo", 10, correct, missed.toList())

    @Test
    fun `an empty history has no summaries`() {
        assertNull(PracticeHistory().drillSummary())
        assertNull(PracticeHistory().quizSummary())
    }

    @Test
    fun `drill summary counts exact drills overall, recently and by speed`() {
        // 15 drills: the first five missed at Slow, then ten at Fast of which the last eight are exact.
        val drills = (0L until 5).map { drill(it, exact = false, speed = DrillSpeed.SLOW, miss = it + 1.0) } +
            (5L until 15).map { drill(it, exact = it >= 7, speed = DrillSpeed.FAST, miss = 1.0) }
        val summary = PracticeHistory(drills = drills).drillSummary()!!

        assertEquals(ExactTally(exact = 8, total = 15), summary.all)
        assertEquals(53, summary.all.percent)
        assertEquals(ExactTally(exact = 8, total = 10), summary.recent)
        assertEquals(
            mapOf(DrillSpeed.SLOW to ExactTally(0, 5), DrillSpeed.FAST to ExactTally(8, 10)),
            summary.bySpeed
        )
        // Misses of 1..5 at Slow and two of 1 at Fast.
        assertEquals((1.0 + 2 + 3 + 4 + 5 + 1 + 1) / 7, summary.averageMiss!!, 1e-9)
        assertEquals(drills.map { it.isExact }, summary.streak)
        assertEquals(listOf(14L, 13L, 12L, 11L, 10L), summary.latest.map { it.finishedAtMs })
    }

    @Test
    fun `the streak keeps only the newest drills and average miss is empty when all are exact`() {
        val drills = (0L until 30).map { drill(it, exact = true) }
        val summary = PracticeHistory(drills = drills).drillSummary()!!
        assertEquals(STREAK_LENGTH, summary.streak.size)
        assertNull(summary.averageMiss)
        assertEquals(100, summary.recent.percent)
    }

    @Test
    fun `quiz summary totals answers, finds the best round and the most missed hands`() {
        val sixteenVsTen = QuizHand("16", "10")
        val twelveVsThree = QuizHand("12", "3")
        val elevenVsAce = QuizHand("11", "A")
        val pairOfTens = QuizHand("10,10", "6")
        val history = PracticeHistory(
            quizzes = listOf(
                round(1, 7, sixteenVsTen, twelveVsThree, sixteenVsTen),
                round(2, 9, sixteenVsTen),
                round(3, 8, twelveVsThree, elevenVsAce),
                round(4, 9, pairOfTens)
            )
        )
        val summary = history.quizSummary()!!

        assertEquals(4, summary.rounds)
        assertEquals(ExactTally(exact = 33, total = 40), summary.answers)
        // Rounds 2 and 4 tie on 9/10; the later one counts as best.
        assertEquals(4L, summary.bestRound!!.finishedAtMs)
        assertEquals(
            listOf(sixteenVsTen to 3, twelveVsThree to 2, elevenVsAce to 1),
            summary.mostMissed
        )
        assertEquals(listOf(4L, 3L, 2L, 1L), summary.latest.map { it.finishedAtMs })
    }

    @Test
    fun `history keeps only the newest records`() {
        val history = (0L until PracticeHistory.MAX_RECORDS + 5L).fold(PracticeHistory()) { h, at ->
            h.withDrill(drill(at, exact = true)).withQuiz(round(at, 10))
        }
        assertEquals(PracticeHistory.MAX_RECORDS, history.drills.size)
        assertEquals(PracticeHistory.MAX_RECORDS, history.quizzes.size)
        assertEquals(5L, history.drills.first().finishedAtMs)
        assertEquals(PracticeHistory.MAX_RECORDS + 4L, history.quizzes.last().finishedAtMs)
    }
}
