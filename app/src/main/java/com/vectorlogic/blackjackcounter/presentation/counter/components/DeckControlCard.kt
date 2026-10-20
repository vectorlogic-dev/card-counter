package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

private val DECK_OPTIONS = listOf(1, 2, 4, 6, 8)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeckControlCard(
    totalDecks: Int,
    decksRemaining: Double,
    deckPenetrationPercent: Double,
    cardsDrawn: Int,
    cardsLeft: Int,
    totalCards: Int,
    showAceSideCount: Boolean,
    acesSeen: Int,
    acesExpected: Double,
    onTotalDecksChanged: (Int) -> Unit,
    onDecksRemainingChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val minDecksRemaining = 0.25f
    val sliderMax = totalDecks.toFloat().coerceAtLeast(minDecksRemaining)
    val sliderSteps = (((sliderMax - minDecksRemaining) / 0.25f).roundToInt() - 1).coerceAtLeast(0)
    val aceDiff = acesSeen - acesExpected
    val aceStatusColor = when {
        aceDiff > 1.0 -> MaterialTheme.colorScheme.error
        aceDiff < -1.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val aceOutlook = calculateAceOutlook(
        acesSeen = acesSeen,
        acesExpected = acesExpected,
        compact = compact
    )

    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            shape = MaterialTheme.shapes.medium
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 7.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "SHOE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatDecksRemaining(decksRemaining)} decks",
                        style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Pen ${formatPenetration(deckPenetrationPercent)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp)
            ) {
                CardsMetricChip(
                    label = "Drawn",
                    value = cardsDrawn.toString(),
                    compact = true,
                    modifier = Modifier.weight(1f, fill = true)
                )
                CardsMetricChip(
                    label = "Left",
                    value = cardsLeft.toString(),
                    compact = true,
                    modifier = Modifier.weight(1f, fill = true)
                )
                CardsMetricChip(
                    label = "Total",
                    value = totalCards.toString(),
                    compact = true,
                    modifier = Modifier.weight(1f, fill = true)
                )
                if (showAceSideCount) {
                    CardsMetricChip(
                        label = "A Seen",
                        value = acesSeen.toString(),
                        valueColor = aceStatusColor,
                        compact = true,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                    CardsMetricChip(
                        label = "A Exp",
                        value = formatAcesExpected(acesExpected),
                        valueColor = aceStatusColor,
                        compact = true,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                }
            }

            if (showAceSideCount) {
                Text(
                    text = aceOutlook,
                    style = MaterialTheme.typography.labelSmall,
                    color = aceStatusColor
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Decks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DECK_OPTIONS.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = totalDecks == option,
                            onClick = { onTotalDecksChanged(option) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = DECK_OPTIONS.size
                            )
                        ) {
                            Text(option.toString())
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatDecksRemaining(decksRemaining),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = decksRemaining.toFloat().coerceIn(minDecksRemaining, sliderMax),
                    onValueChange = { raw ->
                        val snapped = (raw / 0.25f).roundToInt() * 0.25f
                        onDecksRemainingChanged(snapped.coerceIn(minDecksRemaining, sliderMax).toDouble())
                    },
                    valueRange = minDecksRemaining..sliderMax,
                    steps = sliderSteps
                )
            }
        }
    }
}

@Composable
private fun CardsMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    compact: Boolean = false
) {
    val resolvedValueColor = if (valueColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        valueColor
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 3.dp else 5.dp
            ),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = resolvedValueColor
            )
        }
    }
}

private fun formatDecksRemaining(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatPenetration(value: Double): String {
    return String.format(Locale.US, "%.1f%%", value)
}

private fun formatAcesExpected(value: Double): String {
    return String.format(Locale.US, "%.1f", value)
}

private fun calculateAceOutlook(
    acesSeen: Int,
    acesExpected: Double,
    compact: Boolean
): String {
    val diff = acesSeen - acesExpected

    return when {
        diff > 1.0 -> if (compact) "Ace outlook: fewer ahead." else "Ace outlook: fewer aces likely ahead."
        diff < -1.0 -> if (compact) "Ace outlook: more ahead." else "Ace outlook: more aces likely ahead."
        else -> if (compact) "Ace outlook: neutral." else "Ace outlook: near expected."
    }
}
