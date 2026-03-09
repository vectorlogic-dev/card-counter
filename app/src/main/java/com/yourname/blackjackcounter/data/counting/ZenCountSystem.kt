package com.yourname.blackjackcounter.data.counting

import com.yourname.blackjackcounter.domain.model.CardRank

object ZenCountSystem : CountingSystem {
    override val id: String = "zen_count"
    override val displayName: String = "Zen Count"
    override val level: Int = 2
    override val isBalanced: Boolean = true

    override fun getTagValue(cardRank: CardRank): Double {
        return when (cardRank) {
            CardRank.TWO, CardRank.THREE, CardRank.SEVEN -> 1.0
            CardRank.FOUR, CardRank.FIVE, CardRank.SIX -> 2.0
            CardRank.EIGHT, CardRank.NINE -> 0.0
            CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> -2.0
            CardRank.ACE -> -1.0
        }
    }

    override fun usesTrueCount(): Boolean = true
}
