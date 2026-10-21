package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {}
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
        // Landscape phones, foldables and tablets: a single stretched column pushes the pad
        // below the fold, so split into stats on the left and the pad on the right. Small
        // phones in landscape can be under 600 dp once the camera cutout is inset, so any
        // reasonably wide landscape window splits too.
        val twoPane = maxWidth >= TWO_PANE_MIN_WIDTH ||
            (maxWidth > maxHeight && maxWidth >= LANDSCAPE_TWO_PANE_MIN_WIDTH)
        val sectionSpacing = if (compactLayout) 4.dp else 6.dp
        val horizontalPadding = if (compactLayout) 8.dp else 10.dp
        val verticalPadding = if (compactLayout) 4.dp else 6.dp

        val header: @Composable () -> Unit = {
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
        }
        val counts: @Composable () -> Unit = {
            CountCards(state = state, compact = compactLayout)
        }
        val bet: @Composable () -> Unit = {
            BetRecommendationCard(
                units = state.recommendedBetUnits,
                countLabel = state.betSizingCountLabel,
                countValue = state.betSizingCountValue,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val chartButton: @Composable () -> Unit = {
            StrategyChartButton(
                onClick = onStrategyChartClick,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val pad: @Composable () -> Unit = {
            CountButtonsRow(
                buttonTagValues = state.buttonTagValues,
                latestCardLabels = state.seenCards
                    .takeLast(PAD_LATEST_CARDS)
                    .map { it.rank.toPadLabel() },
                canUndo = state.seenCards.isNotEmpty(),
                onCardSelected = onCardSelected,
                onFaceCardsSelected = onFaceCardsSelected,
                onUndo = onUndo,
                compact = compactLayout
            )
        }
        val shoe: @Composable () -> Unit = {
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
        }
        val recentCards: @Composable () -> Unit = {
            RecentCardsCard(
                recentCardLabels = state.seenCards
                    .takeLast(if (compactLayout) 12 else 15)
                    .map { it.rank.toPadLabel() },
                totalCardsSeen = state.seenCards.size,
                compact = compactLayout
            )
        }
        val reset: @Composable () -> Unit = {
            ResetButton(
                onReset = onResetClick,
                compact = compactLayout,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (twoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(horizontalPadding)
            ) {
                // Stats side. The snackbar sits here so it never covers the pad.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                    ) {
                        header()
                        counts()
                        bet()
                        shoe()
                        reset()
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) { snackbarHost() }
                }
                // Input side: pad and Undo first, so they're always on screen.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    pad()
                    chartButton()
                    recentCards()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                header()
                counts()
                bet()
                chartButton()
                pad()
                shoe()
                recentCards()
                reset()
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter)) { snackbarHost() }
        }
    }
}

private val TWO_PANE_MIN_WIDTH = 600.dp
private val LANDSCAPE_TWO_PANE_MIN_WIDTH = 480.dp

// Cards shown in the pad header next to Undo; the strip drops older ones on narrow screens.
private const val PAD_LATEST_CARDS = 6

@Composable
private fun CountCards(state: CounterUiState, compact: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)) {
        CountDisplayCard(
            title = "Running",
            value = formatSignedCount(state.runningCount),
            valueColor = countColor(state.runningCount),
            hero = true,
            compact = compact,
            modifier = Modifier.fillMaxWidth()
        )

        val trueCountValue = if (state.selectedSystemUsesTrueCount) state.trueCount else 0.0
        val edgeValue = if (state.canEstimateEdge) state.estimatedPlayerEdgePercent else 0.0

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
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
                compact = compact,
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
                compact = compact,
                modifier = Modifier.weight(1f)
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
