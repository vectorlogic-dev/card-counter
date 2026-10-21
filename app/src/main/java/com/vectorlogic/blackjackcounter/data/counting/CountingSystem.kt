package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank

interface CountingSystem {
    val id: String
    val displayName: String
    val level: Int
    val isBalanced: Boolean

    // Approximate player edge gained per +1 Hi-Lo-equivalent true count (native TC divided by
    // TrueCountScaling.hiLoEquivalentScale), scaled by the system's Betting Correlation.
    // Hi-Lo baseline = 0.50% / TC.
    val edgeGainPercentPerTrueCount: Double get() = 0.50

    fun getTagValue(cardRank: CardRank): Double
    fun usesTrueCount(): Boolean

    // Some systems are strongest only when aces are tracked separately.
    fun recommendsAceSideCount(): Boolean = false
}
