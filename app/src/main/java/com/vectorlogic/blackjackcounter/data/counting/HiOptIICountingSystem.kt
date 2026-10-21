package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CountingSystemId

object HiOptIICountingSystem : CountingSystem {
    override val id: String = CountingSystemId.HI_OPT_II
    override val displayName: String = "Hi-Opt II"
    override val level: Int = 2
    override val isBalanced: Boolean = true
    override val edgeGainPercentPerTrueCount: Double = 0.46

    override fun getTagValue(cardRank: CardRank): Double {
        return when (cardRank) {
            CardRank.TWO, CardRank.THREE, CardRank.SIX, CardRank.SEVEN -> 1.0
            CardRank.FOUR, CardRank.FIVE -> 2.0
            CardRank.EIGHT, CardRank.NINE, CardRank.ACE -> 0.0
            CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> -2.0
        }
    }

    override fun usesTrueCount(): Boolean = true

    override fun recommendsAceSideCount(): Boolean = true
}
