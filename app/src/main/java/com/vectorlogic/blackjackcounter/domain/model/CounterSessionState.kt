package com.vectorlogic.blackjackcounter.domain.model

data class SeenCard(
    val rank: CardRank,
    val deltaApplied: Double,
    val countedAce: Boolean,
    // Decks-remaining reduction this card actually caused, so undo restores exactly that much.
    val decksAdvanced: Double = DECKS_PER_CARD
) {
    companion object {
        const val DECKS_PER_CARD: Double = 1.0 / 52.0
    }
}

data class CounterSessionState(
    val runningCount: Double = 0.0,
    val totalDecks: Int = 6,
    val decksRemaining: Double = 6.0,
    val acesSeen: Int = 0,
    val selectedCountingSystemId: String = CountingSystemId.HI_LO,
    val seenCards: List<SeenCard> = emptyList(),
    val tableRules: TableRules = TableRules()
)
