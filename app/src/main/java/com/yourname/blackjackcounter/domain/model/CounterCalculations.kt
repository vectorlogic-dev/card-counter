package com.yourname.blackjackcounter.domain.model

import kotlin.math.round

object CounterCalculations {

    fun roundToOneDecimal(value: Double): Double {
        return round(value * 10.0) / 10.0
    }

    fun calculateTrueCount(
        runningCount: Double,
        decksRemaining: Double,
        usesTrueCount: Boolean,
        minDecksRemaining: Double
    ): Double {
        if (!usesTrueCount) return 0.0

        val safeDecksRemaining = decksRemaining.coerceAtLeast(minDecksRemaining)
        return roundToOneDecimal(runningCount / safeDecksRemaining)
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
        tableRules: TableRules
    ): Double {
        if (!usesTrueCount) return 0.0

        val houseEdge = estimateHouseEdgePercent(tableRules)
        val countAdjustment = trueCount * PLAYER_EDGE_PER_TRUE_COUNT
        return roundToOneDecimal((-houseEdge) + countAdjustment)
    }

    fun estimateHouseEdgePercent(tableRules: TableRules): Double {
        var houseEdge = BASE_HOUSE_EDGE_PERCENT

        houseEdge += when (tableRules.dealerSoft17Rule) {
            DealerSoft17Rule.STAND -> 0.0
            DealerSoft17Rule.HIT -> 0.2
        }

        houseEdge += when (tableRules.dealerPeekRule) {
            DealerPeekRule.ACE_AND_TEN -> 0.0
            DealerPeekRule.ACE_ONLY -> 0.1
            DealerPeekRule.NO_PEEK -> 0.2
        }

        houseEdge += when (tableRules.blackjackPayoutRule) {
            BlackjackPayoutRule.THREE_TO_TWO -> 0.0
            BlackjackPayoutRule.SIX_TO_FIVE -> 1.4
        }

        houseEdge += when (tableRules.doubleDownRule) {
            DoubleDownRule.ANY_TWO_CARDS -> 0.0
            DoubleDownRule.NINE_TO_ELEVEN -> 0.1
            DoubleDownRule.TEN_TO_ELEVEN -> 0.2
        }

        if (!tableRules.doubleAfterSplit) {
            houseEdge += 0.14
        }

        houseEdge += when (tableRules.surrenderRule) {
            SurrenderRule.NONE -> 0.0
            SurrenderRule.LATE -> -0.08
            SurrenderRule.EARLY -> -0.6
        }

        if (tableRules.resplitAces) {
            houseEdge += -0.08
        }
        if (tableRules.hitSplitAces) {
            houseEdge += -0.13
        }
        if (tableRules.sixCardCharlieAutoWin) {
            houseEdge += -0.16
        }

        houseEdge += when (tableRules.maxSplitHands.coerceIn(2, 6)) {
            2 -> 0.06
            3 -> 0.03
            4 -> 0.0
            5 -> -0.01
            else -> -0.02
        }

        return roundToOneDecimal(houseEdge)
    }

    private const val CARDS_PER_DECK = 52.0
    private const val ACES_PER_DECK = 4.0
    private const val BASE_HOUSE_EDGE_PERCENT = 0.5
    private const val PLAYER_EDGE_PER_TRUE_COUNT = 0.5
}
