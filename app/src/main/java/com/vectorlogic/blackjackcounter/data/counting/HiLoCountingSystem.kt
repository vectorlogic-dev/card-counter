package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank

object HiLoCountingSystem : CountingSystem {
    override val id: String = "hi_lo"
    override val displayName: String = "Hi-Lo"
    override val level: Int = 1
    override val isBalanced: Boolean = true

    override fun getTagValue(cardRank: CardRank): Double {
        return when (cardRank) {
            CardRank.TWO, CardRank.THREE, CardRank.FOUR, CardRank.FIVE, CardRank.SIX -> 1.0
            CardRank.SEVEN, CardRank.EIGHT, CardRank.NINE -> 0.0
            CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.ACE -> -1.0
        }
    }

    override fun usesTrueCount(): Boolean = true
}
