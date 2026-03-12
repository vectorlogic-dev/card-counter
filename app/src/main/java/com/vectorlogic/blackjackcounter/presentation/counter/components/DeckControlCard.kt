package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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

@OptIn(ExperimentalMaterial3Api::class)
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
    compact: Boolean = false,
    modifier: Modifier = Modifier
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

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deck Controls",
                    style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Penetration ${formatPenetration(deckPenetrationPercent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
            ) {
                CardsMetricChip(
                    label = "Drawn",
                    value = cardsDrawn.toString(),
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
                CardsMetricChip(
                    label = "Left",
                    value = cardsLeft.toString(),
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
                CardsMetricChip(
                    label = "Total",
                    value = totalCards.toString(),
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showAceSideCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
                ) {
                    CardsMetricChip(
                        label = "Aces Seen",
                        value = acesSeen.toString(),
                        valueColor = aceStatusColor,
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                    CardsMetricChip(
                        label = "Aces Expected",
                        value = formatAcesExpected(acesExpected),
                        valueColor = aceStatusColor,
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = aceOutlook,
                    style = MaterialTheme.typography.labelSmall,
                    color = aceStatusColor
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)) {
                Text(
                    text = "Decks in Shoe",
                    style = MaterialTheme.typography.bodySmall,
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

            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Decks Remaining",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatDecksRemaining(decksRemaining),
                        style = MaterialTheme.typography.bodyMedium,
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
    valueColor: Color = Color.Unspecified,
    compact: Boolean = false,
    modifier: Modifier = Modifier
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
                vertical = if (compact) 4.dp else 6.dp
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
        diff > 1.0 -> if (compact) "Ace outlook: fewer ahead." else "Ace outlook: Fewer aces likely ahead (seen above expected)."
        diff < -1.0 -> if (compact) "Ace outlook: more ahead." else "Ace outlook: More aces likely ahead (seen below expected)."
        else -> if (compact) "Ace outlook: neutral." else "Ace outlook: About average Ace frequency."
    }
}
