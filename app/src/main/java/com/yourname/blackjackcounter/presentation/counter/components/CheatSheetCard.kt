package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.blackjackcounter.domain.model.CheatSheetRow
import com.yourname.blackjackcounter.domain.model.StrategyDeviationRow

@Composable
fun CheatSheetCard(
    systemName: String,
    rows: List<CheatSheetRow>,
    strategyRows: List<StrategyDeviationRow>,
    showTrueCountDisclaimer: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$systemName Cheat Sheet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            rows.forEach { row ->
                CheatSheetRow(cards = row.cardsLabel, value = row.tagValueLabel)
            }

            if (strategyRows.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Strategy Deviations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "System-aware guide. Verify rules and deck count before using live.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                strategyRows.forEach { row ->
                    CheatSheetRow(cards = row.playLabel, value = row.indexLabel)
                }
            } else if (showTrueCountDisclaimer) {
                HorizontalDivider()
                Text(
                    text = "Strategy deviations hidden for running-count systems.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CheatSheetRow(cards: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = cards,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
