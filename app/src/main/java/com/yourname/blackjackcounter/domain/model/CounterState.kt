package com.yourname.blackjackcounter.domain.model

data class CheatSheetRow(
    val cardsLabel: String,
    val tagValueLabel: String
)

data class StrategyDeviationRow(
    val playLabel: String,
    val indexLabel: String
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
    val acesSeen: Int = 0,
    val acesExpected: Double = 0.0,
    val estimatedPlayerEdgePercent: Double = 0.0,
    val canEstimateEdge: Boolean = true,
    val selectedCountingSystemId: String = "hi_lo",
    val selectedCountingSystemName: String = "Hi-Lo",
    val selectedSystemUsesTrueCount: Boolean = true,
    val selectedSystemRecommendsAceSideCount: Boolean = false,
    val tableRules: TableRules = TableRules(),
    val buttonTagValues: Map<CardRank, Double> = emptyMap(),
    val cheatSheetRows: List<CheatSheetRow> = emptyList(),
    val strategyDeviationRows: List<StrategyDeviationRow> = emptyList(),
    val showCheatSheet: Boolean = false
)
