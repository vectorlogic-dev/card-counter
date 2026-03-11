package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CountingSystemId

object KOCountingSystem : CountingSystem {
    override val id: String = CountingSystemId.KO
    override val displayName: String = "KO"
    override val level: Int = 1
    override val isBalanced: Boolean = false

    override fun getTagValue(cardRank: CardRank): Double {
        return when (cardRank) {
            CardRank.TWO,
            CardRank.THREE,
            CardRank.FOUR,
            CardRank.FIVE,
            CardRank.SIX,
            CardRank.SEVEN -> 1.0

            CardRank.EIGHT, CardRank.NINE -> 0.0
            CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.ACE -> -1.0
        }
    }

    override fun usesTrueCount(): Boolean = false
}
