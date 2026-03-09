package com.yourname.blackjackcounter.domain.model

enum class DealerSoft17Rule {
    STAND,
    HIT
}

enum class DealerPeekRule {
    ACE_AND_TEN,
    ACE_ONLY,
    NO_PEEK
}

enum class BlackjackPayoutRule {
    THREE_TO_TWO,
    SIX_TO_FIVE
}

enum class DoubleDownRule {
    ANY_TWO_CARDS,
    NINE_TO_ELEVEN,
    TEN_TO_ELEVEN
}

enum class SurrenderRule {
    NONE,
    LATE,
    EARLY
}

data class TableRules(
    val dealerSoft17Rule: DealerSoft17Rule = DealerSoft17Rule.STAND,
    val dealerPeekRule: DealerPeekRule = DealerPeekRule.ACE_AND_TEN,
    val blackjackPayoutRule: BlackjackPayoutRule = BlackjackPayoutRule.THREE_TO_TWO,
    val doubleDownRule: DoubleDownRule = DoubleDownRule.ANY_TWO_CARDS,
    val doubleAfterSplit: Boolean = true,
    val surrenderRule: SurrenderRule = SurrenderRule.NONE,
    val resplitAces: Boolean = false,
    val hitSplitAces: Boolean = false,
    val maxSplitHands: Int = 4,
    val sixCardCharlieAutoWin: Boolean = false
)
