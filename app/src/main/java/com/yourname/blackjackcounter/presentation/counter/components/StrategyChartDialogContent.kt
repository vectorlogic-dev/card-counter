package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun StrategyChartDialogContent(
    model: StrategyChartModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Blackjack Strategy Table (${model.trueCountLabel})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (model.supportsTrueCount) {
                        "Actions update with current true count. '*' marks count deviations."
                    } else {
                        "Running-count system selected. Count deviations are disabled."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Legend: H Hit | S Stand | D Double | Ds Double/Stand | P Split | Rh Surrender/Hit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        model.sections.forEach { section ->
            StrategyTableSectionCard(section = section)
        }
    }
}

@Composable
private fun StrategyTableSectionCard(section: StrategyTableSection) {
    val horizontalState = rememberScrollState()
    val dealerHeaders = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "A")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalState),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TableHeaderCell(text = "Hand", modifier = Modifier.width(78.dp))
                dealerHeaders.forEach { header ->
                    TableHeaderCell(text = header, modifier = Modifier.width(42.dp))
                }
            }

            section.rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalState),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TableHandCell(text = row.handLabel, modifier = Modifier.width(78.dp))
                    row.cells.forEach { cell ->
                        TableActionCell(cell = cell, modifier = Modifier.width(42.dp))
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
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TableHandCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
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
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
