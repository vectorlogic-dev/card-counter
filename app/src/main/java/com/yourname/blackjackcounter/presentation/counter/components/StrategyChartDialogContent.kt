package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.blackjackcounter.presentation.counter.StrategyChartModel
import com.yourname.blackjackcounter.presentation.counter.StrategyTableCell
import com.yourname.blackjackcounter.presentation.counter.StrategyTableSection

@Composable
fun StrategyChartFullScreen(
    model: StrategyChartModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                Text(
                    text = "Strategy Chart (${model.trueCountLabel})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Box(modifier = Modifier.padding(end = 8.dp))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (model.supportsTrueCount) {
                            "Actions update with current true count. '*' marks deviations."
                        } else {
                            "Running-count system selected. Deviations are disabled."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "H Hit | S Stand | D Double | Ds Double/Stand | P Split | Rh Surrender/Hit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                model.sections.forEach { section ->
                    StrategyTableSectionCard(section = section)
                }
            }
        }
    }
}

@Composable
private fun StrategyTableSectionCard(section: StrategyTableSection) {
    val dealerHeaders = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "A")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                TableHeaderCell(text = "Hand", modifier = Modifier.weight(1.7f))
                dealerHeaders.forEach { header ->
                    TableHeaderCell(text = header, modifier = Modifier.weight(1f))
                }
            }

            section.rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    TableHandCell(text = row.handLabel, modifier = Modifier.weight(1.7f))
                    row.cells.forEach { cell ->
                        TableActionCell(cell = cell, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TableHandCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TableActionCell(cell: StrategyTableCell, modifier: Modifier = Modifier) {
    val bgColor = when {
        cell.action.startsWith("P") -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        cell.action.startsWith("D") -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)
        cell.action.startsWith("S") -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f)
        cell.action.startsWith("R") -> MaterialTheme.colorScheme.error.copy(alpha = 0.24f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val text = if (cell.isDeviation) "${cell.action}*" else cell.action
    val textColor = if (cell.isDeviation) MaterialTheme.colorScheme.primary else Color.Unspecified

    Box(
        modifier = modifier
            .background(bgColor)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
