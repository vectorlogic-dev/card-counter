package com.yourname.blackjackcounter.data.counting

import com.yourname.blackjackcounter.domain.model.CardRank

interface CountingSystem {
    val id: String
    val displayName: String
    val level: Int
    val isBalanced: Boolean

    fun getTagValue(cardRank: CardRank): Double
    fun usesTrueCount(): Boolean
}
