package com.vectorlogic.blackjackcounter.domain.drill

import com.vectorlogic.blackjackcounter.domain.model.CardRank

enum class DrillSpeed(val intervalMs: Long) {
    SLOW(1500L),
    MEDIUM(1000L),
    FAST(600L)
}

enum class DrillSize(val cardCount: Int) {
    HALF_DECK(26),
    DECK(52),
    DECK_AND_A_HALF(78)
}

data class DrillConfig(
    val size: DrillSize = DrillSize.HALF_DECK,
    val speed: DrillSpeed = DrillSpeed.MEDIUM
)

data class DrillResult(
    val actualRunningCount: Double,
    val userReportedCount: Double,
    val cards: List<CardRank>,
    val elapsedMs: Long,
    val systemDisplayName: String
) {
    val isExact: Boolean get() = kotlin.math.abs(actualRunningCount - userReportedCount) < 0.0001
    val absoluteError: Double get() = kotlin.math.abs(actualRunningCount - userReportedCount)
    val cardsPerSecond: Double
        get() = if (elapsedMs <= 0L) 0.0 else cards.size.toDouble() / (elapsedMs / 1000.0)
}
