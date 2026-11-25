package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemProfiles
import com.vectorlogic.blackjackcounter.domain.persistence.PracticeStatsStore
import com.vectorlogic.blackjackcounter.domain.persistence.SessionPersister
import com.vectorlogic.blackjackcounter.presentation.asString
import com.vectorlogic.blackjackcounter.presentation.counter.components.CheatSheetCard
import com.vectorlogic.blackjackcounter.presentation.counter.components.HelpDialogContent
import com.vectorlogic.blackjackcounter.presentation.counter.components.StrategyChartFullScreen
import com.vectorlogic.blackjackcounter.presentation.counter.components.SystemSelector
import com.vectorlogic.blackjackcounter.presentation.counter.components.TableRulesDialogContent
import com.vectorlogic.blackjackcounter.presentation.drill.DrillDialog
import com.vectorlogic.blackjackcounter.presentation.quiz.DeviationQuizDialog
import com.vectorlogic.blackjackcounter.presentation.resolve
import com.vectorlogic.blackjackcounter.presentation.stats.PracticeStatsDialog
import com.vectorlogic.blackjackcounter.presentation.stats.PracticeStatsViewModel
import kotlinx.coroutines.launch

@Composable
fun CounterScreen(
    persister: SessionPersister? = null,
    statsStore: PracticeStatsStore? = null
) {
    val viewModel: CounterViewModel = viewModel(
        factory = remember(persister) { CounterViewModel.provideFactory(persister) }
    )
    val statsViewModel: PracticeStatsViewModel = viewModel(
        factory = remember(statsStore) { PracticeStatsViewModel.provideFactory(statsStore) }
    )
    val state by viewModel.uiState.collectAsState()
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var showSystemSelector by rememberSaveable { mutableStateOf(false) }
    var showRulesDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showStrategyChartDialog by rememberSaveable { mutableStateOf(false) }
    var showDrillDialog by rememberSaveable { mutableStateOf(false) }
    var showQuizDialog by rememberSaveable { mutableStateOf(false) }
    var showStatsDialog by rememberSaveable { mutableStateOf(false) }
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // A reset wipes the whole shoe, so ask first unless there is nothing to lose.
    val requestReset = {
        val hasProgress = state.seenCards.isNotEmpty() || state.decksRemaining < state.totalDecks
        if (hasProgress) showResetConfirm = true else viewModel.resetCount()
    }

    // The chart replaces the main content, so system back should close it, not the app.
    BackHandler(enabled = showStrategyChartDialog) { showStrategyChartDialog = false }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            // Counting a shoe takes longer than a typical screen timeout.
            .keepScreenOn(),
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
            onResetClick = requestReset,
            onToggleCheatSheet = viewModel::toggleCheatSheet,
            onTotalDecksChanged = viewModel::setTotalDecks,
            onDecksRemainingChanged = viewModel::setDecksRemaining,
            onCardSelected = viewModel::onCardSeen,
            onFaceCardsSelected = viewModel::onFaceCardsSeen,
            onUndo = viewModel::undoLastCard,
            onStrategyChartClick = { showStrategyChartDialog = true },
            onDrillClick = { showDrillDialog = true },
            onQuizClick = { showQuizDialog = true },
            onStatsClick = { showStatsDialog = true },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        )

        if (showDrillDialog) {
            val system = viewModel.activeCountingSystem()
            DrillDialog(
                system = system,
                onDrillFinished = { speed, result -> statsViewModel.recordDrill(system.id, speed, result) },
                onDismiss = { showDrillDialog = false }
            )
        }

        if (showQuizDialog) {
            // Built once per opening: the table rules can't change while the quiz is up.
            val quiz = remember { viewModel.buildDeviationQuiz() }
            DeviationQuizDialog(
                quiz = quiz,
                onRoundFinished = { score -> statsViewModel.recordQuiz(quiz.systemId, score) },
                onDismiss = { showQuizDialog = false }
            )
        }

        if (showStatsDialog) {
            val history by statsViewModel.history.collectAsState()
            PracticeStatsDialog(
                history = history,
                onClear = statsViewModel::clear,
                onDismiss = { showStatsDialog = false }
            )
        }

        if (showResetConfirm) {
            val cardsSeen = state.seenCards.size
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text(stringResource(R.string.reset_confirm_title)) },
                text = {
                    Text(
                        if (cardsSeen > 0) {
                            pluralStringResource(R.plurals.reset_confirm_message, cardsSeen, cardsSeen)
                        } else {
                            stringResource(R.string.reset_confirm_message_empty)
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetCount()
                        showResetConfirm = false
                    }) {
                        Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        if (showSystemSelector) {
            CounterAlertDialog(
                title = stringResource(R.string.counting_system_title),
                onDismiss = { showSystemSelector = false }
            ) {
                SystemSelector(
                    selectedSystemId = state.selectedCountingSystemId,
                    systems = viewModel.countingSystemGuides,
                    cardsSeen = state.seenCards.size,
                    onSystemSelected = { systemId ->
                        val before = viewModel.uiState.value
                        if (viewModel.setCountingSystem(systemId)) {
                            showSystemSelector = false
                            val after = viewModel.uiState.value
                            val message = systemSwitchMessage(
                                previousSystemId = before.selectedCountingSystemId,
                                previousRunningCount = before.runningCount,
                                newSystemId = after.selectedCountingSystemId,
                                newSystemName = after.selectedCountingSystemName,
                                newRunningCount = after.runningCount,
                                cardsSeen = after.seenCards.size
                            )
                            if (message != null) {
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(message.resolve(resources), withDismissAction = true)
                                }
                            }
                        }
                    }
                )
            }
        }

        if (showHelpDialog) {
            CounterAlertDialog(
                title = stringResource(R.string.help_title),
                onDismiss = { showHelpDialog = false }
            ) {
                HelpDialogContent(
                    countingSystemGuides = viewModel.countingSystemGuides,
                    tableRules = state.tableRules
                )
            }
        }

        if (showRulesDialog) {
            CounterAlertDialog(
                title = stringResource(R.string.table_rules_title),
                confirmLabel = stringResource(R.string.done),
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
                        Text(stringResource(R.string.close))
                    }
                },
                text = {
                    CheatSheetCard(
                        systemName = state.selectedCountingSystemName,
                        rows = state.cheatSheetRows,
                        strategyRows = state.strategyDeviationRows,
                        usesTrueCount = state.selectedSystemUsesTrueCount,
                        trueCountScale = CountingSystemProfiles
                            .profileFor(state.selectedCountingSystemId)
                            .trueCountScale,
                        // The bet-sizing count is the same count the strategy chart decides
                        // deviations with (TC, or RC for KO/ReKO).
                        decisionCountLabel = state.betSizingCountLabel.asString(),
                        decisionCount = state.betSizingCountValue
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
    confirmLabel: String? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(confirmLabel ?: stringResource(R.string.close)) }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = content
    )
}
