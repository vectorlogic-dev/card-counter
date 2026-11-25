package com.vectorlogic.blackjackcounter.presentation.quiz

import com.vectorlogic.blackjackcounter.domain.model.CounterCalculations
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.presentation.counter.CounterStrategyEngine
import com.vectorlogic.blackjackcounter.presentation.counter.DeviationQuizItem
import com.vectorlogic.blackjackcounter.presentation.counter.DeviationTrigger
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyPlay
import kotlin.random.Random

/** One question: a count-dependent hand ([itemIndex] into [DeviationQuiz.items]) at a true count. */
data class QuizQuestion(val itemIndex: Int, val trueCount: Double)

/**
 * A deviation quiz for one set of table rules and one counting system. Answers come from the
 * same engine as the Strategy Chart, so the quiz never disagrees with the chart.
 *
 * @param systemName the system whose true counts the questions use. Running-count systems (KO,
 *   ReKO) have no true-count indices, so they are quizzed on Hi-Lo.
 */
class DeviationQuiz(
    private val state: CounterSessionState,
    private val trueCountScale: Double,
    val systemName: String,
    val usesSelectedSystem: Boolean
) {
    val items: List<DeviationQuizItem> =
        CounterStrategyEngine.buildDeviationQuizItems(state, trueCountScale)

    private val surrenderAllowed = state.tableRules.surrenderRule != SurrenderRule.NONE

    fun item(question: QuizQuestion): DeviationQuizItem = items[question.itemIndex]

    fun correctPlay(question: QuizQuestion): StrategyPlay =
        CounterStrategyEngine.playFor(state, item(question), question.trueCount, trueCountScale)

    /** Whether the count in [question] reaches the hand's index. */
    fun indexApplies(question: QuizQuestion): Boolean = item(question).trigger.isMetBy(question.trueCount)

    /** The plays offered as answers: split only for pairs, surrender only where the table offers it. */
    fun choices(question: QuizQuestion): List<StrategyPlay> = StrategyPlay.entries.filter { play ->
        when (play) {
            StrategyPlay.SPLIT -> item(question).isPair
            StrategyPlay.SURRENDER -> surrenderAllowed
            else -> true
        }
    }

    fun newRound(count: Int = QUESTIONS_PER_ROUND, random: Random = Random.Default): List<QuizQuestion> =
        generateQuestions(items, count, random)

    companion object {
        const val QUESTIONS_PER_ROUND = 10

        /**
         * Every hand once (shuffled) before any repeats. Each question lands on the index or one
         * or two counts past it about half the time, and one to three counts short of it
         * otherwise, so knowing the play isn't enough: you also need the number.
         */
        fun generateQuestions(
            items: List<DeviationQuizItem>,
            count: Int,
            random: Random = Random.Default
        ): List<QuizQuestion> {
            if (items.isEmpty()) return emptyList()
            return generateSequence { items.indices.shuffled(random) }
                .flatten()
                .take(count)
                .map { index ->
                    QuizQuestion(
                        itemIndex = index,
                        trueCount = questionTrueCount(
                            trigger = items[index].trigger,
                            applies = random.nextBoolean(),
                            step = random.nextInt(3)
                        )
                    )
                }
                .toList()
        }

        /** A true count [step] (0-2) whole counts inside the index, or 1-3 counts outside it. */
        fun questionTrueCount(trigger: DeviationTrigger, applies: Boolean, step: Int): Double {
            val index = trigger.threshold
            val count = if (trigger.atOrAbove == applies) index + step else index - 1 - step
            return CounterCalculations.roundToOneDecimal(count)
        }
    }
}

private val DeviationQuizItem.trigger: DeviationTrigger
    get() = checkNotNull(row.trigger) { "Quiz items always have a count trigger" }
