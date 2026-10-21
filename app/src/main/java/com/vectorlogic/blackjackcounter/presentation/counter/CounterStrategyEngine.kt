package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.data.counting.TrueCountScaling
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import java.util.Locale

object CounterStrategyEngine {

    private val dealerUpcards = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)

    fun buildStrategyChartModel(
        state: CounterSessionState,
        trueCount: Double,
        supportsTrueCount: Boolean,
        // CounterCalculations.decisionTrueCount: the native true count to one decimal.
        decisionTrueCount: Double,
        trueCountScale: Double
    ): StrategyChartModel {
        val hardRows = listOf(17, 16, 15, 14, 13, 12, 11, 10, 9, 8).map { total ->
            StrategyTableRow(
                handLabel = if (total == 17) "17+" else total.toString(),
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.HARD,
                        handValue = total,
                        dealerUpcard = dealer,
                        state = state,
                        supportsTrueCount = supportsTrueCount,
                        decisionTrueCount = decisionTrueCount,
                        trueCountScale = trueCountScale
                    )
                }
            )
        }

        val softRows = listOf(9, 8, 7, 6, 5, 4, 3, 2).map { soft ->
            StrategyTableRow(
                handLabel = "A,$soft",
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.SOFT,
                        handValue = soft,
                        dealerUpcard = dealer,
                        state = state,
                        supportsTrueCount = supportsTrueCount,
                        decisionTrueCount = decisionTrueCount,
                        trueCountScale = trueCountScale
                    )
                }
            )
        }

        val pairRows = listOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2).map { pairRank ->
            val label = if (pairRank == 11) "A,A" else "$pairRank,$pairRank"
            StrategyTableRow(
                handLabel = label,
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.PAIR,
                        handValue = pairRank,
                        dealerUpcard = dealer,
                        state = state,
                        supportsTrueCount = supportsTrueCount,
                        decisionTrueCount = decisionTrueCount,
                        trueCountScale = trueCountScale
                    )
                }
            )
        }

        return StrategyChartModel(
            trueCountLabel = if (supportsTrueCount) {
                String.format(Locale.US, "TC %.1f", trueCount)
            } else {
                "TC N/A"
            },
            supportsTrueCount = supportsTrueCount,
            sections = listOf(
                StrategyTableSection(title = "Hard Totals", rows = hardRows),
                StrategyTableSection(title = "Soft Totals", rows = softRows),
                StrategyTableSection(title = "Pairs", rows = pairRows)
            )
        )
    }

    fun buildStrategyDeviationRows(
        state: CounterSessionState,
        usesTrueCount: Boolean,
        keyCount: Int?,
        pivot: Int?,
        trueCountScale: Double
    ): List<StrategyDeviationRow> {
        if (!usesTrueCount) {
            return listOfNotNull(
                keyCount?.let {
                    StrategyDeviationRow(
                        playLabel = "Key Count (${state.totalDecks}D)",
                        indexLabel = "RC $it",
                        trigger = DeviationTrigger(threshold = it.toDouble())
                    )
                },
                pivot?.let {
                    StrategyDeviationRow(
                        playLabel = "Pivot (${state.totalDecks}D)",
                        indexLabel = "RC $it",
                        trigger = DeviationTrigger(threshold = it.toDouble())
                    )
                }
            )
        }

        val rules = state.tableRules
        val surrenderAllowed = rules.surrenderRule != SurrenderRule.NONE
        val insuranceRow = StrategyDeviationRow(
            playLabel = "Insurance vs A",
            indexLabel = "Take at TC ${formatAtOrAbove(nativeIndex(INSURANCE_HI_LO_INDEX, trueCountScale))}",
            trigger = DeviationTrigger(threshold = nativeIndex(INSURANCE_HI_LO_INDEX, trueCountScale).toDouble())
        )
        val playRows = playIndices.mapNotNull { index ->
            val baseAction = getBaseAction(
                handType = index.handType,
                handValue = index.handValue,
                dealerUpcard = index.dealerUpcard,
                rules = rules
            )
            when {
                // Surrender takes precedence, so the surrender index row covers this hand.
                surrenderAllowed && surrenderIndices.any { it.sameHandAs(index) } -> null
                surrenderAllowed && baseAction.startsWith("R") ->
                    StrategyDeviationRow(playLabel = index.handLabel(), indexLabel = "Surrender (any TC)")
                // The chart never shows a double the table forbids (e.g. 10 vs 10 without a peek on 10).
                index.isBlockedBy(rules) ->
                    StrategyDeviationRow(playLabel = index.handLabel(), indexLabel = "No double (rules)")
                // Basic strategy already makes this play at every count (11 vs A under H17).
                baseAction == index.action ->
                    StrategyDeviationRow(playLabel = index.handLabel(), indexLabel = "${index.verb()} (any TC)")
                else -> index.toRow(trueCountScale)
            }
        }
        val surrenderRows = if (surrenderAllowed) {
            surrenderIndices.map { it.toRow(trueCountScale) }
        } else {
            emptyList()
        }
        return listOf(insuranceRow) + playRows + surrenderRows
    }

    private fun computeChartCell(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        state: CounterSessionState,
        supportsTrueCount: Boolean,
        decisionTrueCount: Double,
        trueCountScale: Double
    ): StrategyTableCell {
        val rules = state.tableRules
        val baseAction = getBaseAction(
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = rules
        )
        val baseAdjusted = applyRuleAdjustments(
            action = baseAction,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = rules
        )

        if (!supportsTrueCount) {
            return StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }

        val surrenderAllowed = rules.surrenderRule != SurrenderRule.NONE
        val surrenderIndex = if (surrenderAllowed) {
            findIndex(surrenderIndices, handType, handValue, dealerUpcard)
        } else {
            null
        }
        val afterSurrender = when {
            surrenderIndex == null -> baseAction
            surrenderIndex.isTriggered(decisionTrueCount, trueCountScale) -> surrenderIndex.action
            else -> baseAction.withoutSurrender()
        }
        // Playing indices (e.g. stand 16 vs 10) only apply when surrender is not the chosen play;
        // at those counts surrendering is still better than standing.
        val playDeviation = if (afterSurrender.startsWith("R")) {
            null
        } else {
            findIndex(playIndices, handType, handValue, dealerUpcard)
                ?.takeIf { it.isTriggered(decisionTrueCount, trueCountScale) }
                ?.action
        }

        val deviationAdjusted = applyRuleAdjustments(
            action = playDeviation ?: afterSurrender,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = rules
        )

        return StrategyTableCell(action = deviationAdjusted, isDeviation = deviationAdjusted != baseAdjusted)
    }

    private fun getBaseAction(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        return when (handType) {
            ChartHandType.HARD -> getBaseHardAction(
                hardTotal = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
            ChartHandType.SOFT -> getBaseSoftAction(
                softCard = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
            ChartHandType.PAIR -> getBasePairAction(
                pairRank = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
        }
    }

    private fun getBaseHardAction(
        hardTotal: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        val surrenderAllowed = rules.surrenderRule != SurrenderRule.NONE
        return when {
            hardTotal >= 17 -> "S"
            hardTotal == 16 -> when {
                surrenderAllowed && dealerUpcard in listOf(9, 10, 11) -> "Rh"
                dealerUpcard in 2..6 -> "S"
                else -> "H"
            }
            hardTotal == 15 -> when {
                surrenderAllowed && dealerUpcard == 10 -> "Rh"
                dealerUpcard in 2..6 -> "S"
                else -> "H"
            }
            hardTotal in 13..14 -> if (dealerUpcard in 2..6) "S" else "H"
            hardTotal == 12 -> if (dealerUpcard in 4..6) "S" else "H"
            hardTotal == 11 -> {
                if (dealerUpcard == 11 && rules.dealerSoft17Rule == DealerSoft17Rule.STAND) "H" else "D"
            }
            hardTotal == 10 -> if (dealerUpcard in 2..9) "D" else "H"
            hardTotal == 9 -> if (dealerUpcard in 3..6) "D" else "H"
            else -> "H"
        }
    }

    private fun getBaseSoftAction(
        softCard: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        return when (softCard) {
            9, 8 -> "S"
            7 -> when {
                dealerUpcard in 3..6 -> "Ds"
                dealerUpcard in listOf(2, 7, 8) -> {
                    if (dealerUpcard == 2 && rules.dealerSoft17Rule == DealerSoft17Rule.HIT) "Ds" else "S"
                }
                else -> "H"
            }
            6 -> if (dealerUpcard in 3..6) "D" else "H"
            5, 4 -> if (dealerUpcard in 4..6) "D" else "H"
            3, 2 -> if (dealerUpcard in 5..6) "D" else "H"
            else -> "H"
        }
    }

    private fun getBasePairAction(
        pairRank: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        val noPeekOnTen = rules.dealerPeekRule == DealerPeekRule.ACE_ONLY ||
            rules.dealerPeekRule == DealerPeekRule.NO_PEEK
        val noPeekOnAce = rules.dealerPeekRule == DealerPeekRule.NO_PEEK

        return when (pairRank) {
            11 -> if (noPeekOnAce && dealerUpcard == 11) "H" else "P"
            10 -> "S"
            9 -> if (dealerUpcard in listOf(2, 3, 4, 5, 6, 8, 9)) "P" else "S"
            8 -> {
                val blocked = (noPeekOnTen && dealerUpcard == 10) || (noPeekOnAce && dealerUpcard == 11)
                if (blocked) "H" else "P"
            }
            7 -> if (dealerUpcard in 2..7) "P" else "H"
            6 -> {
                if (rules.doubleAfterSplit) {
                    if (dealerUpcard in 2..6) "P" else "H"
                } else {
                    if (dealerUpcard in 3..6) "P" else "H"
                }
            }
            5 -> if (dealerUpcard in 2..9) "D" else "H"
            4 -> if (rules.doubleAfterSplit && dealerUpcard in 5..6) "P" else "H"
            3, 2 -> {
                if (rules.doubleAfterSplit) {
                    if (dealerUpcard in 2..7) "P" else "H"
                } else {
                    if (dealerUpcard in 4..7) "P" else "H"
                }
            }
            else -> "H"
        }
    }

    private fun findIndex(
        indices: List<CountIndex>,
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int
    ): CountIndex? {
        return indices.firstOrNull {
            it.handType == handType && it.handValue == handValue && it.dealerUpcard == dealerUpcard
        }
    }

    // Both sides are multiples of 0.1, so a play triggers exactly at the threshold its label shows.
    private fun CountIndex.isTriggered(decisionTrueCount: Double, trueCountScale: Double): Boolean {
        val nativeIndex = nativeIndex(hiLoIndex, trueCountScale)
        return when (direction) {
            IndexDirection.AT_OR_ABOVE -> decisionTrueCount >= nativeIndex
            IndexDirection.BELOW -> decisionTrueCount < nativeIndex
        }
    }

    // Same threshold and direction as isTriggered, for the cheat sheet to highlight the row.
    private fun CountIndex.trigger(trueCountScale: Double): DeviationTrigger {
        return DeviationTrigger(
            threshold = nativeIndex(hiLoIndex, trueCountScale).toDouble(),
            atOrAbove = direction == IndexDirection.AT_OR_ABOVE
        )
    }

    private fun CountIndex.isBlockedBy(rules: TableRules): Boolean {
        return applyRuleAdjustments(
            action = action,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = rules
        ) != action
    }

    private fun String.withoutSurrender(): String {
        return if (startsWith("R")) drop(1).uppercase() else this
    }

    private fun nativeIndex(hiLoIndex: Int, trueCountScale: Double): Double {
        return TrueCountScaling.toNativeThreshold(hiLoIndex, trueCountScale)
    }

    // Same formatter as the bet card's count, e.g. "+7.2", "+4", "0+".
    private fun formatAtOrAbove(index: Double): String {
        return if (index > 0) formatSignedCount(index) else "${formatSignedCount(index)}+"
    }

    private fun formatSigned(index: Double): String {
        return formatSignedCount(index)
    }

    private fun CountIndex.handLabel(): String {
        val dealerLabel = if (dealerUpcard == 11) "A" else dealerUpcard.toString()
        val handLabel = when (handType) {
            ChartHandType.PAIR -> if (handValue == 11) "A,A" else "$handValue,$handValue"
            ChartHandType.SOFT -> "A,$handValue"
            ChartHandType.HARD -> handValue.toString()
        }
        return "$handLabel vs $dealerLabel"
    }

    private fun CountIndex.sameHandAs(other: CountIndex): Boolean {
        return handType == other.handType && handValue == other.handValue && dealerUpcard == other.dealerUpcard
    }

    private fun CountIndex.verb(): String = when {
        action.startsWith("R") -> "Surrender"
        action == "S" -> "Stand"
        action == "H" -> "Hit"
        action == "P" -> "Split"
        else -> "Double"
    }

    private fun CountIndex.toRow(trueCountScale: Double): StrategyDeviationRow {
        val nativeIndex = nativeIndex(hiLoIndex, trueCountScale)
        val threshold = when (direction) {
            IndexDirection.AT_OR_ABOVE -> "at TC ${formatAtOrAbove(nativeIndex)}"
            IndexDirection.BELOW -> "below TC ${formatSigned(nativeIndex)}"
        }
        return StrategyDeviationRow(
            playLabel = handLabel(),
            indexLabel = "${verb()} $threshold",
            trigger = trigger(trueCountScale)
        )
    }

    private enum class IndexDirection {
        AT_OR_ABOVE,
        BELOW
    }

    // Indices are Hi-Lo values (multi-deck, S17); other balanced systems use them converted to
    // native true counts (one decimal) via TrueCountScaling. Labels and chart triggers use the
    // same converted value, so they always agree.
    private data class CountIndex(
        val handType: ChartHandType,
        val handValue: Int,
        val dealerUpcard: Int,
        val hiLoIndex: Int,
        val action: String,
        val direction: IndexDirection = IndexDirection.AT_OR_ABOVE
    )

    private const val INSURANCE_HI_LO_INDEX = 3

    // Illustrious 18 (minus insurance, which has no chart cell) plus 15 vs A.
    private val playIndices: List<CountIndex> = listOf(
        CountIndex(ChartHandType.HARD, 16, 10, 0, "S"),
        CountIndex(ChartHandType.HARD, 15, 10, 4, "S"),
        CountIndex(ChartHandType.PAIR, 10, 5, 5, "P"),
        CountIndex(ChartHandType.PAIR, 10, 6, 4, "P"),
        CountIndex(ChartHandType.HARD, 10, 10, 4, "D"),
        CountIndex(ChartHandType.HARD, 12, 3, 2, "S"),
        CountIndex(ChartHandType.HARD, 12, 2, 3, "S"),
        CountIndex(ChartHandType.HARD, 11, 11, 1, "D"),
        CountIndex(ChartHandType.HARD, 9, 2, 1, "D"),
        CountIndex(ChartHandType.HARD, 10, 11, 4, "D"),
        CountIndex(ChartHandType.HARD, 9, 7, 3, "D"),
        CountIndex(ChartHandType.HARD, 16, 9, 5, "S"),
        CountIndex(ChartHandType.HARD, 13, 2, -1, "H", IndexDirection.BELOW),
        CountIndex(ChartHandType.HARD, 12, 4, 0, "H", IndexDirection.BELOW),
        CountIndex(ChartHandType.HARD, 12, 5, -2, "H", IndexDirection.BELOW),
        CountIndex(ChartHandType.HARD, 12, 6, -1, "H", IndexDirection.BELOW),
        CountIndex(ChartHandType.HARD, 13, 3, -2, "H", IndexDirection.BELOW),
        CountIndex(ChartHandType.HARD, 15, 11, 5, "S")
    )

    // Fab 4 late-surrender indices, applied only when surrender is offered: surrender at or above
    // the index, otherwise play the hand without surrendering.
    private val surrenderIndices: List<CountIndex> = listOf(
        CountIndex(ChartHandType.HARD, 14, 10, 3, "Rh"),
        CountIndex(ChartHandType.HARD, 15, 10, 0, "Rh"),
        CountIndex(ChartHandType.HARD, 15, 9, 2, "Rh"),
        CountIndex(ChartHandType.HARD, 15, 11, 1, "Rh")
    )

    private fun applyRuleAdjustments(
        action: String,
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        var adjusted = action

        if (adjusted.startsWith("R") && rules.surrenderRule == SurrenderRule.NONE) {
            adjusted = "H"
        }

        val noPeekOnTenOnly = rules.dealerPeekRule == DealerPeekRule.ACE_ONLY
        val noPeekOnTenAndAce = rules.dealerPeekRule == DealerPeekRule.NO_PEEK
        if (adjusted in listOf("D", "Ds")) {
            val allowedByDoubleRule = isDoubleAllowed(
                handType = handType,
                handValue = handValue,
                rule = rules.doubleDownRule
            )
            val blockedByNoPeek = (noPeekOnTenOnly && dealerUpcard == 10) ||
                (noPeekOnTenAndAce && dealerUpcard in listOf(10, 11))
            if (!allowedByDoubleRule || blockedByNoPeek) {
                adjusted = if (adjusted == "Ds") "S" else "H"
            }
        }

        return adjusted
    }

    private fun isDoubleAllowed(
        handType: ChartHandType,
        handValue: Int,
        rule: DoubleDownRule
    ): Boolean {
        if (rule == DoubleDownRule.ANY_TWO_CARDS) return true

        val totalForRule = when (handType) {
            ChartHandType.HARD -> handValue
            ChartHandType.SOFT -> 11 + handValue
            ChartHandType.PAIR -> handValue * 2
        }

        return when (rule) {
            DoubleDownRule.ANY_TWO_CARDS -> true
            DoubleDownRule.NINE_TO_ELEVEN -> totalForRule in 9..11
            DoubleDownRule.TEN_TO_ELEVEN -> totalForRule in 10..11
        }
    }

    private enum class ChartHandType {
        HARD,
        SOFT,
        PAIR
    }
}
