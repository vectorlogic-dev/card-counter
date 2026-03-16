package com.vectorlogic.blackjackcounter.domain.model

data class SeenCard(
    val rank: CardRank,
    val deltaApplied: Double,
    val countedAce: Boolean
)

data class CounterSessionState(
    val runningCount: Double = 0.0,
    val totalDecks: Int = 6,
    val decksRemaining: Double = 6.0,
    val acesSeen: Int = 0,
    val selectedCountingSystemId: String = CountingSystemId.HI_LO,
    val seenCards: List<SeenCard> = emptyList(),
    val tableRules: TableRules = TableRules()
)
