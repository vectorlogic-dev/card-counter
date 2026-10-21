package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.KOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.OmegaIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.ReKOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.WongHalvesSystem
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules

object CounterHelpContentFactory {

    // Help no longer shows basicStrategySections or buildSplitDeviationSections: the static prose
    // disagreed with the rule- and count-aware Strategy Table and cheat sheet, so the dialog points
    // to those instead. Both stay only because CounterViewModel still exposes them; remove together.
    val basicStrategySections: List<StrategyChartSection> = listOf(
        StrategyChartSection(
            title = "Hard Totals",
            rows = listOf(
                StrategyChartRow("Hard 8 or less", "Hit"),
                StrategyChartRow("Hard 9", "Double vs 3-6, otherwise Hit"),
                StrategyChartRow("Hard 10", "Double vs 2-9, otherwise Hit"),
                StrategyChartRow("Hard 11", "Double (otherwise Hit)"),
                StrategyChartRow("Hard 12", "Stand vs 4-6, otherwise Hit"),
                StrategyChartRow("Hard 13-16", "Stand vs 2-6, otherwise Hit"),
                StrategyChartRow("Hard 17+", "Stand")
            )
        ),
        StrategyChartSection(
            title = "Soft Totals",
            rows = listOf(
                StrategyChartRow("A,2 or A,3", "Double vs 5-6, otherwise Hit"),
                StrategyChartRow("A,4 or A,5", "Double vs 4-6, otherwise Hit"),
                StrategyChartRow("A,6", "Double vs 3-6, otherwise Hit"),
                StrategyChartRow("A,7", "Stand vs 2,7,8; Double vs 3-6; Hit vs 9,10,A"),
                StrategyChartRow("A,8+", "Stand")
            )
        ),
        StrategyChartSection(
            title = "Pairs",
            rows = listOf(
                StrategyChartRow("A,A", "Split"),
                StrategyChartRow("10,10", "Stand"),
                StrategyChartRow("9,9", "Split vs 2-6,8,9; otherwise Stand"),
                StrategyChartRow("8,8", "Split"),
                StrategyChartRow("7,7", "Split vs 2-7, otherwise Hit"),
                StrategyChartRow("6,6", "Split vs 2-6, otherwise Hit"),
                StrategyChartRow("5,5", "Double vs 2-9, otherwise Hit"),
                StrategyChartRow("4,4", "Split vs 5-6, otherwise Hit"),
                StrategyChartRow("3,3 or 2,2", "Split vs 2-7, otherwise Hit")
            )
        )
    )

    fun buildCountingSystemGuides(systems: List<CountingSystem>): List<CountingSystemGuide> {
        return systems.map { system ->
            CountingSystemGuide(
                id = system.id,
                displayName = system.displayName,
                level = system.level,
                isBalanced = system.isBalanced,
                usesTrueCount = system.usesTrueCount(),
                recommendsAceSideCount = system.recommendsAceSideCount(),
                explanation = systemExplanation(system),
                tagRows = buildCheatSheetRows(system)
            )
        }
    }

    fun buildSplitDeviationSections(tableRules: TableRules): List<StrategyChartSection> {
        val noPeekOnTen = tableRules.dealerPeekRule == DealerPeekRule.ACE_ONLY ||
            tableRules.dealerPeekRule == DealerPeekRule.NO_PEEK
        val noPeekOnAce = tableRules.dealerPeekRule == DealerPeekRule.NO_PEEK

        val baseRows = mutableListOf(
            StrategyChartRow("10,10 vs 5", "Split at very high TC (commonly around +5)"),
            StrategyChartRow("10,10 vs 6", "Split at very high TC (commonly around +4)"),
            StrategyChartRow(
                "10 vs 10 (double deviation)",
                if (noPeekOnTen) {
                    "Avoid if no-peek on 10 is active."
                } else {
                    "Double only at high TC (commonly around +4)."
                }
            ),
            StrategyChartRow("9,9 vs 7", "Can shift by count/rules; use your index chart"),
            StrategyChartRow("All split deviations", "Rule and system dependent; verify table rules")
        )

        if (!tableRules.resplitAces) {
            baseRows.add(StrategyChartRow("A,A re-split", "Not allowed at this table."))
        }
        if (!tableRules.hitSplitAces) {
            baseRows.add(StrategyChartRow("After splitting A,A", "No hit after split aces."))
        }
        if (noPeekOnTen) {
            baseRows.add(StrategyChartRow("A,A vs 10", "Use no-peek-on-10 chart adjustments."))
        }
        if (noPeekOnAce) {
            baseRows.add(StrategyChartRow("A,A vs A", "Use no-peek-on-A chart adjustments."))
        }

        return listOf(
            StrategyChartSection(
                title = "Count-Based Split Deviations (Advanced, Hi-Lo style)",
                rows = baseRows
            )
        )
    }

    fun buildCheatSheetRows(system: CountingSystem): List<CheatSheetRow> {
        val grouped = CardRank.entries
            .groupBy { rank -> system.getTagValue(rank) }
            .toList()
            .sortedByDescending { (value, _) -> value }

        return grouped.map { (value, ranks) ->
            CheatSheetRow(
                cardsLabel = ranks.joinToString(", ") { rank -> rank.toCardLabel() },
                tagValueLabel = formatTagValue(value)
            )
        }
    }

    private fun systemExplanation(system: CountingSystem): String {
        return when (system.id) {
            HiLoCountingSystem.id -> "Most common beginner system: balanced +1/-1 tags with truncated true-count decisions."
            KOCountingSystem.id -> "Unbalanced running-count system using deck-specific initial running counts, key counts, and a pivot."
            ReKOCountingSystem.id -> "Refined KO variant with deck-specific initial running counts and key-count references."
            HiOptIICountingSystem.id -> "Higher-power level-2 system with truncated true-count decisions; strongest with an Ace side count."
            OmegaIICountingSystem.id -> "Advanced level-2 balanced count using truncated true-count decisions; often paired with Ace side count."
            ZenCountSystem.id -> "Balanced level-2 count with heavier 4-6 tags and truncated true-count decision handling."
            WongHalvesSystem.id -> "High-precision balanced system with half-point tags and truncated true-count decision handling."
            else -> "Card-counting system with custom tag values for betting and deviation decisions."
        }
    }

    private fun CardRank.toCardLabel(): String {
        return when (this) {
            CardRank.TWO -> "2"
            CardRank.THREE -> "3"
            CardRank.FOUR -> "4"
            CardRank.FIVE -> "5"
            CardRank.SIX -> "6"
            CardRank.SEVEN -> "7"
            CardRank.EIGHT -> "8"
            CardRank.NINE -> "9"
            CardRank.TEN -> "10"
            CardRank.JACK -> "J"
            CardRank.QUEEN -> "Q"
            CardRank.KING -> "K"
            CardRank.ACE -> "A"
        }
    }
}
