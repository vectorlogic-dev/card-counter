package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorlogic.blackjackcounter.domain.persistence.SessionPersister
import com.vectorlogic.blackjackcounter.presentation.counter.components.CheatSheetCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.HelpDialogContent
import com.vectorlogic.blackjackcounter.presentation.counter.components.StrategyChartFullScreen
import com.vectorlogic.blackjackcounter.presentation.counter.components.SystemSelector
import com.vectorlogic.blackjackcounter.presentation.counter.components.TableRulesDialogContent
import com.vectorlogic.blackjackcounter.presentation.drill.DrillDialog

@Composable
fun CounterScreen(
    persister: SessionPersister? = null
) {
    val viewModel: CounterViewModel = viewModel(
        factory = remember(persister) { CounterViewModel.provideFactory(persister) }
    )
    val state by viewModel.uiState.collectAsState()
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var showSystemSelector by rememberSaveable { mutableStateOf(false) }
    var showRulesDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showStrategyChartDialog by rememberSaveable { mutableStateOf(false) }
    var showDrillDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showStrategyChartDialog) {
            StrategyChartFullScreen(
                model = viewModel.buildStrategyChartModel(),
                onBack = { showStrategyChartDialog = false },
                modifier = Modifier.fillMaxSize()
            )
            return@Surface
        }

        CounterScreenContent(
            state = state,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = { menuExpanded = it },
            onHelpClick = { showHelpDialog = true },
            onSelectCountingSystem = { showSystemSelector = true },
            onTableRulesClick = { showRulesDialog = true },
            onResetClick = viewModel::resetCount,
            onToggleCheatSheet = viewModel::toggleCheatSheet,
            onTotalDecksChanged = viewModel::setTotalDecks,
            onDecksRemainingChanged = viewModel::setDecksRemaining,
            onCardSelected = viewModel::onCardSeen,
            onFaceCardsSelected = viewModel::onFaceCardsSeen,
            onUndo = viewModel::undoLastCard,
            onStrategyChartClick = { showStrategyChartDialog = true },
            onDrillClick = { showDrillDialog = true }
        )

        if (showDrillDialog) {
            DrillDialog(
                system = viewModel.activeCountingSystem(),
                onDismiss = { showDrillDialog = false }
            )
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
                        if (viewModel.setCountingSystem(systemId)) {
                            showSystemSelector = false
                        }
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
