package com.yourname.blackjackcounter.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.blackjackcounter.presentation.counter.components.CheatSheetCard
import com.yourname.blackjackcounter.presentation.counter.components.CountButtonsRow
import com.yourname.blackjackcounter.presentation.counter.components.CountDisplayCard
import com.yourname.blackjackcounter.presentation.counter.components.DeckControlCard
import com.yourname.blackjackcounter.presentation.counter.components.ResetButton
import com.yourname.blackjackcounter.ui.theme.FeltGreen
import com.yourname.blackjackcounter.ui.theme.GoldAccent
import com.yourname.blackjackcounter.ui.theme.TableEdge
import java.util.Locale
import kotlin.math.round

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TableEdge, FeltGreen.copy(alpha = 0.65f), GoldAccent.copy(alpha = 0.1f), TableEdge)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Blackjack Counter",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "System: ${state.selectedCountingSystemName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            TextButton(onClick = { menuExpanded = true }) {
                                Text("Menu")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Reset Count") },
                                    onClick = {
                                        viewModel.resetCount()
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset Shoe") },
                                    onClick = {
                                        viewModel.resetShoe()
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (state.showCheatSheet) "Hide Cheat Sheet"
                                            else "Show Cheat Sheet"
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleCheatSheet()
                                        menuExpanded = false
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Counting System") },
                                    onClick = {},
                                    enabled = false
                                )
                                viewModel.availableSystems.forEach { system ->
                                    val selectedMark = if (system.id == state.selectedCountingSystemId) "[x] " else ""
                                    DropdownMenuItem(
                                        text = { Text("$selectedMark${system.displayName}") },
                                        onClick = {
                                            viewModel.setCountingSystem(system.id)
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountDisplayCard(
                        title = "Running",
                        value = formatSignedCount(state.runningCount),
                        valueColor = countColor(state.runningCount),
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )

                    val trueCountValue = if (state.selectedSystemUsesTrueCount) {
                        state.trueCount
                    } else {
                        0.0
                    }
                    CountDisplayCard(
                        title = "True",
                        value = if (state.selectedSystemUsesTrueCount) {
                            formatSignedCount(state.trueCount, alwaysOneDecimal = true)
                        } else {
                            "N/A"
                        },
                        valueColor = if (state.selectedSystemUsesTrueCount) {
                            countColor(trueCountValue)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                DeckControlCard(
                    totalDecks = state.totalDecks,
                    decksRemaining = state.decksRemaining,
                    deckPenetrationPercent = state.deckPenetrationPercent,
                    cardsDrawn = state.cardsDrawn,
                    cardsLeft = state.cardsLeft,
                    totalCards = state.totalCards,
                    onTotalDecksChanged = viewModel::setTotalDecks,
                    onDecksRemainingChanged = viewModel::setDecksRemaining
                )

                Text(
                    text = "Tap each card rank as it appears",
                    style = MaterialTheme.typography.titleSmall
                )

                CountButtonsRow(
                    onCardSelected = viewModel::onCardSeen,
                    onFaceCardsSelected = viewModel::onFaceCardsSeen
                )

                ResetButton(onReset = viewModel::resetCount)
            }

            if (state.showCheatSheet) {
                AlertDialog(
                    onDismissRequest = { viewModel.setCheatSheetVisible(false) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.setCheatSheetVisible(false) }) {
                            Text("Close")
                        }
                    },
                    text = {
                        CheatSheetCard(
                            systemName = state.selectedCountingSystemName,
                            rows = state.cheatSheetRows
                        )
                    }
                )
            }
        }
    }
}

private fun formatSignedCount(
    value: Double,
    alwaysOneDecimal: Boolean = false
): String {
    val rounded = round(value * 10.0) / 10.0
    val formatted = if (alwaysOneDecimal || rounded % 1.0 != 0.0) {
        String.format(Locale.US, "%.1f", rounded)
    } else {
        rounded.toInt().toString()
    }

    return if (rounded > 0) "+$formatted" else formatted
}

@Composable
private fun countColor(value: Double): Color {
    return when {
        value > 0 -> MaterialTheme.colorScheme.tertiary
        value < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}
