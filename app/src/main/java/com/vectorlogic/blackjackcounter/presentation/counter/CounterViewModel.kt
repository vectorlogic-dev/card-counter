package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemProfiles
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CounterCalculations
import com.vectorlogic.blackjackcounter.domain.model.CounterState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.domain.model.TrueCountIntegerization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

private data class RunningCountTotals(
    val runningCount: Double,
    val acesSeen: Int
)

class CounterViewModel private constructor() : ViewModel() {

    private val allSystems: List<CountingSystem> = CountingSystemsCatalog.allSystems
    private val systemsById: Map<String, CountingSystem> = CountingSystemsCatalog.systemsById
    private val defaultSystem: CountingSystem = CountingSystemsCatalog.defaultSystem
    private val profilesBySystemId = allSystems.associate { system ->
        system.id to CountingSystemProfiles.profileFor(system.id)
    }
    private val _uiState = MutableStateFlow(withDerivedValues(
        CounterState(
            selectedCountingSystemId = defaultSystem.id,
            selectedCountingSystemName = defaultSystem.displayName,
            selectedSystemUsesTrueCount = defaultSystem.usesTrueCount(),
            selectedSystemRecommendsAceSideCount = defaultSystem.recommendsAceSideCount()
        )
    ))
    val uiState: StateFlow<CounterState> = _uiState.asStateFlow()

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

    fun buildStrategyChartModel(state: CounterState): StrategyChartModel {
        return CounterStrategyEngine.buildStrategyChartModel(
            state = state,
            decisionTrueCount = decisionTrueCount(state)
        )
    }

    fun onCardSeen(cardRank: CardRank) {
        val selectedSystem = getSelectedSystem(_uiState.value.selectedCountingSystemId)
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
        _uiState.update { current ->
            val selectedSystem = getSelectedSystem(current.selectedCountingSystemId)
            withDerivedValues(
                current.copy(
                    runningCount = initialRunningCount(
                        systemId = selectedSystem.id,
                        totalDecks = current.totalDecks
                    ),
                    decksRemaining = current.totalDecks.toDouble(),
                    acesSeen = 0,
                    seenCards = emptyList()
                )
            )
        }
    }

    fun setTotalDecks(totalDecks: Int) {
        val normalizedTotalDecks = totalDecks.coerceIn(MIN_TOTAL_DECKS, MAX_TOTAL_DECKS)
        _uiState.update { current ->
            val normalizedDecksRemaining = current.decksRemaining.coerceIn(
                MIN_DECKS_REMAINING,
                normalizedTotalDecks.toDouble()
            )

            val nextState = current.copy(
                totalDecks = normalizedTotalDecks,
                decksRemaining = normalizedDecksRemaining
            )
            withDerivedValues(
                recalculateSystemState(nextState)
            )
        }
    }

    fun setDecksRemaining(decksRemaining: Double) {
        _uiState.update { current ->
            val normalizedDecksRemaining = decksRemaining.coerceIn(
                MIN_DECKS_REMAINING,
                current.totalDecks.toDouble()
            )
            val nextState = current.copy(decksRemaining = normalizedDecksRemaining)
            withDerivedValues(nextState)
        }
    }

    fun setCountingSystem(systemId: String): Boolean {
        val selectedSystem = systemsById[systemId] ?: return false

        _uiState.update { current ->
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
            val nextState = current.copy(
                selectedCountingSystemId = selectedSystem.id,
                selectedCountingSystemName = selectedSystem.displayName,
                selectedSystemUsesTrueCount = selectedSystem.usesTrueCount(),
                selectedSystemRecommendsAceSideCount = shouldTrackAces,
                runningCount = countTotals.runningCount,
                acesSeen = countTotals.acesSeen,
                seenCards = remappedSeenCards
            )
            withDerivedValues(nextState)
        }

        return true
    }

    fun undoLastCard() {
        _uiState.update { current ->
            val lastSeen = current.seenCards.lastOrNull() ?: return@update current
            val nextState = current.copy(
                runningCount = current.runningCount - lastSeen.deltaApplied,
                decksRemaining = (current.decksRemaining + DECKS_PER_CARD)
                    .coerceAtMost(current.totalDecks.toDouble()),
                acesSeen = if (lastSeen.countedAce) {
                    (current.acesSeen - 1).coerceAtLeast(0)
                } else {
                    current.acesSeen
                },
                seenCards = current.seenCards.dropLast(1)
            )
            withDerivedValues(nextState)
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
        setCheatSheetVisible(!_uiState.value.showCheatSheet)
    }

    fun setCheatSheetVisible(visible: Boolean) {
        _uiState.update { current ->
            current.copy(showCheatSheet = visible)
        }
    }

    private fun updateRunningCount(
        delta: Double,
        advancePenetration: Boolean = false,
        seenAce: Boolean = false,
        seenCardRank: CardRank? = null
    ) {
        _uiState.update { current ->
            val nextDecksRemaining = if (advancePenetration) {
                (current.decksRemaining - DECKS_PER_CARD).coerceAtLeast(MIN_DECKS_REMAINING)
            } else {
                current.decksRemaining
            }

            val nextState = current.copy(
                runningCount = current.runningCount + delta,
                decksRemaining = nextDecksRemaining,
                acesSeen = if (seenAce) current.acesSeen + 1 else current.acesSeen,
                seenCards = if (seenCardRank != null && advancePenetration) {
                    current.seenCards + SeenCard(
                        rank = seenCardRank,
                        deltaApplied = delta,
                        countedAce = seenAce
                    )
                } else {
                    current.seenCards
                }
            )
            withDerivedValues(nextState)
        }
    }

    private fun getSelectedSystem(systemId: String): CountingSystem {
        return systemsById[systemId] ?: defaultSystem
    }

    private fun calculateRawTrueCount(
        state: CounterState,
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

    private fun recalculateSystemState(state: CounterState): CounterState {
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

    private fun decisionCountValue(
        state: CounterState,
        selectedSystem: CountingSystem,
        rawTrueCount: Double
    ): Double {
        val profile = profileFor(selectedSystem.id)
        return if (selectedSystem.usesTrueCount()) {
            val method = profile.trueCountIntegerization ?: TrueCountIntegerization.TRUNCATE
            CounterCalculations.integerizeTrueCount(rawTrueCount, method).toDouble()
        } else {
            state.runningCount
        }
    }

    private fun calculateDeckPenetrationPercent(state: CounterState): Double {
        return CounterCalculations.calculateDeckPenetrationPercent(
            totalDecks = state.totalDecks,
            decksRemaining = state.decksRemaining
        )
    }

    private fun decisionTrueCount(state: CounterState): Int {
        if (!state.selectedSystemUsesTrueCount) return 0
        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val rawTrueCount = calculateRawTrueCount(state, usesTrueCount = true)
        return decisionCountValue(
            state = state,
            selectedSystem = selectedSystem,
            rawTrueCount = rawTrueCount
        ).toInt()
    }

    private fun withDerivedValues(state: CounterState): CounterState {
        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val usesTrueCount = selectedSystem.usesTrueCount()
        val profile = profileFor(selectedSystem.id)
        val totalCards = state.totalDecks * CARDS_PER_DECK
        val cardsLeft = (state.decksRemaining * CARDS_PER_DECK).roundToInt().coerceIn(0, totalCards)
        val cardsDrawn = (totalCards - cardsLeft).coerceAtLeast(0)
        val shouldTrackAces = selectedSystem.recommendsAceSideCount()
        val rawTrueCount = calculateRawTrueCount(state, usesTrueCount)
        val trueCount = CounterCalculations.roundToOneDecimal(rawTrueCount)
        val estimatedPlayerEdgePercent = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = rawTrueCount,
            usesTrueCount = usesTrueCount,
            tableRules = state.tableRules,
            totalDecks = state.totalDecks
        )
        val betSizingCountValue = decisionCountValue(
            state = state,
            selectedSystem = selectedSystem,
            rawTrueCount = rawTrueCount
        )
        val betRamp = if (usesTrueCount) {
            profile.balancedBetRamp
        } else {
            profile.runningCountBetRamp(state.totalDecks)
        }
        val recommendedBetUnits = CounterCalculations.calculateRecommendedBetUnits(
            countValue = betSizingCountValue,
            ramp = betRamp
        )
        val acesExpected = if (shouldTrackAces) {
            CounterCalculations.calculateExpectedAces(cardsDrawn)
        } else {
            0.0
        }
        val acesSeen = if (shouldTrackAces) state.acesSeen else 0
        val buttonTagValues = CardRank.entries.associateWith { rank ->
            selectedSystem.getTagValue(rank)
        }

        return state.copy(
            trueCount = trueCount,
            recommendedBetUnits = recommendedBetUnits,
            betSizingCountLabel = if (usesTrueCount) "TC" else "RC",
            betSizingCountValue = betSizingCountValue,
            deckPenetrationPercent = calculateDeckPenetrationPercent(state),
            totalCards = totalCards,
            cardsLeft = cardsLeft,
            cardsDrawn = cardsDrawn,
            acesSeen = acesSeen,
            acesExpected = acesExpected,
            estimatedPlayerEdgePercent = estimatedPlayerEdgePercent,
            canEstimateEdge = usesTrueCount,
            selectedSystemUsesTrueCount = usesTrueCount,
            selectedSystemRecommendsAceSideCount = shouldTrackAces,
            buttonTagValues = buttonTagValues,
            cheatSheetRows = CounterHelpContentFactory.buildCheatSheetRows(selectedSystem),
            strategyDeviationRows = CounterStrategyEngine.buildStrategyDeviationRows(
                state = state,
                usesTrueCount = usesTrueCount,
                keyCount = profile.keyCount(state.totalDecks),
                pivot = profile.pivot(state.totalDecks)
            )
        )
    }

    private fun updateTableRules(update: (TableRules) -> TableRules) {
        _uiState.update { current ->
            withDerivedValues(
                current.copy(
                    tableRules = update(current.tableRules)
                )
            )
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CounterViewModel() as T
                }
            }
        }

        private const val MIN_TOTAL_DECKS = 1
        private const val MAX_TOTAL_DECKS = 8
        private const val MIN_DECKS_REMAINING = 0.25
        private const val DECKS_PER_CARD = 1.0 / 52.0
        private const val CARDS_PER_DECK = 52
    }
}

