package com.yourname.blackjackcounter.domain.model

data class CounterState(
    val runningCount: Double = 0.0,
    val trueCount: Double = 0.0,
    val deckPenetrationPercent: Double = 0.0,
    val totalDecks: Int = 6,
    val decksRemaining: Double = 6.0,
    val selectedCountingSystemId: String = "hi_lo",
    val selectedCountingSystemName: String = "Hi-Lo",
    val selectedSystemUsesTrueCount: Boolean = true,
    val showCheatSheet: Boolean = false
)
