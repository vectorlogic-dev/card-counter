package com.yourname.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import com.yourname.blackjackcounter.data.counting.CountingSystem
import com.yourname.blackjackcounter.data.counting.HiLoCountingSystem
import com.yourname.blackjackcounter.data.counting.KOCountingSystem
import com.yourname.blackjackcounter.data.counting.OmegaIICountingSystem
import com.yourname.blackjackcounter.data.counting.WongHalvesSystem
import com.yourname.blackjackcounter.data.counting.ZenCountSystem
import com.yourname.blackjackcounter.domain.model.CardRank
import com.yourname.blackjackcounter.domain.model.CheatSheetRow
import com.yourname.blackjackcounter.domain.model.CounterCalculations
import com.yourname.blackjackcounter.domain.model.CounterState
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

data class CountingSystemOption(
    val id: String,
    val displayName: String
)

class CounterViewModel : ViewModel() {

    private val allSystems: List<CountingSystem> = listOf(
        HiLoCountingSystem,
        KOCountingSystem,
        OmegaIICountingSystem,
        ZenCountSystem,
        WongHalvesSystem
    )

    private val systemsById: Map<String, CountingSystem> = allSystems.associateBy { it.id }

    val availableSystems: List<CountingSystemOption> = allSystems.map { system ->
        CountingSystemOption(id = system.id, displayName = system.displayName)
    }

    private val _uiState = MutableStateFlow(withDerivedValues(
        CounterState(
            selectedCountingSystemId = HiLoCountingSystem.id,
            selectedCountingSystemName = HiLoCountingSystem.displayName,
            selectedSystemUsesTrueCount = HiLoCountingSystem.usesTrueCount()
        )
    ))
    val uiState: StateFlow<CounterState> = _uiState.asStateFlow()

    fun onCardSeen(cardRank: CardRank) {
        val selectedSystem = getSelectedSystem(_uiState.value.selectedCountingSystemId)
        val delta = selectedSystem.getTagValue(cardRank)
        updateRunningCount(delta = delta, advancePenetration = true)
    }

    fun onFaceCardsSeen() {
        // In current systems, J/Q/K share the same tag value, so one grouped button is safe.
        onCardSeen(CardRank.JACK)
    }

    fun resetCount() {
        _uiState.update { current ->
            withDerivedValues(current.copy(runningCount = 0.0))
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
            val nextState = current.copy(
                selectedCountingSystemId = selectedSystem.id,
                selectedCountingSystemName = selectedSystem.displayName,
                selectedSystemUsesTrueCount = selectedSystem.usesTrueCount()
            )
            withDerivedValues(nextState)
        }
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
                decksRemaining = current.totalDecks.toDouble()
            )
            withDerivedValues(nextState)
        }
    }

    private fun updateRunningCount(
        delta: Double,
        advancePenetration: Boolean = false
    ) {
        _uiState.update { current ->
            val nextDecksRemaining = if (advancePenetration) {
                (current.decksRemaining - DECKS_PER_CARD).coerceAtLeast(MIN_DECKS_REMAINING)
            } else {
                current.decksRemaining
            }

            val nextState = current.copy(
                runningCount = current.runningCount + delta,
                decksRemaining = nextDecksRemaining
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
        val totalCards = state.totalDecks * CARDS_PER_DECK
        val cardsLeft = (state.decksRemaining * CARDS_PER_DECK).roundToInt().coerceIn(0, totalCards)
        val cardsDrawn = (totalCards - cardsLeft).coerceAtLeast(0)

        return state.copy(
            trueCount = calculateTrueCount(state),
            deckPenetrationPercent = calculateDeckPenetrationPercent(state),
            totalCards = totalCards,
            cardsLeft = cardsLeft,
            cardsDrawn = cardsDrawn,
            cheatSheetRows = calculateCheatSheetRows(selectedSystem)
        )
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
