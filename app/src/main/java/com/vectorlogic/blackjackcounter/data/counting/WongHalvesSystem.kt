package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank

object WongHalvesSystem : CountingSystem {
    override val id: String = "wong_halves"
    override val displayName: String = "Wong Halves"
    override val level: Int = 3
    override val isBalanced: Boolean = true

    override fun getTagValue(cardRank: CardRank): Double {
        return when (cardRank) {
            CardRank.TWO -> 0.5
            CardRank.THREE -> 1.0
            CardRank.FOUR -> 1.0
            CardRank.FIVE -> 1.5
            CardRank.SIX -> 1.0
            CardRank.SEVEN -> 0.5
            CardRank.EIGHT -> 0.0
            CardRank.NINE -> -0.5
            CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.ACE -> -1.0
        }
    }

    override fun usesTrueCount(): Boolean = true
}
