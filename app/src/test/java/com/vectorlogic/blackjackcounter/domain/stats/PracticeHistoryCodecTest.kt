package com.vectorlogic.blackjackcounter.domain.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeHistoryCodecTest {

    private val drills = listOf(
        DrillRecord(1_700_000_000_000, "hi_lo", 52, DrillSpeed.FAST, 31_250, -3.0, -3.0),
        DrillRecord(1_700_000_100_000, "wong_halves", 26, DrillSpeed.SLOW, 39_000, 1.5, 2.0)
    )
    private val quizzes = listOf(
        QuizRecord(1_700_000_200_000, "hi_lo", 10, 7, listOf(QuizHand("16", "10"), QuizHand("A,8", "6"), QuizHand("10,10", "5"))),
        QuizRecord(1_700_000_300_000, "zen_count", 10, 10, emptyList())
    )

    @Test
    fun `drills and quiz rounds survive a round trip`() {
        assertEquals(drills, PracticeHistoryCodec.decodeDrills(PracticeHistoryCodec.encodeDrills(drills)))
        assertEquals(quizzes, PracticeHistoryCodec.decodeQuizzes(PracticeHistoryCodec.encodeQuizzes(quizzes)))
    }

    @Test
    fun `nothing stored decodes to nothing`() {
        assertEquals(emptyList<DrillRecord>(), PracticeHistoryCodec.decodeDrills(""))
        assertEquals(emptyList<QuizRecord>(), PracticeHistoryCodec.decodeQuizzes(""))
    }

    @Test
    fun `a damaged line is dropped and the rest kept`() {
        val text = PracticeHistoryCodec.encodeDrills(drills).lines().let { (first, second) ->
            listOf(first, "garbage", "1|hi_lo|52|WARP|1000|0|0", "1|hi_lo|52|FAST|1000|NaN|0", second)
        }.joinToString("\n")
        assertEquals(drills, PracticeHistoryCodec.decodeDrills(text))

        val quizText = listOf(
            "1|hi_lo|10|11|",          // more right than asked
            "1|hi_lo|10|9|16@",        // a hand with no upcard
            PracticeHistoryCodec.encodeQuizzes(quizzes)
        ).joinToString("\n")
        assertEquals(quizzes, PracticeHistoryCodec.decodeQuizzes(quizText))
    }
}
