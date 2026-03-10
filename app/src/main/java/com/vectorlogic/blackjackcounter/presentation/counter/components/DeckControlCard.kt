package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

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
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val minDecksRemaining = 0.25f
    val sliderMax = totalDecks.toFloat().coerceAtLeast(minDecksRemaining)
    val sliderSteps = (((sliderMax - minDecksRemaining) / 0.25f).roundToInt() - 1).coerceAtLeast(0)
    val aceDiff = acesSeen - acesExpected
    val aceStatusColor = when {
        aceDiff > 1.0 -> MaterialTheme.colorScheme.error
        aceDiff < -1.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val aceOutlook = calculateAceOutlook(acesSeen = acesSeen, acesExpected = acesExpected)

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deck Controls",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Penetration ${formatPenetration(deckPenetrationPercent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardsMetricChip(
                    label = "Drawn",
                    value = cardsDrawn.toString(),
                    modifier = Modifier.weight(1f)
                )
                CardsMetricChip(
                    label = "Left",
                    value = cardsLeft.toString(),
                    modifier = Modifier.weight(1f)
                )
                CardsMetricChip(
                    label = "Total",
                    value = totalCards.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (showAceSideCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardsMetricChip(
                        label = "Aces Seen",
                        value = acesSeen.toString(),
                        valueColor = aceStatusColor,
                        modifier = Modifier.weight(1f)
                    )
                    CardsMetricChip(
                        label = "Aces Expected",
                        value = formatAcesExpected(acesExpected),
                        valueColor = aceStatusColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = aceOutlook,
                    style = MaterialTheme.typography.labelSmall,
                    color = aceStatusColor
                )
            }

            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = !menuExpanded }
            ) {
                TextField(
                    value = totalDecks.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total Decks in Shoe") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    (1..8).forEach { deckOption ->
                        DropdownMenuItem(
                            text = { Text(deckOption.toString()) },
                            onClick = {
                                onTotalDecksChanged(deckOption)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
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
    acesExpected: Double
): String {
    val diff = acesSeen - acesExpected

    return when {
        diff > 1.0 -> "Ace outlook: Fewer aces likely ahead (seen above expected)."
        diff < -1.0 -> "Ace outlook: More aces likely ahead (seen below expected)."
        else -> "Ace outlook: About average Ace frequency."
    }
}
