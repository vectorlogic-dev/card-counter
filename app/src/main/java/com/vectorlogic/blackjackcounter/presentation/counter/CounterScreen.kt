package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.presentation.counter.components.BetRecommendationCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.CheatSheetCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountButtonsRow
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountDisplayCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.DeckControlCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.HelpDialogContent
import com.vectorlogic.blackjackcounter.presentation.counter.components.RecentCardsCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.ResetButton
import com.vectorlogic.blackjackcounter.presentation.counter.components.StrategyChartButton
import com.vectorlogic.blackjackcounter.presentation.counter.components.StrategyChartFullScreen
import com.vectorlogic.blackjackcounter.presentation.counter.components.SystemSelector
import com.vectorlogic.blackjackcounter.presentation.counter.components.TableRulesDialogContent
import com.vectorlogic.blackjackcounter.ui.theme.FeltGreen
import com.vectorlogic.blackjackcounter.ui.theme.GoldAccent
import com.vectorlogic.blackjackcounter.ui.theme.TableEdge
import java.util.Locale
import kotlin.math.round

@Composable
fun CounterScreen() {
    val viewModel: CounterViewModel = viewModel(
        factory = remember { CounterViewModel.provideFactory() }
    )
    val state by viewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var showSystemSelector by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showStrategyChartDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showStrategyChartDialog) {
            StrategyChartFullScreen(
                model = viewModel.buildStrategyChartModel(state),
                onBack = { showStrategyChartDialog = false },
                modifier = Modifier.fillMaxSize()
            )
            return@Surface
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TableEdge,
                            FeltGreen.copy(alpha = 0.65f),
                            GoldAccent.copy(alpha = 0.1f),
                            TableEdge
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "BLACKJACK COUNTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent.copy(alpha = 0.6f)
                        )
                        Text(
                            text = state.selectedCountingSystemName,
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent
                        )
                        if (state.selectedSystemRecommendsAceSideCount) {
                            Text(
                                text = "Ace side count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Select Counting System") },
                                    onClick = { showSystemSelector = true; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Table Rules") },
                                    onClick = { showRulesDialog = true; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset Count") },
                                    onClick = { viewModel.resetCount(); menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(if (state.showCheatSheet) "Hide Cheat Sheet" else "Show Cheat Sheet")
                                    },
                                    onClick = { viewModel.toggleCheatSheet(); menuExpanded = false }
                                )
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
                        modifier = Modifier.weight(1f)
                    )

                    val trueCountValue = if (state.selectedSystemUsesTrueCount) state.trueCount else 0.0
                    CountDisplayCard(
                        title = "True",
                        value = if (state.selectedSystemUsesTrueCount) {
                            formatSignedCount(state.trueCount, alwaysOneDecimal = true)
                        } else "N/A",
                        valueColor = if (state.selectedSystemUsesTrueCount) countColor(trueCountValue)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    val edgeValue = if (state.canEstimateEdge) state.estimatedPlayerEdgePercent else 0.0
                    CountDisplayCard(
                        title = "Edge %",
                        value = if (state.canEstimateEdge) formatSignedPercent(state.estimatedPlayerEdgePercent)
                        else "N/A",
                        valueColor = if (state.canEstimateEdge) countColor(edgeValue)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                BetRecommendationCard(
                    units = state.recommendedBetUnits,
                    countLabel = state.betSizingCountLabel,
                    countValue = state.betSizingCountValue,
                    modifier = Modifier.fillMaxWidth()
                )

                DeckControlCard(
                    totalDecks = state.totalDecks,
                    decksRemaining = state.decksRemaining,
                    deckPenetrationPercent = state.deckPenetrationPercent,
                    cardsDrawn = state.cardsDrawn,
                    cardsLeft = state.cardsLeft,
                    totalCards = state.totalCards,
                    showAceSideCount = state.selectedSystemRecommendsAceSideCount,
                    acesSeen = state.acesSeen,
                    acesExpected = state.acesExpected,
                    onTotalDecksChanged = viewModel::setTotalDecks,
                    onDecksRemainingChanged = viewModel::setDecksRemaining
                )

                CountButtonsRow(
                    buttonTagValues = state.buttonTagValues,
                    onCardSelected = viewModel::onCardSeen,
                    onFaceCardsSelected = viewModel::onFaceCardsSeen
                )

                RecentCardsCard(
                    recentCardLabels = state.seenCards.takeLast(15).map { it.rank.toHistoryLabel() },
                    onUndo = viewModel::undoLastCard,
                    canUndo = state.seenCards.isNotEmpty()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResetButton(onReset = viewModel::resetCount, modifier = Modifier.weight(1f))
                    StrategyChartButton(
                        onClick = { showStrategyChartDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showSystemSelector) {
                CounterAlertDialog(
                    title = "Counting System",
                    onDismiss = { showSystemSelector = false }
                ) {
                    SystemSelector(
                        selectedSystemId = state.selectedCountingSystemId,
                        availableSystems = viewModel.availableSystems,
                        onSystemSelected = { systemId ->
                            viewModel.setCountingSystem(systemId)
                            showSystemSelector = false
                        }
                    )
                }
            }

            if (showHelpDialog) {
                CounterAlertDialog(
                    title = "Help",
                    onDismiss = { showHelpDialog = false }
                ) {
                    HelpDialogContent(
                        countingSystemGuides = viewModel.countingSystemGuides,
                        basicStrategySections = viewModel.basicStrategySections,
                        splitDeviationSections = viewModel.getSplitDeviationSections(state.tableRules),
                        tableRules = state.tableRules
                    )
                }
            }

            if (showRulesDialog) {
                CounterAlertDialog(
                    title = "Table Rules",
                    confirmLabel = "Done",
                    onDismiss = { showRulesDialog = false }
                ) {
                    TableRulesDialogContent(
                        rules = state.tableRules,
                        onDealerSoft17RuleChanged = viewModel::setDealerSoft17Rule,
                        onDealerPeekRuleChanged = viewModel::setDealerPeekRule,
                        onBlackjackPayoutRuleChanged = viewModel::setBlackjackPayoutRule,
                        onDoubleDownRuleChanged = viewModel::setDoubleDownRule,
                        onDoubleAfterSplitChanged = viewModel::setDoubleAfterSplit,
                        onSurrenderRuleChanged = viewModel::setSurrenderRule,
                        onResplitAcesChanged = viewModel::setResplitAces,
                        onHitSplitAcesChanged = viewModel::setHitSplitAces,
                        onSixCardCharlieAutoWinChanged = viewModel::setSixCardCharlieAutoWin,
                        onMaxSplitHandsChanged = viewModel::setMaxSplitHands
                    )
                }
            }

            if (state.showCheatSheet) {
                AlertDialog(
                    onDismissRequest = { viewModel.setCheatSheetVisible(false) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.setCheatSheetVisible(false) }) { Text("Close") }
                    },
                    text = {
                        CheatSheetCard(
                            systemName = state.selectedCountingSystemName,
                            rows = state.cheatSheetRows,
                            strategyRows = state.strategyDeviationRows,
                            showTrueCountDisclaimer = !state.selectedSystemUsesTrueCount
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CounterAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String = "Close",
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(confirmLabel) }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = content
    )
}

private fun CardRank.toHistoryLabel(): String = when (this) {
    CardRank.ACE -> "A"
    CardRank.TWO -> "2"
    CardRank.THREE -> "3"
    CardRank.FOUR -> "4"
    CardRank.FIVE -> "5"
    CardRank.SIX -> "6"
    CardRank.SEVEN -> "7"
    CardRank.EIGHT -> "8"
    CardRank.NINE -> "9"
    CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> "T"
}

private fun formatSignedCount(value: Double, alwaysOneDecimal: Boolean = false): String {
    val rounded = round(value * 10.0) / 10.0
    val formatted = if (alwaysOneDecimal || rounded % 1.0 != 0.0) {
        String.format(Locale.US, "%.1f", rounded)
    } else {
        rounded.toInt().toString()
    }
    return if (rounded > 0) "+$formatted" else formatted
}

private fun formatSignedPercent(value: Double): String {
    val rounded = round(value * 10.0) / 10.0
    val prefix = if (rounded > 0.0) "+" else ""
    return "$prefix${String.format(Locale.US, "%.1f", rounded)}"
}

@Composable
private fun countColor(value: Double): Color = when {
    value > 0 -> MaterialTheme.colorScheme.tertiary
    value < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}
