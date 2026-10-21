package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemProfiles
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CounterCalculations
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.domain.persistence.SessionPersister
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

private data class RunningCountTotals(
    val runningCount: Double,
    val acesSeen: Int
)

class CounterViewModel private constructor(
    private val persister: SessionPersister? = null
) : ViewModel() {

    private val allSystems: List<CountingSystem> = CountingSystemsCatalog.allSystems
    private val systemsById: Map<String, CountingSystem> = CountingSystemsCatalog.systemsById
    private val defaultSystem: CountingSystem = CountingSystemsCatalog.defaultSystem
    private val profilesBySystemId = allSystems.associate { system ->
        system.id to CountingSystemProfiles.profileFor(system.id)
    }

    private val _sessionState = MutableStateFlow(persister?.load() ?: initialSessionState())
    private val _showCheatSheet = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(buildUiState(_sessionState.value, _showCheatSheet.value))
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    val availableSystems: List<CountingSystemOption> = allSystems.map { system ->
        CountingSystemOption(
            id = system.id,
            displayName = system.displayName,
            level = system.level,
            recommendsAceSideCount = system.recommendsAceSideCount()
        )
    }

    val countingSystemGuides: List<CountingSystemGuide> =
        CounterHelpContentFactory.buildCountingSystemGuides(allSystems)

    val basicStrategySections: List<StrategyChartSection> =
        CounterHelpContentFactory.basicStrategySections

    fun getSplitDeviationSections(tableRules: TableRules): List<StrategyChartSection> {
        return CounterHelpContentFactory.buildSplitDeviationSections(tableRules)
    }

    fun activeCountingSystem(): CountingSystem {
        return getSelectedSystem(_sessionState.value.selectedCountingSystemId)
    }

    fun buildStrategyChartModel(): StrategyChartModel {
        val state = _sessionState.value
        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val trueCount = CounterCalculations.decisionTrueCount(
            calculateRawTrueCount(state, selectedSystem.usesTrueCount())
        )
        return CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            trueCount = trueCount,
            supportsTrueCount = selectedSystem.usesTrueCount(),
            // The chart decides on the same count its header shows.
            decisionTrueCount = trueCount,
            trueCountScale = profileFor(selectedSystem.id).trueCountScale
        )
    }

    fun onCardSeen(cardRank: CardRank) {
        val selectedSystem = getSelectedSystem(_sessionState.value.selectedCountingSystemId)
        val delta = selectedSystem.getTagValue(cardRank)
        val countedAce = cardRank == CardRank.ACE && selectedSystem.recommendsAceSideCount()
        updateRunningCount(
            delta = delta,
            advancePenetration = true,
            seenAce = countedAce,
            seenCardRank = cardRank
        )
    }

    fun onFaceCardsSeen() {
        // In current systems, J/Q/K share the same tag value, so one grouped button is safe.
        onCardSeen(CardRank.TEN)
    }

    fun resetCount() {
        updateSessionState { current ->
            val selectedSystem = getSelectedSystem(current.selectedCountingSystemId)
            current.copy(
                runningCount = initialRunningCount(
                    systemId = selectedSystem.id,
                    totalDecks = current.totalDecks
                ),
                decksRemaining = current.totalDecks.toDouble(),
                acesSeen = 0,
                seenCards = emptyList()
            )
        }
    }

    fun setTotalDecks(totalDecks: Int) {
        val normalizedTotalDecks = totalDecks.coerceIn(MIN_TOTAL_DECKS, MAX_TOTAL_DECKS)
        updateSessionState { current ->
            val totalCardsForNewShoe = normalizedTotalDecks * CARDS_PER_DECK
            val preservedCardsDrawn = cardsDrawn(current).coerceIn(0, totalCardsForNewShoe)
            val normalizedDecksRemaining = ((totalCardsForNewShoe - preservedCardsDrawn).toDouble() / CARDS_PER_DECK)
                .coerceIn(MIN_DECKS_REMAINING, normalizedTotalDecks.toDouble())

            recalculateSystemState(
                current.copy(
                    totalDecks = normalizedTotalDecks,
                    decksRemaining = normalizedDecksRemaining
                )
            )
        }
    }

    fun setDecksRemaining(decksRemaining: Double) {
        updateSessionState { current ->
            current.copy(
                decksRemaining = decksRemaining.coerceIn(
                    MIN_DECKS_REMAINING,
                    current.totalDecks.toDouble()
                )
            )
        }
    }

    fun setCountingSystem(systemId: String): Boolean {
        val selectedSystem = systemsById[systemId] ?: return false

        updateSessionState { current ->
            val shouldTrackAces = selectedSystem.recommendsAceSideCount()
            val remappedSeenCards = current.seenCards.map { seen ->
                seen.copy(
                    deltaApplied = selectedSystem.getTagValue(seen.rank),
                    countedAce = shouldTrackAces && seen.rank == CardRank.ACE
                )
            }
            val countTotals = replayCountForSystem(
                system = selectedSystem,
                totalDecks = current.totalDecks,
                seenCards = remappedSeenCards
            )
            current.copy(
                selectedCountingSystemId = selectedSystem.id,
                runningCount = countTotals.runningCount,
                acesSeen = countTotals.acesSeen,
                seenCards = remappedSeenCards
            )
        }

        return true
    }

    fun undoLastCard() {
        updateSessionState { current ->
            val lastSeen = current.seenCards.lastOrNull() ?: return@updateSessionState current
            current.copy(
                runningCount = current.runningCount - lastSeen.deltaApplied,
                decksRemaining = (current.decksRemaining + lastSeen.decksAdvanced)
                    .coerceAtMost(current.totalDecks.toDouble()),
                acesSeen = if (lastSeen.countedAce) {
                    (current.acesSeen - 1).coerceAtLeast(0)
                } else {
                    current.acesSeen
                },
                seenCards = current.seenCards.dropLast(1)
            )
        }
    }

    fun setDealerSoft17Rule(rule: DealerSoft17Rule) {
        updateTableRules { it.copy(dealerSoft17Rule = rule) }
    }

    fun setDealerPeekRule(rule: DealerPeekRule) {
        updateTableRules { it.copy(dealerPeekRule = rule) }
    }

    fun setBlackjackPayoutRule(rule: BlackjackPayoutRule) {
        updateTableRules { it.copy(blackjackPayoutRule = rule) }
    }

    fun setDoubleDownRule(rule: DoubleDownRule) {
        updateTableRules { it.copy(doubleDownRule = rule) }
    }

    fun setDoubleAfterSplit(enabled: Boolean) {
        updateTableRules { it.copy(doubleAfterSplit = enabled) }
    }

    fun setSurrenderRule(rule: SurrenderRule) {
        updateTableRules { it.copy(surrenderRule = rule) }
    }

    fun setResplitAces(enabled: Boolean) {
        updateTableRules { it.copy(resplitAces = enabled) }
    }

    fun setHitSplitAces(enabled: Boolean) {
        updateTableRules { it.copy(hitSplitAces = enabled) }
    }

    fun setSixCardCharlieAutoWin(enabled: Boolean) {
        updateTableRules { it.copy(sixCardCharlieAutoWin = enabled) }
    }

    fun setMaxSplitHands(maxHands: Int) {
        val safeMaxHands = maxHands.coerceIn(2, 6)
        updateTableRules { it.copy(maxSplitHands = safeMaxHands) }
    }

    fun toggleCheatSheet() {
        setCheatSheetVisible(!_showCheatSheet.value)
    }

    fun setCheatSheetVisible(visible: Boolean) {
        _showCheatSheet.value = visible
        syncUiState()
    }

    private fun updateRunningCount(
        delta: Double,
        advancePenetration: Boolean = false,
        seenAce: Boolean = false,
        seenCardRank: CardRank? = null
    ) {
        updateSessionState { current ->
            val nextDecksRemaining = if (advancePenetration) {
                (current.decksRemaining - DECKS_PER_CARD).coerceAtLeast(MIN_DECKS_REMAINING)
            } else {
                current.decksRemaining
            }

            current.copy(
                runningCount = current.runningCount + delta,
                decksRemaining = nextDecksRemaining,
                acesSeen = if (seenAce) current.acesSeen + 1 else current.acesSeen,
                seenCards = if (seenCardRank != null && advancePenetration) {
                    current.seenCards + SeenCard(
                        rank = seenCardRank,
                        deltaApplied = delta,
                        countedAce = seenAce,
                        // Less than one card's worth when the decks-remaining floor clamps it.
                        decksAdvanced = (current.decksRemaining - nextDecksRemaining).coerceAtLeast(0.0)
                    )
                } else {
                    current.seenCards
                }
            )
        }
    }

    private fun initialSessionState(): CounterSessionState {
        return CounterSessionState(
            selectedCountingSystemId = defaultSystem.id,
            runningCount = initialRunningCount(
                systemId = defaultSystem.id,
                totalDecks = DEFAULT_TOTAL_DECKS
            )
        )
    }

    private fun updateSessionState(transform: (CounterSessionState) -> CounterSessionState) {
        val next = transform(_sessionState.value)
        _sessionState.value = next
        // Persistence must never crash the Activity on a routine tap: a storage
        // failure (encrypted-user state, disk I/O, etc.) is non-fatal here — the
        // in-memory state is still correct, we just lose the resume-on-restart.
        runCatching { persister?.save(next) }
        syncUiState()
    }

    private fun syncUiState() {
        _uiState.value = buildUiState(_sessionState.value, _showCheatSheet.value)
    }

    private fun buildUiState(state: CounterSessionState, showCheatSheet: Boolean): CounterUiState {
        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val usesTrueCount = selectedSystem.usesTrueCount()
        val profile = profileFor(selectedSystem.id)
        val totalCards = state.totalDecks * CARDS_PER_DECK
        val cardsLeft = (state.decksRemaining * CARDS_PER_DECK).roundToInt().coerceIn(0, totalCards)
        val cardsDrawn = (totalCards - cardsLeft).coerceAtLeast(0)
        val shouldTrackAces = selectedSystem.recommendsAceSideCount()
        val rawTrueCount = calculateRawTrueCount(state, usesTrueCount)
        // One count is shown, bet on, deviated on and used for the edge, so they always agree.
        val trueCount = CounterCalculations.decisionTrueCount(rawTrueCount)
        val estimatedPlayerEdgePercent = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = trueCount,
            usesTrueCount = usesTrueCount,
            tableRules = state.tableRules,
            totalDecks = state.totalDecks,
            edgeGainPercentPerTrueCount = selectedSystem.edgeGainPercentPerTrueCount,
            trueCountScale = profile.trueCountScale
        )
        val betSizingCountValue = if (usesTrueCount) trueCount else state.runningCount
        val betRamp = if (usesTrueCount) {
            profile.balancedBetRamp
        } else {
            profile.runningCountBetRamp(state.totalDecks)
        }
        val recommendedBetUnits = CounterCalculations.calculateRecommendedBetUnits(
            countValue = betSizingCountValue,
            ramp = betRamp
        )
        val acesSeen = if (shouldTrackAces) state.acesSeen else 0
        val buttonTagValues = CardRank.entries.associateWith { rank ->
            selectedSystem.getTagValue(rank)
        }

        return CounterUiState(
            runningCount = state.runningCount,
            trueCount = trueCount,
            recommendedBetUnits = recommendedBetUnits,
            betSizingCountLabel = if (usesTrueCount) "TC" else "RC",
            betSizingCountValue = betSizingCountValue,
            deckPenetrationPercent = calculateDeckPenetrationPercent(state),
            totalDecks = state.totalDecks,
            decksRemaining = state.decksRemaining,
            totalCards = totalCards,
            cardsDrawn = cardsDrawn,
            cardsLeft = cardsLeft,
            acesSeen = acesSeen,
            acesExpected = if (shouldTrackAces) {
                CounterCalculations.calculateExpectedAces(cardsDrawn)
            } else {
                0.0
            },
            estimatedPlayerEdgePercent = estimatedPlayerEdgePercent,
            canEstimateEdge = usesTrueCount,
            selectedCountingSystemId = selectedSystem.id,
            selectedCountingSystemName = selectedSystem.displayName,
            selectedSystemUsesTrueCount = usesTrueCount,
            selectedSystemRecommendsAceSideCount = shouldTrackAces,
            seenCards = state.seenCards,
            tableRules = state.tableRules,
            buttonTagValues = buttonTagValues,
            cheatSheetRows = CounterHelpContentFactory.buildCheatSheetRows(selectedSystem),
            strategyDeviationRows = CounterStrategyEngine.buildStrategyDeviationRows(
                state = state,
                usesTrueCount = usesTrueCount,
                keyCount = profile.keyCount(state.totalDecks),
                pivot = profile.pivot(state.totalDecks),
                trueCountScale = profile.trueCountScale
            ),
            showCheatSheet = showCheatSheet
        )
    }

    private fun cardsDrawn(state: CounterSessionState): Int {
        val totalCards = state.totalDecks * CARDS_PER_DECK
        val cardsLeft = (state.decksRemaining * CARDS_PER_DECK).roundToInt().coerceIn(0, totalCards)
        return (totalCards - cardsLeft).coerceAtLeast(0)
    }

    private fun getSelectedSystem(systemId: String): CountingSystem {
        return systemsById[systemId] ?: defaultSystem
    }

    private fun calculateRawTrueCount(
        state: CounterSessionState,
        usesTrueCount: Boolean
    ): Double {
        return CounterCalculations.calculateRawTrueCount(
            runningCount = state.runningCount,
            decksRemaining = state.decksRemaining,
            usesTrueCount = usesTrueCount,
            minDecksRemaining = MIN_DECKS_REMAINING
        )
    }

    private fun profileFor(systemId: String) =
        profilesBySystemId[systemId] ?: CountingSystemProfiles.profileFor(systemId)

    private fun initialRunningCount(systemId: String, totalDecks: Int): Double {
        return profileFor(systemId).initialRunningCount(totalDecks).toDouble()
    }

    private fun recalculateSystemState(state: CounterSessionState): CounterSessionState {
        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val shouldTrackAces = selectedSystem.recommendsAceSideCount()
        val remappedSeenCards = state.seenCards.map { seen ->
            seen.copy(
                deltaApplied = selectedSystem.getTagValue(seen.rank),
                countedAce = shouldTrackAces && seen.rank == CardRank.ACE
            )
        }
        val totals = replayCountForSystem(
            system = selectedSystem,
            totalDecks = state.totalDecks,
            seenCards = remappedSeenCards
        )
        return state.copy(
            runningCount = totals.runningCount,
            acesSeen = totals.acesSeen,
            seenCards = remappedSeenCards
        )
    }

    private fun replayCountForSystem(
        system: CountingSystem,
        totalDecks: Int,
        seenCards: List<SeenCard>
    ): RunningCountTotals {
        val shouldTrackAces = system.recommendsAceSideCount()
        val runningCount = seenCards.fold(
            initialRunningCount(system.id, totalDecks)
        ) { total, seen ->
            total + system.getTagValue(seen.rank)
        }
        val acesSeen = if (shouldTrackAces) {
            seenCards.count { it.rank == CardRank.ACE }
        } else {
            0
        }
        return RunningCountTotals(runningCount = runningCount, acesSeen = acesSeen)
    }

    private fun calculateDeckPenetrationPercent(state: CounterSessionState): Double {
        return CounterCalculations.calculateDeckPenetrationPercent(
            totalDecks = state.totalDecks,
            decksRemaining = state.decksRemaining
        )
    }

    private fun updateTableRules(update: (TableRules) -> TableRules) {
        updateSessionState { current ->
            current.copy(tableRules = update(current.tableRules))
        }
    }

    companion object {
        fun provideFactory(persister: SessionPersister? = null): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CounterViewModel(persister) as T
                }
            }
        }

        private const val DEFAULT_TOTAL_DECKS = 6
        private const val MIN_TOTAL_DECKS = 1
        private const val MAX_TOTAL_DECKS = 8
        private const val MIN_DECKS_REMAINING = 0.25
        private const val DECKS_PER_CARD = 1.0 / 52.0
        private const val CARDS_PER_DECK = 52
    }
}
