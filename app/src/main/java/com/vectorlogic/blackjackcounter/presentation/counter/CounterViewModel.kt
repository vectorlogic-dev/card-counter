package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemProfiles
import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiLoCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.HiOptIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.KOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.OmegaIICountingSystem
import com.vectorlogic.blackjackcounter.data.counting.ReKOCountingSystem
import com.vectorlogic.blackjackcounter.data.counting.WongHalvesSystem
import com.vectorlogic.blackjackcounter.data.counting.ZenCountSystem
import com.vectorlogic.blackjackcounter.data.monetization.CountingSystemMonetization
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CheatSheetRow
import com.vectorlogic.blackjackcounter.domain.model.CounterCalculations
import com.vectorlogic.blackjackcounter.domain.model.CounterState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.StrategyDeviationRow
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.domain.model.TrueCountIntegerization
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

data class CountingSystemOption(
    val id: String,
    val displayName: String,
    val level: Int,
    val recommendsAceSideCount: Boolean,
    val isFree: Boolean,
    val defaultPriceLabel: String?
)

private data class RunningCountTotals(
    val runningCount: Double,
    val acesSeen: Int
)

data class CountingSystemGuide(
    val id: String,
    val displayName: String,
    val level: Int,
    val isBalanced: Boolean,
    val usesTrueCount: Boolean,
    val recommendsAceSideCount: Boolean,
    val explanation: String,
    val tagRows: List<CheatSheetRow>
)

data class StrategyChartSection(
    val title: String,
    val rows: List<StrategyChartRow>
)

data class StrategyChartRow(
    val hand: String,
    val action: String
)

data class StrategyTableCell(
    val action: String,
    val isDeviation: Boolean
)

data class StrategyTableRow(
    val handLabel: String,
    val cells: List<StrategyTableCell>
)

data class StrategyTableSection(
    val title: String,
    val rows: List<StrategyTableRow>
)

data class StrategyChartModel(
    val trueCountLabel: String,
    val supportsTrueCount: Boolean,
    val sections: List<StrategyTableSection>
)

class CounterViewModel private constructor() : ViewModel() {

    private val allSystems: List<CountingSystem> = CountingSystemsCatalog.allSystems
    private val systemsById: Map<String, CountingSystem> = CountingSystemsCatalog.systemsById
    private val defaultSystem: CountingSystem = CountingSystemsCatalog.defaultSystem
    private val freeSystemIds: Set<String> = allSystems
        .filter { system -> CountingSystemMonetization.isFreeLevel(system.level) }
        .map { system -> system.id }
        .toSet()
    private var purchasedSystemIds: Set<String> = emptySet()
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
            recommendsAceSideCount = system.recommendsAceSideCount(),
            isFree = CountingSystemMonetization.isFreeLevel(system.level),
            defaultPriceLabel = CountingSystemMonetization.fallbackPriceForLevel(system.level)
        )
    }

    val countingSystemGuides: List<CountingSystemGuide> = allSystems.map { system ->
        CountingSystemGuide(
            id = system.id,
            displayName = system.displayName,
            level = system.level,
            isBalanced = system.isBalanced,
            usesTrueCount = system.usesTrueCount(),
            recommendsAceSideCount = system.recommendsAceSideCount(),
            explanation = getSystemExplanation(system),
            tagRows = calculateCheatSheetRows(system)
        )
    }

    val basicStrategySections: List<StrategyChartSection> = listOf(
        StrategyChartSection(
            title = "Hard Totals",
            rows = listOf(
                StrategyChartRow("Hard 8 or less", "Hit"),
                StrategyChartRow("Hard 9", "Double vs 3-6, otherwise Hit"),
                StrategyChartRow("Hard 10", "Double vs 2-9, otherwise Hit"),
                StrategyChartRow("Hard 11", "Double (otherwise Hit)"),
                StrategyChartRow("Hard 12", "Stand vs 4-6, otherwise Hit"),
                StrategyChartRow("Hard 13-16", "Stand vs 2-6, otherwise Hit"),
                StrategyChartRow("Hard 17+", "Stand")
            )
        ),
        StrategyChartSection(
            title = "Soft Totals",
            rows = listOf(
                StrategyChartRow("A,2 or A,3", "Double vs 5-6, otherwise Hit"),
                StrategyChartRow("A,4 or A,5", "Double vs 4-6, otherwise Hit"),
                StrategyChartRow("A,6", "Double vs 3-6, otherwise Hit"),
                StrategyChartRow("A,7", "Stand vs 2,7,8; Double vs 3-6; Hit vs 9,10,A"),
                StrategyChartRow("A,8+", "Stand")
            )
        ),
        StrategyChartSection(
            title = "Pairs",
            rows = listOf(
                StrategyChartRow("A,A", "Split"),
                StrategyChartRow("10,10", "Stand"),
                StrategyChartRow("9,9", "Split vs 2-6,8,9; otherwise Stand"),
                StrategyChartRow("8,8", "Split"),
                StrategyChartRow("7,7", "Split vs 2-7, otherwise Hit"),
                StrategyChartRow("6,6", "Split vs 2-6, otherwise Hit"),
                StrategyChartRow("5,5", "Double vs 2-9, otherwise Hit"),
                StrategyChartRow("4,4", "Split vs 5-6, otherwise Hit"),
                StrategyChartRow("3,3 or 2,2", "Split vs 2-7, otherwise Hit")
            )
        )
    )

    fun getSplitDeviationSections(tableRules: TableRules): List<StrategyChartSection> {
        val noPeekOnTen = tableRules.dealerPeekRule == DealerPeekRule.ACE_ONLY ||
            tableRules.dealerPeekRule == DealerPeekRule.NO_PEEK
        val noPeekOnAce = tableRules.dealerPeekRule == DealerPeekRule.NO_PEEK

        val baseRows = mutableListOf(
            StrategyChartRow("10,10 vs 5", "Split at very high TC (commonly around +5)"),
            StrategyChartRow("10,10 vs 6", "Split at very high TC (commonly around +4)"),
            StrategyChartRow(
                "10 vs 10 (double deviation)",
                if (noPeekOnTen) {
                    "Avoid if no-peek on 10 is active."
                } else {
                    "Double only at high TC (commonly around +4)."
                }
            ),
            StrategyChartRow("9,9 vs 7", "Can shift by count/rules; use your index chart"),
            StrategyChartRow("All split deviations", "Rule and system dependent; verify table rules")
        )

        if (!tableRules.resplitAces) {
            baseRows.add(
                StrategyChartRow("A,A re-split", "Not allowed at this table.")
            )
        }
        if (!tableRules.hitSplitAces) {
            baseRows.add(
                StrategyChartRow("After splitting A,A", "No hit after split aces.")
            )
        }
        if (noPeekOnTen) {
            baseRows.add(
                StrategyChartRow("A,A vs 10", "Use no-peek-on-10 chart adjustments.")
            )
        }
        if (noPeekOnAce) {
            baseRows.add(
                StrategyChartRow("A,A vs A", "Use no-peek-on-A chart adjustments.")
            )
        }

        return listOf(
            StrategyChartSection(
                title = "Count-Based Split Deviations (Advanced, Hi-Lo style)",
                rows = baseRows
            )
        )
    }

    fun buildStrategyChartModel(state: CounterState): StrategyChartModel {
        val hardRows = listOf(17, 16, 15, 14, 13, 12, 11, 10, 9, 8).map { total ->
            StrategyTableRow(
                handLabel = if (total == 17) "17+" else total.toString(),
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.HARD,
                        handValue = total,
                        dealerUpcard = dealer,
                        state = state
                    )
                }
            )
        }

        val softRows = listOf(9, 8, 7, 6, 5, 4, 3, 2).map { soft ->
            StrategyTableRow(
                handLabel = "A,$soft",
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.SOFT,
                        handValue = soft,
                        dealerUpcard = dealer,
                        state = state
                    )
                }
            )
        }

        val pairRows = listOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2).map { pairRank ->
            val label = if (pairRank == 11) "A,A" else "$pairRank,$pairRank"
            StrategyTableRow(
                handLabel = label,
                cells = dealerUpcards.map { dealer ->
                    computeChartCell(
                        handType = ChartHandType.PAIR,
                        handValue = pairRank,
                        dealerUpcard = dealer,
                        state = state
                    )
                }
            )
        }

        return StrategyChartModel(
            trueCountLabel = if (state.selectedSystemUsesTrueCount) {
                String.format(Locale.US, "TC %.1f", state.trueCount)
            } else {
                "TC N/A"
            },
            supportsTrueCount = state.selectedSystemUsesTrueCount,
            sections = listOf(
                StrategyTableSection(title = "Hard Totals", rows = hardRows),
                StrategyTableSection(title = "Soft Totals", rows = softRows),
                StrategyTableSection(title = "Pairs", rows = pairRows)
            )
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
        if (!isSystemUnlocked(systemId)) return false

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

    fun setPurchasedSystemIds(systemIds: Set<String>) {
        purchasedSystemIds = systemIds
        val selectedSystemId = _uiState.value.selectedCountingSystemId
        if (!isSystemUnlocked(selectedSystemId)) {
            setCountingSystem(defaultSystem.id)
        }
    }

    fun isSystemUnlocked(systemId: String): Boolean {
        return systemId in freeSystemIds || systemId in purchasedSystemIds
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
            cheatSheetRows = calculateCheatSheetRows(selectedSystem),
            strategyDeviationRows = calculateStrategyDeviationRows(
                system = selectedSystem,
                state = state
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

    private fun calculateCheatSheetRows(system: CountingSystem): List<CheatSheetRow> {
        val grouped = CardRank.entries
            .groupBy { rank -> system.getTagValue(rank) }
            .toList()
            .sortedByDescending { (value, _) -> value }

        return grouped.map { (value, ranks) ->
            CheatSheetRow(
                cardsLabel = ranks.joinToString(", ") { rank -> rank.toCardLabel() },
                tagValueLabel = formatTagValue(value)
            )
        }
    }

    private fun calculateStrategyDeviationRows(
        system: CountingSystem,
        state: CounterState
    ): List<StrategyDeviationRow> {
        if (!system.usesTrueCount()) {
            val profile = profileFor(system.id)
            val keyCount = profile.keyCount(state.totalDecks)
            val pivot = profile.pivot(state.totalDecks)
            return listOfNotNull(
                if (keyCount != null) {
                    StrategyDeviationRow(
                        playLabel = "Key Count (${state.totalDecks}D)",
                        indexLabel = "RC $keyCount"
                    )
                } else {
                    null
                },
                if (pivot != null) {
                    StrategyDeviationRow(
                        playLabel = "Pivot (${state.totalDecks}D)",
                        indexLabel = "RC $pivot"
                    )
                } else {
                    null
                }
            )
        }

        // Common true-count index plays (rough Hi-Lo style references).
        return listOf(
            StrategyDeviationRow(playLabel = "Insurance vs A", indexLabel = "Take at TC +3"),
            StrategyDeviationRow(playLabel = "16 vs 10", indexLabel = "Stand at TC 0+"),
            StrategyDeviationRow(playLabel = "15 vs 10", indexLabel = "Stand at TC +4"),
            StrategyDeviationRow(playLabel = "10 vs 10", indexLabel = "Double at TC +4"),
            StrategyDeviationRow(playLabel = "12 vs 3", indexLabel = "Stand at TC +2"),
            StrategyDeviationRow(playLabel = "12 vs 2", indexLabel = "Stand at TC +3"),
            StrategyDeviationRow(playLabel = "11 vs A", indexLabel = "Double at TC +1"),
            StrategyDeviationRow(playLabel = "9 vs 2", indexLabel = "Double at TC +1"),
            StrategyDeviationRow(playLabel = "9 vs 7", indexLabel = "Double at TC +3")
        )
    }

    private fun computeChartCell(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        state: CounterState
    ): StrategyTableCell {
        val baseAction = getBaseAction(
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )
        val baseAdjusted = applyRuleAdjustments(
            action = baseAction,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )

        if (!state.selectedSystemUsesTrueCount) {
            return StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }

        val selectedSystem = getSelectedSystem(state.selectedCountingSystemId)
        val rawTrueCount = calculateRawTrueCount(
            state = state,
            usesTrueCount = true
        )
        val decisionTrueCount = decisionCountValue(
            state = state,
            selectedSystem = selectedSystem,
            rawTrueCount = rawTrueCount
        ).toInt()

        val deviationAction = getCountDeviationAction(
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            trueCount = decisionTrueCount
        )

        if (deviationAction == null) {
            return StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }

        val deviationAdjusted = applyRuleAdjustments(
            action = deviationAction,
            handType = handType,
            handValue = handValue,
            dealerUpcard = dealerUpcard,
            rules = state.tableRules
        )

        return if (deviationAdjusted != baseAdjusted) {
            StrategyTableCell(action = deviationAdjusted, isDeviation = true)
        } else {
            StrategyTableCell(action = baseAdjusted, isDeviation = false)
        }
    }

    private fun getBaseAction(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        return when (handType) {
            ChartHandType.HARD -> getBaseHardAction(
                hardTotal = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
            ChartHandType.SOFT -> getBaseSoftAction(
                softCard = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
            ChartHandType.PAIR -> getBasePairAction(
                pairRank = handValue,
                dealerUpcard = dealerUpcard,
                rules = rules
            )
        }
    }

    private fun getBaseHardAction(
        hardTotal: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        val surrenderAllowed = rules.surrenderRule != SurrenderRule.NONE
        return when {
            hardTotal >= 17 -> "S"
            hardTotal == 16 -> when {
                surrenderAllowed && dealerUpcard in listOf(9, 10, 11) -> "Rh"
                dealerUpcard in 2..6 -> "S"
                else -> "H"
            }
            hardTotal == 15 -> when {
                surrenderAllowed && dealerUpcard == 10 -> "Rh"
                dealerUpcard in 2..6 -> "S"
                else -> "H"
            }
            hardTotal in 13..14 -> if (dealerUpcard in 2..6) "S" else "H"
            hardTotal == 12 -> if (dealerUpcard in 4..6) "S" else "H"
            hardTotal == 11 -> {
                if (dealerUpcard == 11 && rules.dealerSoft17Rule == DealerSoft17Rule.STAND) "H" else "D"
            }
            hardTotal == 10 -> if (dealerUpcard in 2..9) "D" else "H"
            hardTotal == 9 -> if (dealerUpcard in 3..6) "D" else "H"
            else -> "H"
        }
    }

    private fun getBaseSoftAction(
        softCard: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        return when (softCard) {
            9, 8 -> "S"
            7 -> when {
                dealerUpcard in 3..6 -> "Ds"
                dealerUpcard in listOf(2, 7, 8) -> {
                    if (dealerUpcard == 2 && rules.dealerSoft17Rule == DealerSoft17Rule.HIT) "Ds" else "S"
                }
                else -> "H"
            }
            6 -> if (dealerUpcard in 3..6) "D" else "H"
            5, 4 -> if (dealerUpcard in 4..6) "D" else "H"
            3, 2 -> if (dealerUpcard in 5..6) "D" else "H"
            else -> "H"
        }
    }

    private fun getBasePairAction(
        pairRank: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        val noPeekOnTen = rules.dealerPeekRule == DealerPeekRule.ACE_ONLY ||
            rules.dealerPeekRule == DealerPeekRule.NO_PEEK
        val noPeekOnAce = rules.dealerPeekRule == DealerPeekRule.NO_PEEK

        return when (pairRank) {
            11 -> if (noPeekOnAce && dealerUpcard == 11) "H" else "P"
            10 -> "S"
            9 -> if (dealerUpcard in listOf(2, 3, 4, 5, 6, 8, 9)) "P" else "S"
            8 -> {
                val blocked = (noPeekOnTen && dealerUpcard == 10) || (noPeekOnAce && dealerUpcard == 11)
                if (blocked) "H" else "P"
            }
            7 -> if (dealerUpcard in 2..7) "P" else "H"
            6 -> {
                if (rules.doubleAfterSplit) {
                    if (dealerUpcard in 2..6) "P" else "H"
                } else {
                    if (dealerUpcard in 3..6) "P" else "H"
                }
            }
            5 -> if (dealerUpcard in 2..9) "D" else "H"
            4 -> {
                if (rules.doubleAfterSplit && dealerUpcard in 5..6) "P" else "H"
            }
            3, 2 -> {
                if (rules.doubleAfterSplit) {
                    if (dealerUpcard in 2..7) "P" else "H"
                } else {
                    if (dealerUpcard in 4..7) "P" else "H"
                }
            }
            else -> "H"
        }
    }

    private fun getCountDeviationAction(
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        trueCount: Int
    ): String? {
        if (handType == ChartHandType.HARD) {
            return when {
                handValue == 16 && dealerUpcard == 10 && trueCount >= 0 -> "S"
                handValue == 15 && dealerUpcard == 10 && trueCount >= 4 -> "S"
                handValue == 12 && dealerUpcard == 3 && trueCount >= 2 -> "S"
                handValue == 12 && dealerUpcard == 2 && trueCount >= 3 -> "S"
                handValue == 11 && dealerUpcard == 11 && trueCount >= 1 -> "D"
                handValue == 10 && dealerUpcard == 11 && trueCount >= 4 -> "D"
                handValue == 10 && dealerUpcard == 10 && trueCount >= 4 -> "D"
                handValue == 9 && dealerUpcard == 2 && trueCount >= 1 -> "D"
                handValue == 9 && dealerUpcard == 7 && trueCount >= 3 -> "D"
                handValue == 16 && dealerUpcard == 9 && trueCount >= 5 -> "S"
                handValue == 15 && dealerUpcard == 9 && trueCount >= 2 -> "S"
                handValue == 15 && dealerUpcard == 11 && trueCount >= 5 -> "S"
                else -> null
            }
        }

        if (handType == ChartHandType.PAIR && handValue == 10) {
            return when {
                dealerUpcard == 5 && trueCount >= 5 -> "P"
                dealerUpcard == 6 && trueCount >= 4 -> "P"
                else -> null
            }
        }

        return null
    }

    private fun getSystemExplanation(system: CountingSystem): String {
        return when (system.id) {
            HiLoCountingSystem.id -> "Most common beginner system: balanced +1/-1 tags with truncated true-count decisions."
            KOCountingSystem.id -> "Unbalanced running-count system using deck-specific initial running counts, key counts, and a pivot."
            ReKOCountingSystem.id -> "Refined KO variant with deck-specific initial running counts and key-count references."
            HiOptIICountingSystem.id -> "Higher-power level-2 system with truncated true-count decisions; strongest with an Ace side count."
            OmegaIICountingSystem.id -> "Advanced level-2 balanced count using truncated true-count decisions; often paired with Ace side count."
            ZenCountSystem.id -> "Balanced level-2 count with heavier 4-6 tags and truncated true-count decision handling."
            WongHalvesSystem.id -> "High-precision balanced system with half-point tags and truncated true-count decision handling."
            else -> "Card-counting system with custom tag values for betting and deviation decisions."
        }
    }

    private fun applyRuleAdjustments(
        action: String,
        handType: ChartHandType,
        handValue: Int,
        dealerUpcard: Int,
        rules: TableRules
    ): String {
        var adjusted = action

        if (adjusted.startsWith("R") && rules.surrenderRule == SurrenderRule.NONE) {
            adjusted = "H"
        }

        val noPeekOnTenOnly = rules.dealerPeekRule == DealerPeekRule.ACE_ONLY
        val noPeekOnTenAndAce = rules.dealerPeekRule == DealerPeekRule.NO_PEEK
        if (adjusted in listOf("D", "Ds")) {
            val allowedByDoubleRule = isDoubleAllowed(
                handType = handType,
                handValue = handValue,
                rule = rules.doubleDownRule
            )
            val blockedByNoPeek = (noPeekOnTenOnly && dealerUpcard == 10) ||
                (noPeekOnTenAndAce && dealerUpcard in listOf(10, 11))
            if (!allowedByDoubleRule || blockedByNoPeek) {
                adjusted = if (adjusted == "Ds") "S" else "H"
            }
        }

        return adjusted
    }

    private fun isDoubleAllowed(
        handType: ChartHandType,
        handValue: Int,
        rule: DoubleDownRule
    ): Boolean {
        if (rule == DoubleDownRule.ANY_TWO_CARDS) return true

        val totalForRule = when (handType) {
            ChartHandType.HARD -> handValue
            ChartHandType.SOFT -> 11 + handValue
            ChartHandType.PAIR -> handValue * 2
        }

        return when (rule) {
            DoubleDownRule.ANY_TWO_CARDS -> true
            DoubleDownRule.NINE_TO_ELEVEN -> totalForRule in 9..11
            DoubleDownRule.TEN_TO_ELEVEN -> totalForRule in 10..11
        }
    }

    private fun CardRank.toCardLabel(): String {
        return when (this) {
            CardRank.TWO -> "2"
            CardRank.THREE -> "3"
            CardRank.FOUR -> "4"
            CardRank.FIVE -> "5"
            CardRank.SIX -> "6"
            CardRank.SEVEN -> "7"
            CardRank.EIGHT -> "8"
            CardRank.NINE -> "9"
            CardRank.TEN -> "10"
            CardRank.JACK -> "J"
            CardRank.QUEEN -> "Q"
            CardRank.KING -> "K"
            CardRank.ACE -> "A"
        }
    }

    private fun formatTagValue(value: Double): String {
        return if (value % 1.0 == 0.0) {
            when {
                value > 0 -> "+${value.toInt()}"
                value < 0 -> value.toInt().toString()
                else -> "0"
            }
        } else {
            String.format(Locale.US, "%+.1f", value)
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

        private val dealerUpcards = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        private const val MIN_TOTAL_DECKS = 1
        private const val MAX_TOTAL_DECKS = 8
        private const val MIN_DECKS_REMAINING = 0.25
        private const val DECKS_PER_CARD = 1.0 / 52.0
        private const val CARDS_PER_DECK = 52
    }

    private enum class ChartHandType {
        HARD,
        SOFT,
        PAIR
    }
}

