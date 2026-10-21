package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyChartModel
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyTableSection
import com.vectorlogic.blackjackcounter.ui.theme.ChartDouble
import com.vectorlogic.blackjackcounter.ui.theme.ChartHit
import com.vectorlogic.blackjackcounter.ui.theme.ChartOnDouble
import com.vectorlogic.blackjackcounter.ui.theme.ChartOnHit
import com.vectorlogic.blackjackcounter.ui.theme.ChartOnSplit
import com.vectorlogic.blackjackcounter.ui.theme.ChartOnStand
import com.vectorlogic.blackjackcounter.ui.theme.ChartOnSurrender
import com.vectorlogic.blackjackcounter.ui.theme.ChartSplit
import com.vectorlogic.blackjackcounter.ui.theme.ChartStand
import com.vectorlogic.blackjackcounter.ui.theme.ChartSurrender

private val DEALER_UPCARDS = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "A")
private val CELL_SHAPE = RoundedCornerShape(2.dp)

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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StrategyChartLegend(supportsTrueCount = model.supportsTrueCount)
                model.sections.forEach { section ->
                    StrategyTableSectionCard(section = section)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StrategyChartLegend(supportsTrueCount: Boolean) {
    ChartCard {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (supportsTrueCount) {
                    "Actions update with the current true count."
                } else {
                    "Running-count system selected. Deviations are disabled."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Each entry is one unit (swatch + meaning), so the line wraps between entries,
            // never inside one.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LEGEND_ACTIONS.forEach { action ->
                    LegendEntry(action = action, isDeviation = false, meaning = actionMeaning(action))
                }
                if (supportsTrueCount) {
                    LegendEntry(action = "S", isDeviation = true, meaning = "Count deviation")
                }
            }
        }
    }
}

private val LEGEND_ACTIONS = listOf("H", "S", "D", "Ds", "P", "Rh")

@Composable
private fun LegendEntry(action: String, isDeviation: Boolean, meaning: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionCell(
            action = action,
            isDeviation = isDeviation,
            description = null,
            modifier = Modifier.width(30.dp)
        )
        Text(
            text = meaning,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun StrategyTableSectionCard(section: StrategyTableSection) {
    ChartCard {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TableHeaderCell(text = "Hand", modifier = Modifier.weight(1.7f))
                DEALER_UPCARDS.forEach { header ->
                    TableHeaderCell(text = header, modifier = Modifier.weight(1f))
                }
            }

            section.rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TableHandCell(text = row.handLabel, modifier = Modifier.weight(1.7f))
                    row.cells.forEachIndexed { index, cell ->
                        val upcard = DEALER_UPCARDS.getOrNull(index).orEmpty()
                        ActionCell(
                            action = cell.action,
                            isDeviation = cell.isDeviation,
                            description = buildString {
                                append("${row.handLabel} vs $upcard: ${actionMeaning(cell.action)}")
                                if (cell.isDeviation) append(", count deviation")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CELL_SHAPE)
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
            .clip(CELL_SHAPE)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * One action cell. The letter is always shown, the fill identifies the action family, and a
 * count deviation adds both a '*' and an outline in the letter's colour.
 */
@Composable
private fun ActionCell(
    action: String,
    isDeviation: Boolean,
    description: String?,
    modifier: Modifier = Modifier
) {
    val (fill, onFill) = actionColors(action)
    val base = modifier
        .clip(CELL_SHAPE)
        .background(fill)
        .then(if (isDeviation) Modifier.border(1.5.dp, onFill, CELL_SHAPE) else Modifier)
    Box(
        modifier = (if (description != null) base.clearAndSetSemantics { contentDescription = description } else base)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isDeviation) "$action*" else action,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = onFill,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun actionColors(action: String): Pair<Color, Color> = when {
    action.startsWith("S") -> ChartStand to ChartOnStand
    action.startsWith("D") -> ChartDouble to ChartOnDouble
    action.startsWith("P") -> ChartSplit to ChartOnSplit
    action.startsWith("R") -> ChartSurrender to ChartOnSurrender
    else -> ChartHit to ChartOnHit
}

private fun actionMeaning(action: String): String = when (action) {
    "H" -> "Hit"
    "S" -> "Stand"
    "D" -> "Double, else hit"
    "Ds" -> "Double, else stand"
    "P" -> "Split"
    "Rh" -> "Surrender, else hit"
    else -> action
}
