package com.vectorlogic.blackjackcounter.presentation.counter

import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.KOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.OmegaIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.ReKOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.WongHalvesSystem
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.presentation.UiText
import com.vectorlogic.blackjackcounter.presentation.uiText

object CounterHelpContentFactory {

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

    private fun systemExplanation(system: CountingSystem): UiText = uiText(
        when (system.id) {
            HiLoCountingSystem.id -> R.string.system_explanation_hi_lo
            KOCountingSystem.id -> R.string.system_explanation_ko
            ReKOCountingSystem.id -> R.string.system_explanation_reko
            HiOptIICountingSystem.id -> R.string.system_explanation_hi_opt_ii
            OmegaIICountingSystem.id -> R.string.system_explanation_omega_ii
            ZenCountSystem.id -> R.string.system_explanation_zen_count
            WongHalvesSystem.id -> R.string.system_explanation_wong_halves
            else -> R.string.system_explanation_other
        }
    )

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
