package com.yourname.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import com.yourname.blackjackcounter.data.counting.CountingSystem
import com.yourname.blackjackcounter.data.counting.HiLoCountingSystem
import com.yourname.blackjackcounter.data.counting.HiOptIICountingSystem
import com.yourname.blackjackcounter.data.counting.KOCountingSystem
import com.yourname.blackjackcounter.data.counting.OmegaIICountingSystem
import com.yourname.blackjackcounter.data.counting.ReKOCountingSystem
import com.yourname.blackjackcounter.data.counting.WongHalvesSystem
import com.yourname.blackjackcounter.data.counting.ZenCountSystem
import com.yourname.blackjackcounter.domain.model.CardRank
import com.yourname.blackjackcounter.domain.model.CheatSheetRow
import com.yourname.blackjackcounter.domain.model.CounterCalculations
import com.yourname.blackjackcounter.domain.model.CounterState
import com.yourname.blackjackcounter.domain.model.DealerPeekRule
import com.yourname.blackjackcounter.domain.model.DealerSoft17Rule
import com.yourname.blackjackcounter.domain.model.BlackjackPayoutRule
import com.yourname.blackjackcounter.domain.model.DoubleDownRule
import com.yourname.blackjackcounter.domain.model.SurrenderRule
import com.yourname.blackjackcounter.domain.model.StrategyDeviationRow
import com.yourname.blackjackcounter.domain.model.TableRules
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

data class CountingSystemOption(
    val id: String,
    val displayName: String,
    val recommendsAceSideCount: Boolean
)

data class CountingSystemGuide(
    val id: String,
    val displayName: String,
    val level: Int,
    val isBalanced: Boolean,
    val usesTrueCount: Boolean,
    val recommendsAceSideCount: Boolean,
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

class CounterViewModel : ViewModel() {

    private val allSystems: List<CountingSystem> = listOf(
        HiLoCountingSystem,
        KOCountingSystem,
        ReKOCountingSystem,
        HiOptIICountingSystem,
        OmegaIICountingSystem,
        ZenCountSystem,
        WongHalvesSystem
    )

    private val systemsById: Map<String, CountingSystem> = allSystems.associateBy { it.id }

    val availableSystems: List<CountingSystemOption> = allSystems.map { system ->
        CountingSystemOption(
            id = system.id,
            displayName = system.displayName,
            recommendsAceSideCount = system.recommendsAceSideCount()
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
        val noPeekOnTen = tableRules.dealerPeekRule != DealerPeekRule.ACE_AND_TEN

        val baseRows = mutableListOf(
            StrategyChartRow("10,10 vs 5", "Split at very high TC (commonly around +5)"),
            StrategyChartRow("10,10 vs 6", "Split at very high TC (commonly around +4)"),
            StrategyChartRow(
                "10 vs 10 (double deviation)",
                if (noPeekOnTen) {
                    "Avoid if no-peek on 10/A is active."
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
                StrategyChartRow("A,A vs 10/A", "Use a no-peek strategy chart (ace split EV changes).")
            )
        }

        return listOf(
            StrategyChartSection(
                title = "Count-Based Split Deviations (Advanced, Hi-Lo style)",
                rows = baseRows
            )
        )
    }

    private val _uiState = MutableStateFlow(withDerivedValues(
        CounterState(
            selectedCountingSystemId = HiLoCountingSystem.id,
            selectedCountingSystemName = HiLoCountingSystem.displayName,
            selectedSystemUsesTrueCount = HiLoCountingSystem.usesTrueCount(),
            selectedSystemRecommendsAceSideCount = HiLoCountingSystem.recommendsAceSideCount()
        )
    ))
    val uiState: StateFlow<CounterState> = _uiState.asStateFlow()

    fun onCardSeen(cardRank: CardRank) {
        val selectedSystem = getSelectedSystem(_uiState.value.selectedCountingSystemId)
        val delta = selectedSystem.getTagValue(cardRank)
        updateRunningCount(
            delta = delta,
            advancePenetration = true,
            seenAce = cardRank == CardRank.ACE && selectedSystem.recommendsAceSideCount()
        )
    }

    fun onFaceCardsSeen() {
        // In current systems, J/Q/K share the same tag value, so one grouped button is safe.
        onCardSeen(CardRank.JACK)
    }

    fun resetCount() {
        _uiState.update { current ->
            withDerivedValues(
                current.copy(
                    runningCount = 0.0,
                    decksRemaining = current.totalDecks.toDouble(),
                    acesSeen = 0
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
            withDerivedValues(nextState)
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

    fun setCountingSystem(systemId: String) {
        val selectedSystem = systemsById[systemId] ?: return

        _uiState.update { current ->
            val shouldTrackAces = selectedSystem.recommendsAceSideCount()
            val nextState = current.copy(
                selectedCountingSystemId = selectedSystem.id,
                selectedCountingSystemName = selectedSystem.displayName,
                selectedSystemUsesTrueCount = selectedSystem.usesTrueCount(),
                selectedSystemRecommendsAceSideCount = shouldTrackAces,
                acesSeen = if (shouldTrackAces) current.acesSeen else 0
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

    fun resetShoe() {
        _uiState.update { current ->
            val nextState = current.copy(
                runningCount = 0.0,
                decksRemaining = current.totalDecks.toDouble(),
                acesSeen = 0
            )
            withDerivedValues(nextState)
        }
    }

    private fun updateRunningCount(
        delta: Double,
        advancePenetration: Boolean = false,
        seenAce: Boolean = false
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
                acesSeen = if (seenAce) current.acesSeen + 1 else current.acesSeen
            )
            withDerivedValues(nextState)
        }
    }

    private fun getSelectedSystem(systemId: String): CountingSystem {
        return systemsById[systemId] ?: HiLoCountingSystem
    }

    private fun calculateTrueCount(state: CounterState): Double {
        return CounterCalculations.calculateTrueCount(
            runningCount = state.runningCount,
            decksRemaining = state.decksRemaining,
            usesTrueCount = state.selectedSystemUsesTrueCount,
            minDecksRemaining = MIN_DECKS_REMAINING
        )
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
        val totalCards = state.totalDecks * CARDS_PER_DECK
        val cardsLeft = (state.decksRemaining * CARDS_PER_DECK).roundToInt().coerceIn(0, totalCards)
        val cardsDrawn = (totalCards - cardsLeft).coerceAtLeast(0)
        val shouldTrackAces = selectedSystem.recommendsAceSideCount()
        val trueCount = calculateTrueCount(state)
        val estimatedPlayerEdgePercent = CounterCalculations.calculateEstimatedPlayerEdgePercent(
            trueCount = trueCount,
            usesTrueCount = usesTrueCount,
            tableRules = state.tableRules
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
            deckPenetrationPercent = calculateDeckPenetrationPercent(state),
            totalCards = totalCards,
            cardsLeft = cardsLeft,
            cardsDrawn = cardsDrawn,
            acesSeen = acesSeen,
            acesExpected = acesExpected,
            estimatedPlayerEdgePercent = estimatedPlayerEdgePercent,
            canEstimateEdge = usesTrueCount,
            selectedSystemRecommendsAceSideCount = shouldTrackAces,
            buttonTagValues = buttonTagValues,
            cheatSheetRows = calculateCheatSheetRows(selectedSystem),
            strategyDeviationRows = calculateStrategyDeviationRows(selectedSystem)
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

    private fun calculateStrategyDeviationRows(system: CountingSystem): List<StrategyDeviationRow> {
        if (!system.usesTrueCount()) return emptyList()

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

    private companion object {
        const val MIN_TOTAL_DECKS = 1
        const val MAX_TOTAL_DECKS = 8
        const val MIN_DECKS_REMAINING = 0.25
        const val DECKS_PER_CARD = 1.0 / 52.0
        const val CARDS_PER_DECK = 52
    }
}
