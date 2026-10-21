package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentCardsCard(
    recentCardLabels: List<String>,
    totalCardsSeen: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 7.dp else 10.dp,
                vertical = if (compact) 6.dp else 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Cards",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (totalCardsSeen > 0) {
                    Text(
                        text = if (totalCardsSeen > recentCardLabels.size) {
                            "last ${recentCardLabels.size} of $totalCardsSeen, newest last"
                        } else {
                            "newest last"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (recentCardLabels.isEmpty()) {
                Text(
                    text = "No cards yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    recentCardLabels.forEachIndexed { index, label ->
                        RecentCardChip(
                            label = label,
                            newest = index == recentCardLabels.lastIndex,
                            compact = compact
                        )
                    }
                }
            }
        }
    }
}

/**
 * One seen card. The newest card is the one Undo removes, so it gets the gold fill and an
 * outline (shape as well as colour) plus a spoken "newest" for screen readers.
 */
@Composable
internal fun RecentCardChip(
    label: String,
    newest: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = if (newest) {
            modifier.clearAndSetSemantics { contentDescription = "$label, newest" }
        } else {
            modifier
        },
        shape = MaterialTheme.shapes.small,
        color = if (newest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        border = if (newest) BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 7.dp,
                vertical = if (compact) 2.dp else 3.dp
            ),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (newest) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Single-line strip of the latest cards, aligned to the end so the newest card always sits
 * next to Undo. Older cards that don't fit the available width are dropped.
 */
@Composable
internal fun LatestCardsStrip(
    labels: List<String>,
    compact: Boolean,
    modifier: Modifier = Modifier,
    spacing: Dp = 3.dp
) {
    Layout(
        modifier = modifier,
        content = {
            labels.forEachIndexed { index, label ->
                RecentCardChip(label = label, newest = index == labels.lastIndex, compact = compact)
            }
        }
    ) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(looseConstraints) }

        // Walk back from the newest card and keep as many as fit.
        var usedWidth = 0
        var firstShown = placeables.size
        for (index in placeables.indices.reversed()) {
            val extra = placeables[index].width + if (firstShown < placeables.size) gap else 0
            if (usedWidth + extra > constraints.maxWidth) break
            usedWidth += extra
            firstShown = index
        }
        val shown = placeables.subList(firstShown, placeables.size)
        val height = shown.maxOfOrNull { it.height } ?: 0
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else usedWidth

        layout(width, height.coerceAtLeast(constraints.minHeight)) {
            var x = width - usedWidth
            shown.forEach { placeable ->
                placeable.placeRelative(x, (height - placeable.height) / 2)
                x += placeable.width + gap
            }
        }
    }
}
