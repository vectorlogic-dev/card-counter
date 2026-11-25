package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.presentation.asString
import com.vectorlogic.blackjackcounter.presentation.counter.CountingSystemGuide
import com.vectorlogic.blackjackcounter.presentation.counter.systemTraitsLabel

@Composable
fun HelpDialogContent(
    countingSystemGuides: List<CountingSystemGuide>,
    tableRules: TableRules,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HelpHeaderCard(tableRules = tableRules)
        CurrentRulesCard(tableRules = tableRules)
        StrategyGuideCard()

        countingSystemGuides.forEach { guide ->
            CountingSystemGuideCard(guide = guide)
        }
    }
}

@Composable
private fun HelpHeaderCard(tableRules: TableRules) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.help_reference_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.help_reference_line_1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.help_reference_line_2),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.help_reference_line_3),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.help_current_core_rule,
                    tableRules.dealerSoft17Rule.toLabel(),
                    tableRules.dealerPeekRule.toLabel()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrentRulesCard(tableRules: TableRules) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.help_current_rules),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            RuleLine(stringResource(R.string.help_rule_dealer_soft_17), tableRules.dealerSoft17Rule.toLabel())
            RuleLine(stringResource(R.string.help_rule_dealer_peek), tableRules.dealerPeekRule.toLabel())
            RuleLine(stringResource(R.string.help_rule_blackjack_payout), tableRules.blackjackPayoutRule.toLabel())
            RuleLine(stringResource(R.string.help_rule_double_down), tableRules.doubleDownRule.toLabel())
            RuleLine(stringResource(R.string.help_rule_double_after_split), allowedLabel(tableRules.doubleAfterSplit))
            RuleLine(stringResource(R.string.action_surrender), tableRules.surrenderRule.toLabel())
            RuleLine(stringResource(R.string.help_rule_resplit_aces), allowedLabel(tableRules.resplitAces))
            RuleLine(stringResource(R.string.help_rule_hit_split_aces), allowedLabel(tableRules.hitSplitAces))
            RuleLine(
                stringResource(R.string.help_rule_six_card_charlie),
                stringResource(if (tableRules.sixCardCharlieAutoWin) R.string.enabled else R.string.disabled)
            )
            RuleLine(stringResource(R.string.help_rule_max_split_hands), tableRules.maxSplitHands.toString())
        }
    }
}

@Composable
private fun allowedLabel(allowed: Boolean): String =
    stringResource(if (allowed) R.string.allowed else R.string.not_allowed)

@Composable
private fun RuleLine(label: String, value: String) {
    LabelValueRow(label = label, value = value)
}

@Composable
private fun StrategyGuideCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.help_strategy_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            GuideEntry(
                title = stringResource(R.string.strategy_chart),
                body = stringResource(R.string.help_strategy_chart_body)
            )
            GuideEntry(
                title = stringResource(R.string.help_cheat_sheet),
                body = stringResource(R.string.help_cheat_sheet_body)
            )
            GuideEntry(
                title = stringResource(R.string.quiz_title),
                body = stringResource(R.string.help_quiz_body)
            )
            GuideEntry(
                title = stringResource(R.string.help_indices_used),
                body = stringResource(R.string.help_indices_used_body)
            )
        }
    }
}

@Composable
private fun GuideEntry(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CountingSystemGuideCard(guide: CountingSystemGuide) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = guide.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = systemTraitsLabel(
                    level = guide.level,
                    isBalanced = guide.isBalanced,
                    usesTrueCount = guide.usesTrueCount
                ).asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = guide.explanation.asString(),
                style = MaterialTheme.typography.bodySmall
            )
            if (guide.recommendsAceSideCount) {
                Text(
                    text = stringResource(R.string.help_ace_side_count_recommended),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            HorizontalDivider()
            guide.tagRows.forEach { tagRow ->
                LabelValueRow(
                    label = tagRow.cardsLabel,
                    value = tagRow.tagValueLabel,
                    valueWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DealerSoft17Rule.toLabel(): String = stringResource(
    when (this) {
        DealerSoft17Rule.STAND -> R.string.help_s17
        DealerSoft17Rule.HIT -> R.string.help_h17
    }
)

@Composable
private fun DealerPeekRule.toLabel(): String = stringResource(
    when (this) {
        DealerPeekRule.ACE_AND_TEN -> R.string.rules_peek_ace_and_ten
        DealerPeekRule.ACE_ONLY -> R.string.rules_peek_ace_only
        DealerPeekRule.NO_PEEK -> R.string.help_no_peek
    }
)

private fun BlackjackPayoutRule.toLabel(): String = when (this) {
    BlackjackPayoutRule.THREE_TO_TWO -> "3:2"
    BlackjackPayoutRule.SIX_TO_FIVE -> "6:5"
}

@Composable
private fun DoubleDownRule.toLabel(): String = stringResource(
    when (this) {
        DoubleDownRule.ANY_TWO_CARDS -> R.string.rules_double_any_two
        DoubleDownRule.NINE_TO_ELEVEN -> R.string.rules_double_nine_to_eleven
        DoubleDownRule.TEN_TO_ELEVEN -> R.string.rules_double_ten_to_eleven
    }
)

@Composable
private fun SurrenderRule.toLabel(): String = stringResource(
    when (this) {
        SurrenderRule.NONE -> R.string.rules_none
        SurrenderRule.LATE -> R.string.help_surrender_late
        SurrenderRule.EARLY -> R.string.help_surrender_early
    }
)
