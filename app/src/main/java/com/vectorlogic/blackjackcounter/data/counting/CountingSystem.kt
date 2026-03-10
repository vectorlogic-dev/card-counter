package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank

interface CountingSystem {
    val id: String
    val displayName: String
    val level: Int
    val isBalanced: Boolean

    fun getTagValue(cardRank: CardRank): Double
    fun usesTrueCount(): Boolean

    // Some systems are strongest only when aces are tracked separately.
    fun recommendsAceSideCount(): Boolean = false
}
