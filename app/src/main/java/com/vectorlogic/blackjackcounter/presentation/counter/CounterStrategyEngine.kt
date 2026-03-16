package com.vectorlogic.blackjackcounter.presentation.counter

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
        decisionTrueCount: Int
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
                        decisionTrueCount = decisionTrueCount
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
                        decisionTrueCount = decisionTrueCount
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
                        decisionTrueCount = decisionTrueCount
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
        pivot: Int?
    ): List<StrategyDeviationRow> {
        if (!usesTrueCount) {
            return listOfNotNull(
                keyCount?.let {
                    StrategyDeviationRow(
                        playLabel = "Key Count (${state.totalDecks}D)",
                        indexLabel = "RC $it"
                    )
                },
                pivot?.let {
                    StrategyDeviationRow(
                        playLabel = "Pivot (${state.totalDecks}D)",
                        indexLabel = "RC $it"
                    )
                }
            )
        }

        return listOf(
            StrategyDeviationRow(playLabel = "Insurance vs A", indexLabel = "Take at TC +3"),
            StrategyDeviationRow(playLabel = "16 vs 10", indexLabel = "Stand at TC 0+"),
            StrategyDeviationRow(playLabel = "15 vs 10", indexLabel = "Stand at TC +4"),
            StrategyDeviationRow(playLabel = "10 vs 10", indexLabel = "Double at TC +4"),
            StrategyDeviationRow(playLabel = "12 vs 3", indexLabel = "Stand at TC +2"),
            StrategyDeviationRow(playLabel = "12 vs 2", indexLabel = "Stand at TC +3"),
            StrategyDeviationRow(playLabel = "11 vs A", indexLabel = "Double at TC +1"),
            StrategyDeviationRow(playLabel = "9 vs 2", indexLabel = "Double at TC +1"),
            StrategyDeviationRow(playLabel = "9 vs 7", indexLabel = "Double at TC +3")
        )
    }

    private fun computeChartCell(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        state: CounterSessionState,
        supportsTrueCount: Boolean,
        decisionTrueCount: Int
    ): StrategyTableCell {
        val baseAction = getBaseAction(
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )
        val baseAdjusted = applyRuleAdjustments(
            action = baseAction,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )

        if (!supportsTrueCount) {
            return StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }

        val deviationAction = getCountDeviationAction(
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            trueCount = decisionTrueCount
        ) ?: return StrategyTableCell(action = baseAdjusted, isDeviation = false)

        val deviationAdjusted = applyRuleAdjustments(
            action = deviationAction,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )

        return if (deviationAdjusted != baseAdjusted) {
            StrategyTableCell(action = deviationAdjusted, isDeviation = true)
        } else {
            StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }
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

    private fun getCountDeviationAction(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        trueCount: Int
    ): String? {
        if (handType == ChartHandType.HARD) {
            return when {
                handValue == 16 && dealerUpcard == 10 && trueCount >= 0 -> "S"
                handValue == 15 && dealerUpcard == 10 && trueCount >= 4 -> "S"
                handValue == 12 && dealerUpcard == 3 && trueCount >= 2 -> "S"
                handValue == 12 && dealerUpcard == 2 && trueCount >= 3 -> "S"
                handValue == 11 && dealerUpcard == 11 && trueCount >= 1 -> "D"
                handValue == 10 && dealerUpcard == 11 && trueCount >= 4 -> "D"
                handValue == 10 && dealerUpcard == 10 && trueCount >= 4 -> "D"
                handValue == 9 && dealerUpcard == 2 && trueCount >= 1 -> "D"
                handValue == 9 && dealerUpcard == 7 && trueCount >= 3 -> "D"
                handValue == 16 && dealerUpcard == 9 && trueCount >= 5 -> "S"
                handValue == 15 && dealerUpcard == 9 && trueCount >= 2 -> "S"
                handValue == 15 && dealerUpcard == 11 && trueCount >= 5 -> "S"
                else -> null
            }
        }

        if (handType == ChartHandType.PAIR && handValue == 10) {
            return when {
                dealerUpcard == 5 && trueCount >= 5 -> "P"
                dealerUpcard == 6 && trueCount >= 4 -> "P"
                else -> null
            }
        }

        return null
    }

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
