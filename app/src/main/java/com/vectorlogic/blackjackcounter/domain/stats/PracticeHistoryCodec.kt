package com.vectorlogic.blackjackcounter.domain.stats

import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed

/**
 * Plain-text form of [PracticeHistory] for storage: one record per line, fields split by '|'.
 * Hand notation already uses ',' (A,7), so a quiz's missed hands are split by ';' and each hand
 * from its upcard by '@'. A line that doesn't parse is dropped rather than losing the rest.
 */
object PracticeHistoryCodec {
    private const val FIELD = "|"
    private const val LIST = ";"
    private const val PAIR = "@"

    fun encodeDrills(drills: List<DrillRecord>): String = drills.joinToString("\n") { drill ->
        listOf(
            drill.finishedAtMs,
            drill.systemId,
            drill.cardCount,
            drill.speed.name,
            drill.elapsedMs,
            drill.actualCount,
            drill.reportedCount
        ).joinToString(FIELD)
    }

    fun decodeDrills(text: String): List<DrillRecord> = text.lineSequence().mapNotNull { line ->
        val fields = line.split(FIELD)
        if (fields.size != 7) return@mapNotNull null
        DrillRecord(
            finishedAtMs = fields[0].toLongOrNull() ?: return@mapNotNull null,
            systemId = fields[1].ifBlank { return@mapNotNull null },
            cardCount = fields[2].toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null,
            speed = DrillSpeed.entries.firstOrNull { it.name == fields[3] } ?: return@mapNotNull null,
            elapsedMs = fields[4].toLongOrNull()?.coerceAtLeast(0L) ?: return@mapNotNull null,
            actualCount = fields[5].toFiniteDouble() ?: return@mapNotNull null,
            reportedCount = fields[6].toFiniteDouble() ?: return@mapNotNull null
        )
    }.toList()

    fun encodeQuizzes(quizzes: List<QuizRecord>): String = quizzes.joinToString("\n") { quiz ->
        listOf(
            quiz.finishedAtMs,
            quiz.systemId,
            quiz.questionCount,
            quiz.correctCount,
            quiz.missedHands.joinToString(LIST) { it.hand + PAIR + it.dealer }
        ).joinToString(FIELD)
    }

    fun decodeQuizzes(text: String): List<QuizRecord> = text.lineSequence().mapNotNull { line ->
        val fields = line.split(FIELD)
        if (fields.size != 5) return@mapNotNull null
        val questions = fields[2].toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
        val correct = fields[3].toIntOrNull()?.takeIf { it in 0..questions } ?: return@mapNotNull null
        val missed = fields[4].split(LIST).filter { it.isNotEmpty() }.map { pair ->
            val parts = pair.split(PAIR)
            if (parts.size != 2 || parts.any { it.isBlank() }) return@mapNotNull null
            QuizHand(hand = parts[0], dealer = parts[1])
        }
        QuizRecord(
            finishedAtMs = fields[0].toLongOrNull() ?: return@mapNotNull null,
            systemId = fields[1].ifBlank { return@mapNotNull null },
            questionCount = questions,
            correctCount = correct,
            missedHands = missed
        )
    }.toList()

    private fun String.toFiniteDouble(): Double? = toDoubleOrNull()?.takeIf { it.isFinite() }
}
