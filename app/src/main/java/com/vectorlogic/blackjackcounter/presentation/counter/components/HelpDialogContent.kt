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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.CountingSystemGuide
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules

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
                text = "The Strategy Chart and cheat sheet follow the current table rules below.",
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
                text = "Strategy and Deviations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            GuideEntry(
                title = "Strategy Chart",
                body = "Button on the main screen. Basic strategy for the current table rules, with " +
                    "count deviations applied at the current true count (marked *)."
            )
            GuideEntry(
                title = "Cheat Sheet",
                body = "Menu > Show Cheat Sheet. Tag values and every deviation index for the " +
                    "selected system, converted to its true count. Plays that apply now are highlighted."
            )
            GuideEntry(
                title = "Indices used",
                body = "Hi-Lo Illustrious 18 plus 15 vs A, and the Fab 4 surrenders when surrender " +
                    "is offered. KO and ReKO use their key count and pivot instead."
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
                LabelValueRow(
                    label = tagRow.cardsLabel,
                    value = tagRow.tagValueLabel,
                    valueWeight = FontWeight.Bold
                )
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
