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
}
