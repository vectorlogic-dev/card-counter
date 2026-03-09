package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.blackjackcounter.presentation.counter.CountingSystemGuide
import com.yourname.blackjackcounter.presentation.counter.StrategyChartSection
import com.yourname.blackjackcounter.domain.model.BlackjackPayoutRule
import com.yourname.blackjackcounter.domain.model.DealerPeekRule
import com.yourname.blackjackcounter.domain.model.DealerSoft17Rule
import com.yourname.blackjackcounter.domain.model.DoubleDownRule
import com.yourname.blackjackcounter.domain.model.SurrenderRule
import com.yourname.blackjackcounter.domain.model.TableRules

@Composable
fun HelpDialogContent(
    countingSystemGuides: List<CountingSystemGuide>,
    basicStrategySections: List<StrategyChartSection>,
    splitDeviationSections: List<StrategyChartSection>,
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

        countingSystemGuides.forEach { guide ->
            CountingSystemGuideCard(guide = guide)
        }

        BasicStrategyChartCard(sections = basicStrategySections)
        SplitDeviationChartCard(sections = splitDeviationSections)
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
                text = "Counting Systems Reference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Each system shows level, balance, true-count usage, and tag values.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Each card also includes a quick summary of how that system is typically used.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Strategy chart is baseline. Use current table rules below for context.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Current core rule: ${tableRules.dealerSoft17Rule.toLabel()}, " +
                    "${tableRules.dealerPeekRule.toLabel()}.",
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
                text = "Current Table Rules",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            RuleLine("Dealer soft 17", tableRules.dealerSoft17Rule.toLabel())
            RuleLine("Dealer peek", tableRules.dealerPeekRule.toLabel())
            RuleLine("Blackjack payout", tableRules.blackjackPayoutRule.toLabel())
            RuleLine("Double down", tableRules.doubleDownRule.toLabel())
            RuleLine("Double after split", if (tableRules.doubleAfterSplit) "Allowed" else "Not allowed")
            RuleLine("Surrender", tableRules.surrenderRule.toLabel())
            RuleLine("Resplit aces", if (tableRules.resplitAces) "Allowed" else "Not allowed")
            RuleLine("Hit split aces", if (tableRules.hitSplitAces) "Allowed" else "Not allowed")
            RuleLine("6-card Charlie", if (tableRules.sixCardCharlieAutoWin) "Enabled" else "Disabled")
            RuleLine("Max split hands", tableRules.maxSplitHands.toString())
        }
    }
}

@Composable
private fun RuleLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
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
                text = "Level ${guide.level} | ${if (guide.isBalanced) "Balanced" else "Unbalanced"} | " +
                    "${if (guide.usesTrueCount) "Uses True Count" else "Running Count Only"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = guide.explanation,
                style = MaterialTheme.typography.bodySmall
            )
            if (guide.recommendsAceSideCount) {
                Text(
                    text = "Ace side count recommended",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            HorizontalDivider()
            guide.tagRows.forEach { tagRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tagRow.cardsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tagRow.tagValueLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicStrategyChartCard(sections: List<StrategyChartSection>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Basic Strategy Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            sections.forEach { section ->
                HorizontalDivider()
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                section.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = row.hand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = row.action,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitDeviationChartCard(sections: List<StrategyChartSection>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Split Deviations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            sections.forEach { section ->
                HorizontalDivider()
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                section.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = row.hand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = row.action,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun DealerSoft17Rule.toLabel(): String = when (this) {
    DealerSoft17Rule.STAND -> "S17 (Dealer stands on soft 17)"
    DealerSoft17Rule.HIT -> "H17 (Dealer hits soft 17)"
}

private fun DealerPeekRule.toLabel(): String = when (this) {
    DealerPeekRule.ACE_AND_TEN -> "Peek on Ace and 10"
    DealerPeekRule.ACE_ONLY -> "Peek on Ace only (no peek on 10)"
    DealerPeekRule.NO_PEEK -> "No peek"
}

private fun BlackjackPayoutRule.toLabel(): String = when (this) {
    BlackjackPayoutRule.THREE_TO_TWO -> "3:2"
    BlackjackPayoutRule.SIX_TO_FIVE -> "6:5"
}

private fun DoubleDownRule.toLabel(): String = when (this) {
    DoubleDownRule.ANY_TWO_CARDS -> "Any 2 cards"
    DoubleDownRule.NINE_TO_ELEVEN -> "9-11 only"
    DoubleDownRule.TEN_TO_ELEVEN -> "10-11 only"
}

private fun SurrenderRule.toLabel(): String = when (this) {
    SurrenderRule.NONE -> "None"
    SurrenderRule.LATE -> "Late"
    SurrenderRule.EARLY -> "Early"
}
