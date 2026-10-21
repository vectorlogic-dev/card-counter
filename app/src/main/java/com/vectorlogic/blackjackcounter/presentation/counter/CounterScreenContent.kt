package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.presentation.counter.components.BetRecommendationCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountButtonsRow
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountDisplayCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.DeckControlCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.RecentCardsCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.ResetButton
import com.vectorlogic.blackjackcounter.presentation.counter.components.StrategyChartButton
import com.vectorlogic.blackjackcounter.ui.theme.FeltGreen
import com.vectorlogic.blackjackcounter.ui.theme.GoldAccent
import com.vectorlogic.blackjackcounter.ui.theme.GoldBright
import com.vectorlogic.blackjackcounter.ui.theme.GoldSoft
import com.vectorlogic.blackjackcounter.ui.theme.SurfaceMuted
import com.vectorlogic.blackjackcounter.ui.theme.TableEdge
import java.util.Locale

@Composable
fun CounterScreenContent(
    state: CounterUiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onHelpClick: () -> Unit,
    onSelectCountingSystem: () -> Unit,
    onTableRulesClick: () -> Unit,
    onResetClick: () -> Unit,
    onToggleCheatSheet: () -> Unit,
    onTotalDecksChanged: (Int) -> Unit,
    onDecksRemainingChanged: (Double) -> Unit,
    onCardSelected: (CardRank) -> Unit,
    onFaceCardsSelected: () -> Unit,
    onUndo: () -> Unit,
    onStrategyChartClick: () -> Unit,
    onDrillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TableEdge,
                        FeltGreen.copy(alpha = 0.88f),
                        GoldAccent.copy(alpha = 0.16f),
                        SurfaceMuted,
                        TableEdge
                    )
                )
            )
    ) {
        val compactLayout = maxHeight < 900.dp
        val sectionSpacing = if (compactLayout) 4.dp else 6.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (compactLayout) 8.dp else 10.dp,
                    vertical = if (compactLayout) 4.dp else 6.dp
                ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {
            CounterHeader(
                systemName = state.selectedCountingSystemName,
                showsAceSideCount = state.selectedSystemRecommendsAceSideCount,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = onMenuExpandedChange,
                onHelpClick = onHelpClick,
                onSelectCountingSystem = onSelectCountingSystem,
                onTableRulesClick = onTableRulesClick,
                onResetClick = onResetClick,
                onToggleCheatSheet = onToggleCheatSheet,
                onDrillClick = onDrillClick,
                showCheatSheet = state.showCheatSheet,
                compact = compactLayout
            )

            CountDisplayCard(
                title = "Running",
                value = formatSignedCount(state.runningCount),
                valueColor = countColor(state.runningCount),
                hero = true,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )

            val trueCountValue = if (state.selectedSystemUsesTrueCount) state.trueCount else 0.0
            val edgeValue = if (state.canEstimateEdge) state.estimatedPlayerEdgePercent else 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compactLayout) 4.dp else 6.dp)
            ) {
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
                    compact = compactLayout,
                    modifier = Modifier.weight(1f)
                )

                CountDisplayCard(
                    title = "Edge %",
                    value = if (state.canEstimateEdge) {
                        formatSignedPercent(state.estimatedPlayerEdgePercent)
                    } else {
                        "N/A"
                    },
                    valueColor = if (state.canEstimateEdge) {
                        countColor(edgeValue)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    compact = compactLayout,
                    modifier = Modifier.weight(1f)
                )
            }

            BetRecommendationCard(
                units = state.recommendedBetUnits,
                countLabel = state.betSizingCountLabel,
                countValue = state.betSizingCountValue,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )

            StrategyChartButton(
                onClick = onStrategyChartClick,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )

            CountButtonsRow(
                buttonTagValues = state.buttonTagValues,
                onCardSelected = onCardSelected,
                onFaceCardsSelected = onFaceCardsSelected,
                compact = compactLayout
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
                onTotalDecksChanged = onTotalDecksChanged,
                onDecksRemainingChanged = onDecksRemainingChanged,
                compact = compactLayout
            )

            RecentCardsCard(
                recentCardLabels = state.seenCards
                    .takeLast(if (compactLayout) 12 else 15)
                    .map { it.rank.toPadLabel() },
                onUndo = onUndo,
                canUndo = state.seenCards.isNotEmpty(),
                compact = compactLayout
            )

            ResetButton(
                onReset = onResetClick,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}

@Composable
private fun CounterHeader(
    systemName: String,
    showsAceSideCount: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onHelpClick: () -> Unit,
    onSelectCountingSystem: () -> Unit,
    onTableRulesClick: () -> Unit,
    onResetClick: () -> Unit,
    onToggleCheatSheet: () -> Unit,
    onDrillClick: () -> Unit,
    showCheatSheet: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 2.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.app_name_short).uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                color = GoldSoft
            )
            Text(
                text = systemName,
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright
            )
            if (showsAceSideCount) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Ace side count",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Box {
                IconButton(onClick = { onMenuExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Select Counting System") },
                        onClick = {
                            onSelectCountingSystem()
                            onMenuExpandedChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Table Rules") },
                        onClick = {
                            onTableRulesClick()
                            onMenuExpandedChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Count") },
                        onClick = {
                            onResetClick()
                            onMenuExpandedChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (showCheatSheet) "Hide Cheat Sheet" else "Show Cheat Sheet") },
                        onClick = {
                            onToggleCheatSheet()
                            onMenuExpandedChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Drill Mode") },
                        onClick = {
                            onDrillClick()
                            onMenuExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}
