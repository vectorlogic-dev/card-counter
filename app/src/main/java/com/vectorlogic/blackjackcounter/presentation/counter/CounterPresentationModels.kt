package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.TableRules

data class CheatSheetRow(
    val cardsLabel: String,
    val tagValueLabel: String
)

data class StrategyDeviationRow(
    val playLabel: String,
    val indexLabel: String,
    // Null when the row doesn't depend on the count (e.g. basic-strategy surrender, or a play the
    // table rules don't allow).
    val trigger: DeviationTrigger? = null
) {
    fun isActiveAt(decisionCount: Double): Boolean = trigger?.isMetBy(decisionCount) ?: false
}

/** The count (native TC, or RC for running-count systems) at which a deviation row applies. */
data class DeviationTrigger(
    val threshold: Double,
    val atOrAbove: Boolean = true
) {
    fun isMetBy(decisionCount: Double): Boolean {
        return if (atOrAbove) decisionCount >= threshold else decisionCount < threshold
    }
}

data class CountingSystemOption(
    val id: String,
    val displayName: String,
    val level: Int,
    val recommendsAceSideCount: Boolean
)

data class CountingSystemGuide(
    val id: String,
    val displayName: String,
    val level: Int,
    val isBalanced: Boolean,
    val usesTrueCount: Boolean,
    val recommendsAceSideCount: Boolean,
    val explanation: String,
    val tagRows: List<CheatSheetRow>
)

data class StrategyChartSection(
    val title: String,
    val rows: List<StrategyChartRow>
)

data class StrategyChartRow(
    val hand: String,
    val action: String
)

data class StrategyTableCell(
    val action: String,
    val isDeviation: Boolean
)

data class StrategyTableRow(
    val handLabel: String,
    val cells: List<StrategyTableCell>
)

data class StrategyTableSection(
    val title: String,
    val rows: List<StrategyTableRow>
)

data class StrategyChartModel(
    val trueCountLabel: String,
    val supportsTrueCount: Boolean,
    val sections: List<StrategyTableSection>
)

data class CounterUiState(
    val runningCount: Double = 0.0,
    val trueCount: Double = 0.0,
    val recommendedBetUnits: Int = 1,
    val betSizingCountLabel: String = "TC",
    val betSizingCountValue: Double = 0.0,
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
    val selectedCountingSystemId: String = "",
    val selectedCountingSystemName: String = "Hi-Lo",
    val selectedSystemUsesTrueCount: Boolean = true,
    val selectedSystemRecommendsAceSideCount: Boolean = false,
    val seenCards: List<SeenCard> = emptyList(),
    val tableRules: TableRules = TableRules(),
    val buttonTagValues: Map<CardRank, Double> = emptyMap(),
    val cheatSheetRows: List<CheatSheetRow> = emptyList(),
    val strategyDeviationRows: List<StrategyDeviationRow> = emptyList(),
    val showCheatSheet: Boolean = false
)
