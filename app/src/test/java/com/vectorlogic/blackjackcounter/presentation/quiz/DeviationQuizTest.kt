package com.vectorlogic.blackjackcounter.presentation.quiz

import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.TrueCountScaling
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.presentation.counter.CounterStrategyEngine
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyPlay
import com.vectorlogic.blackjackcounter.presentation.text
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviationQuizTest {

    private val ruleSets = listOf(
        TableRules(),
        TableRules(surrenderRule = SurrenderRule.LATE),
        TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT, surrenderRule = SurrenderRule.LATE),
        TableRules(dealerPeekRule = DealerPeekRule.ACE_ONLY),
        TableRules(doubleDownRule = DoubleDownRule.TEN_TO_ELEVEN)
    )
    private val scales = listOf(
        1.0,
        TrueCountScaling.hiLoEquivalentScale(ZenCountSystem),
        TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem)
    )

    private fun quiz(rules: TableRules = TableRules(), scale: Double = 1.0) = DeviationQuiz(
        state = CounterSessionState(tableRules = rules),
        trueCountScale = scale,
        systemName = "Hi-Lo",
        usesSelectedSystem = true
    )

    @Test
    fun `the answer is the deviation exactly when the count reaches the index`() {
        ruleSets.forEach { rules ->
            scales.forEach { scale ->
                val quiz = quiz(rules, scale)
                assertTrue(quiz.items.isNotEmpty())
                quiz.items.forEachIndexed { index, item ->
                    val trigger = item.row.trigger!!
                    listOf(true, false).forEach { applies ->
                        (0..2).forEach { step ->
                            val question = QuizQuestion(
                                itemIndex = index,
                                trueCount = DeviationQuiz.questionTrueCount(trigger, applies, step)
                            )
                            val context = "${item.row.playLabel.text} at TC ${question.trueCount}, scale $scale, $rules"
                            assertEquals(context, applies, quiz.indexApplies(question))
                            assertEquals(context, applies, quiz.correctPlay(question) == item.deviationPlay)
                            assertTrue(context, quiz.correctPlay(question) in quiz.choices(question))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `quiz hands are the cheat sheet rows that depend on the count`() {
        ruleSets.forEach { rules ->
            val state = CounterSessionState(tableRules = rules)
            val countRows = CounterStrategyEngine.buildStrategyDeviationRows(
                state = state,
                usesTrueCount = true,
                keyCount = null,
                pivot = null,
                trueCountScale = 1.0
            ).filter { it.trigger != null && it.playLabel.text != "Insurance vs A" }
            assertEquals(rules.toString(), countRows, quiz(rules).items.map { it.row })
        }
    }

    @Test
    fun `rules that block a deviation keep it out of the quiz`() {
        val noPeekOnTen = quiz(TableRules(dealerPeekRule = DealerPeekRule.ACE_ONLY))
        assertFalse(noPeekOnTen.items.any { it.handLabel == "10" && it.dealerLabel == "10" })
        val h17 = quiz(TableRules(dealerSoft17Rule = DealerSoft17Rule.HIT))
        assertFalse(h17.items.any { it.handLabel == "11" && it.dealerLabel == "A" })
    }

    @Test
    fun `every hand comes up once before any repeats`() {
        val items = quiz().items
        val questions = DeviationQuiz.generateQuestions(items, count = items.size + 3, random = Random(7))
        assertEquals(items.size + 3, questions.size)
        assertEquals(items.indices.toSet(), questions.take(items.size).map { it.itemIndex }.toSet())
    }

    @Test
    fun `questions are seeded reproducibly and cover both sides of the index`() {
        val quiz = quiz()
        val round = quiz.newRound(count = 200, random = Random(42))
        assertEquals(round, quiz.newRound(count = 200, random = Random(42)))
        assertTrue(round.any { quiz.indexApplies(it) })
        assertTrue(round.any { !quiz.indexApplies(it) })
    }

    @Test
    fun `split is offered only for pairs and surrender only when the table allows it`() {
        val noSurrender = quiz()
        noSurrender.items.indices.map { QuizQuestion(it, 0.0) }.forEach { question ->
            val choices = noSurrender.choices(question)
            assertEquals(noSurrender.item(question).isPair, StrategyPlay.SPLIT in choices)
            assertFalse(StrategyPlay.SURRENDER in choices)
        }
        val late = quiz(TableRules(surrenderRule = SurrenderRule.LATE))
        assertTrue(StrategyPlay.SURRENDER in late.choices(QuizQuestion(0, 0.0)))
    }
}
