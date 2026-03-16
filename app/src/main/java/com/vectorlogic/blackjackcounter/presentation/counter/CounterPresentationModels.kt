package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.domain.model.CheatSheetRow

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
