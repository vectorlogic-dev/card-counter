package com.yourname.blackjackcounter.domain.model

data class CheatSheetRow(
    val cardsLabel: String,
    val tagValueLabel: String
)

data class CounterState(
    val runningCount: Double = 0.0,
    val trueCount: Double = 0.0,
    val deckPenetrationPercent: Double = 0.0,
    val totalDecks: Int = 6,
    val decksRemaining: Double = 6.0,
    val totalCards: Int = 312,
    val cardsDrawn: Int = 0,
    val cardsLeft: Int = 312,
    val selectedCountingSystemId: String = "hi_lo",
    val selectedCountingSystemName: String = "Hi-Lo",
    val selectedSystemUsesTrueCount: Boolean = true,
    val cheatSheetRows: List<CheatSheetRow> = emptyList(),
    val showCheatSheet: Boolean = false
)
