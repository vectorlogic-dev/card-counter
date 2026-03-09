package com.yourname.blackjackcounter.presentation.counter.components

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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    val aceOutlook = calculateAceOutlook(totalDecks = totalDecks, acesSeen = acesSeen, acesExpected = acesExpected)

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deck Controls",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Penetration ${formatPenetration(deckPenetrationPercent)}",
                    style = MaterialTheme.typography.bodyMedium,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardsMetricChip(
                    label = "Aces Seen",
                    value = acesSeen.toString(),
                    modifier = Modifier.weight(1f)
                )
                CardsMetricChip(
                    label = "Aces Expected Left",
                    value = formatAcesExpected(acesExpected),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = aceOutlook,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        .menuAnchor()
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatDecksRemaining(decksRemaining),
                        style = MaterialTheme.typography.bodyLarge,
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall
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
    totalDecks: Int,
    acesSeen: Int,
    acesExpected: Double
): String {
    if (totalDecks <= 0) return "Ace outlook: N/A"

    val diff = acesSeen - acesExpected

    return when {
        diff > 0.5 -> "Ace outlook: Less likely than average to see an Ace."
        diff < -0.5 -> "Ace outlook: More likely than average to see an Ace."
        else -> "Ace outlook: About average Ace frequency."
    }
}
