package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.annotation.StringRes
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.presentation.UiText

data class CheatSheetRow(
    val cardsLabel: String,
    val tagValueLabel: String
)

data class StrategyDeviationRow(
    val playLabel: UiText,
    val indexLabel: UiText,
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

/** A play the player can choose; the chart's codes (H, S, D, Ds, P, Rh...) map onto these. */
enum class StrategyPlay(@StringRes val labelRes: Int) {
    HIT(R.string.action_hit),
    STAND(R.string.action_stand),
    DOUBLE(R.string.action_double),
    SPLIT(R.string.action_split),
    SURRENDER(R.string.action_surrender);

    companion object {
        fun fromAction(action: String): StrategyPlay = when {
            action.startsWith("R") -> SURRENDER
            action.startsWith("D") -> DOUBLE
            action.startsWith("P") -> SPLIT
            action.startsWith("S") -> STAND
            else -> HIT
        }
    }
}

/**
 * One count-dependent hand for the deviation quiz. [row] carries the cheat-sheet labels and the
 * trigger (always non-null here); the hand fields let the engine look up the chart's play.
 */
data class DeviationQuizItem(
    val handLabel: String,
    val dealerLabel: String,
    val isPair: Boolean,
    val deviationPlay: StrategyPlay,
    val row: StrategyDeviationRow,
    val handType: CounterStrategyEngine.ChartHandType,
    val handValue: Int,
    val dealerUpcard: Int
)

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
    val explanation: UiText,
    val tagRows: List<CheatSheetRow>
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
    val title: UiText,
    val rows: List<StrategyTableRow>
)

data class StrategyChartModel(
    val trueCountLabel: UiText,
    val supportsTrueCount: Boolean,
    val sections: List<StrategyTableSection>
)

data class CounterUiState(
    val runningCount: Double = 0.0,
    val trueCount: Double = 0.0,
    val recommendedBetUnits: Int = 1,
    val betSizingCountLabel: UiText = UiText.Res(R.string.count_tc),
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
