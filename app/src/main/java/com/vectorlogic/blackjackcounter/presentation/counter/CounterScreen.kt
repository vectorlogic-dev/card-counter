package com.vectorlogic.blackjackcounter.presentation.counter

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.EntitlementTier
import com.vectorlogic.blackjackcounter.presentation.counter.components.BetRecommendationCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.CheatSheetCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountButtonsRow
import com.vectorlogic.blackjackcounter.presentation.counter.components.CountDisplayCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.DeckControlCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.FullUpgradeDialogContent
import com.vectorlogic.blackjackcounter.presentation.counter.components.FreeTierBannerAd
import com.vectorlogic.blackjackcounter.presentation.counter.components.HelpDialogContent
import com.vectorlogic.blackjackcounter.presentation.counter.components.ProUpgradeDialogContent
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
    val context = LocalContext.current
    val appContext = context.applicationContext
    val viewModel: CounterViewModel = viewModel(
        factory = remember(appContext) {
            CounterViewModel.provideFactory(appContext)
        }
    )
    val state by viewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var showSystemSelector by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showStrategyChartDialog by remember { mutableStateOf(false) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var showFullUpgradeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.entitlementTier) {
        if (state.entitlementTier == EntitlementTier.PRO) {
            showProUpgradeDialog = false
        }
    }

    LaunchedEffect(state.hasFullUnlock) {
        if (state.hasFullUnlock) {
            showFullUpgradeDialog = false
        }
    }

    LaunchedEffect(showProUpgradeDialog) {
        if (showProUpgradeDialog) {
            viewModel.refreshMonetization()
        }
    }

    LaunchedEffect(showFullUpgradeDialog) {
        if (showFullUpgradeDialog) {
            viewModel.refreshMonetization()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
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
                        colors = listOf(TableEdge, FeltGreen.copy(alpha = 0.65f), GoldAccent.copy(alpha = 0.1f), TableEdge)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Blackjack Counter",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "System: ${state.selectedCountingSystemName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.hasFullUnlock) {
                                    "Full active (ads removed + all systems)"
                                } else if (state.entitlementTier == EntitlementTier.PRO) {
                                    "Pro active (ads removed)"
                                } else {
                                    "Free tier (banner ads)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.selectedSystemRecommendsAceSideCount) {
                                Text(
                                    text = "Ace side count recommended",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(onClick = { showHelpDialog = true }) {
                                Text("Help")
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
                                        text = { Text("Select Counting System") },
                                        onClick = {
                                            showSystemSelector = true
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Table Rules") },
                                        onClick = {
                                            showRulesDialog = true
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Reset Count") },
                                        onClick = {
                                            viewModel.resetCount()
                                            menuExpanded = false
                                        }
                                    )
                                    if (state.entitlementTier == EntitlementTier.FREE) {
                                        DropdownMenuItem(
                                            text = { Text("Upgrade to Pro") },
                                            onClick = {
                                                showProUpgradeDialog = true
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                    if (!state.hasFullUnlock) {
                                        DropdownMenuItem(
                                            text = { Text("Upgrade to Full") },
                                            onClick = {
                                                showFullUpgradeDialog = true
                                                menuExpanded = false
                                            }
                                        )
                                    }
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

                    val edgeValue = if (state.canEstimateEdge) {
                        state.estimatedPlayerEdgePercent
                    } else {
                        0.0
                    }
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
                        compact = true,
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

                Text(
                    text = "Tap each card rank as it appears",
                    style = MaterialTheme.typography.bodySmall
                )

                CountButtonsRow(
                    buttonTagValues = state.buttonTagValues,
                    onCardSelected = viewModel::onCardSeen,
                    onFaceCardsSelected = viewModel::onFaceCardsSeen
                )

                RecentCardsCard(
                    recentCardLabels = state.seenCards
                        .takeLast(15)
                        .map { it.rank.toHistoryLabel() },
                    onUndo = viewModel::undoLastCard,
                    canUndo = state.seenCards.isNotEmpty()
                )

                ResetButton(onReset = viewModel::resetCount)

                StrategyChartButton(onClick = { showStrategyChartDialog = true })

                FreeTierBannerAd(
                    isVisible = state.showAds,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showSystemSelector) {
                AlertDialog(
                    onDismissRequest = { showSystemSelector = false },
                    confirmButton = {
                        TextButton(onClick = { showSystemSelector = false }) {
                            Text("Close")
                        }
                    },
                    title = {
                        Text(
                            text = "Counting System",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        SystemSelector(
                            selectedSystemId = state.selectedCountingSystemId,
                            availableSystems = viewModel.availableSystems,
                            hasPro = state.entitlementTier == EntitlementTier.PRO,
                            hasFullUnlock = state.hasFullUnlock,
                            onSystemSelected = { systemId ->
                                when (viewModel.getSystemLockType(systemId)) {
                                    null -> {
                                        viewModel.setCountingSystem(systemId)
                                        showSystemSelector = false
                                    }
                                    SystemLockType.PRO -> {
                                        showSystemSelector = false
                                        showProUpgradeDialog = true
                                    }
                                    SystemLockType.FULL -> {
                                        showSystemSelector = false
                                        showFullUpgradeDialog = true
                                    }
                                }
                            }
                        )
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Close")
                        }
                    },
                    title = {
                        Text(
                            text = "Help",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        HelpDialogContent(
                            countingSystemGuides = viewModel.countingSystemGuides,
                            basicStrategySections = viewModel.basicStrategySections,
                            splitDeviationSections = viewModel.getSplitDeviationSections(state.tableRules),
                            tableRules = state.tableRules
                        )
                    }
                )
            }

            if (showRulesDialog) {
                AlertDialog(
                    onDismissRequest = { showRulesDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showRulesDialog = false }) {
                            Text("Done")
                        }
                    },
                    title = {
                        Text(
                            text = "Table Rules",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
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
                )
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
                            rows = state.cheatSheetRows,
                            strategyRows = state.strategyDeviationRows,
                            showTrueCountDisclaimer = !state.selectedSystemUsesTrueCount
                        )
                    }
                )
            }

            if (showProUpgradeDialog && state.entitlementTier == EntitlementTier.FREE) {
                AlertDialog(
                    onDismissRequest = { showProUpgradeDialog = false },
                    confirmButton = {
                        TextButton(
                            enabled = !state.purchaseInFlight,
                            onClick = {
                                val activity = context.findActivity()
                                if (activity != null) {
                                    viewModel.launchProPurchase(activity)
                                } else {
                                    viewModel.refreshMonetization()
                                }
                            }
                        ) {
                            Text(
                                if (state.purchaseInFlight) {
                                    "Processing..."
                                } else {
                                    "Unlock Pro"
                                }
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !state.purchaseInFlight,
                            onClick = {
                                val activity = context.findActivity()
                                if (activity != null) {
                                    viewModel.launchFullPurchase(activity)
                                } else {
                                    viewModel.refreshMonetization()
                                }
                            }
                        ) {
                            Text("Unlock Full")
                        }
                    },
                    title = {
                        Text(
                            text = "Upgrade to Pro",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        ProUpgradeDialogContent(
                            proPriceLabel = state.proPriceLabel,
                            isBillingReady = state.billingReady,
                            isPurchaseInFlight = state.purchaseInFlight,
                            message = state.monetizationMessage
                        )
                    }
                )
            }

            if (showFullUpgradeDialog && !state.hasFullUnlock) {
                AlertDialog(
                    onDismissRequest = { showFullUpgradeDialog = false },
                    confirmButton = {
                        TextButton(
                            enabled = !state.purchaseInFlight,
                            onClick = {
                                val activity = context.findActivity()
                                if (activity != null) {
                                    viewModel.launchFullPurchase(activity)
                                } else {
                                    viewModel.refreshMonetization()
                                }
                            }
                        ) {
                            Text(
                                if (state.purchaseInFlight) {
                                    "Processing..."
                                } else {
                                    "Unlock Full"
                                }
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.dismissMonetizationMessage()
                                showFullUpgradeDialog = false
                            }
                        ) {
                            Text("Not now")
                        }
                    },
                    title = {
                        Text(
                            text = "Upgrade to Full",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        FullUpgradeDialogContent(
                            fullPriceLabel = state.fullPriceLabel,
                            isBillingReady = state.billingReady,
                            isPurchaseInFlight = state.purchaseInFlight,
                            message = state.monetizationMessage
                        )
                    }
                )
            }

        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun CardRank.toHistoryLabel(): String {
    return when (this) {
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

private fun formatSignedPercent(value: Double): String {
    val rounded = round(value * 10.0) / 10.0
    val prefix = if (rounded > 0.0) "+" else ""
    return "$prefix${String.format(Locale.US, "%.1f", rounded)}%"
}

@Composable
private fun countColor(value: Double): Color {
    return when {
        value > 0 -> MaterialTheme.colorScheme.tertiary
        value < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}
