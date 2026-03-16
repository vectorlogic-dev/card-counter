package com.vectorlogic.blackjackcounter.domain.model
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

object CounterCalculations {

    fun roundToOneDecimal(value: Double): Double {
        return round(value * 10.0) / 10.0
    }

    fun calculateRawTrueCount(
        runningCount: Double,
        decksRemaining: Double,
        usesTrueCount: Boolean,
        minDecksRemaining: Double
    ): Double {
        if (!usesTrueCount) return 0.0

        val safeDecksRemaining = decksRemaining.coerceAtLeast(minDecksRemaining)
        return runningCount / safeDecksRemaining
    }

    fun calculateTrueCount(
        runningCount: Double,
        decksRemaining: Double,
        usesTrueCount: Boolean,
        minDecksRemaining: Double
    ): Double {
        val raw = calculateRawTrueCount(
            runningCount = runningCount,
            decksRemaining = decksRemaining,
            usesTrueCount = usesTrueCount,
            minDecksRemaining = minDecksRemaining
        )
        return roundToOneDecimal(raw)
    }

    fun integerizeTrueCount(
        rawTrueCount: Double,
        method: TrueCountIntegerization
    ): Int {
        return when (method) {
            TrueCountIntegerization.TRUNCATE -> rawTrueCount.toInt()
            TrueCountIntegerization.FLOOR -> floor(rawTrueCount).toInt()
            TrueCountIntegerization.ROUND -> rawTrueCount.roundToInt()
        }
    }

    fun calculateDeckPenetrationPercent(
        totalDecks: Int,
        decksRemaining: Double
    ): Double {
        if (totalDecks <= 0) return 0.0
        val dealtDecks = (totalDecks - decksRemaining).coerceAtLeast(0.0)
        return roundToOneDecimal((dealtDecks / totalDecks.toDouble()) * 100.0)
    }

    fun calculateExpectedAces(cardsDrawn: Int): Double {
        val safeCardsDrawn = cardsDrawn.coerceAtLeast(0)
        val expected = (safeCardsDrawn.toDouble() / CARDS_PER_DECK) * ACES_PER_DECK
        return roundToOneDecimal(expected)
    }

    fun calculateEstimatedPlayerEdgePercent(
        trueCount: Double,
        usesTrueCount: Boolean,
        tableRules: TableRules,
        totalDecks: Int
    ): Double {
        if (!usesTrueCount) return 0.0

        val houseEdge = estimateHouseEdgePercent(
            tableRules = tableRules,
            totalDecks = totalDecks
        )
        val countAdjustment = trueCount * PLAYER_EDGE_PER_TRUE_COUNT
        return roundToOneDecimal((-houseEdge) + countAdjustment)
    }

    fun estimateHouseEdgePercent(
        tableRules: TableRules,
        totalDecks: Int = DEFAULT_TOTAL_DECKS_FOR_BASELINE
    ): Double {
        // Baseline and rule adjustments are calibrated from Wizard of Odds
        // "Blackjack Rule Variations" table (8-deck baseline), with
        // linearized approximations for options this app models as discrete toggles.
        var houseEdge = BASE_HOUSE_EDGE_PERCENT
        houseEdge += deckCountAdjustment(totalDecks)

        houseEdge += when (tableRules.dealerSoft17Rule) {
            DealerSoft17Rule.STAND -> 0.0
            DealerSoft17Rule.HIT -> 0.22
        }

        houseEdge += when (tableRules.dealerPeekRule) {
            DealerPeekRule.ACE_AND_TEN -> 0.0
            DealerPeekRule.ACE_ONLY -> 0.10
            DealerPeekRule.NO_PEEK -> 0.11
        }

        houseEdge += when (tableRules.blackjackPayoutRule) {
            BlackjackPayoutRule.THREE_TO_TWO -> 0.0
            BlackjackPayoutRule.SIX_TO_FIVE -> 1.39
        }

        houseEdge += when (tableRules.doubleDownRule) {
            DoubleDownRule.ANY_TWO_CARDS -> 0.0
            DoubleDownRule.NINE_TO_ELEVEN -> 0.09
            DoubleDownRule.TEN_TO_ELEVEN -> 0.18
        }

        if (!tableRules.doubleAfterSplit) {
            houseEdge += 0.14
        }

        houseEdge += when (tableRules.surrenderRule) {
            SurrenderRule.NONE -> 0.0
            SurrenderRule.LATE -> -0.07
            SurrenderRule.EARLY -> -0.63
        }

        if (tableRules.resplitAces) {
            houseEdge += -0.08
        }
        if (tableRules.hitSplitAces) {
            houseEdge += -0.19
        }
        if (tableRules.sixCardCharlieAutoWin) {
            houseEdge += -0.16
        }

        houseEdge += when (tableRules.maxSplitHands.coerceIn(2, 6)) {
            2 -> 0.10
            3 -> 0.01
            4 -> 0.0
            5 -> -0.01
            else -> -0.02
        }

        return roundToOneDecimal(houseEdge)
    }

    fun calculateRecommendedBetUnits(
        countValue: Double,
        ramp: List<CountRampStep>,
        defaultUnits: Int = 1
    ): Int {
        var result = defaultUnits
        ramp.sortedBy { it.minCountInclusive }.forEach { step ->
            if (countValue >= step.minCountInclusive) {
                result = step.units
            }
        }
        return result
    }

    private fun deckCountAdjustment(totalDecks: Int): Double {
        return when (totalDecks.coerceIn(1, 8)) {
            1 -> -0.48
            2 -> -0.19
            3 -> -0.10
            4 -> -0.06
            5 -> -0.03
            6 -> -0.02
            7 -> -0.01
            else -> 0.0
        }
    }

    private const val ACES_PER_DECK = 4.0
    private const val CARDS_PER_DECK = 52.0
    private const val BASE_HOUSE_EDGE_PERCENT = 0.50
    private const val DEFAULT_TOTAL_DECKS_FOR_BASELINE = 8
    private const val PLAYER_EDGE_PER_TRUE_COUNT = 0.5
}
