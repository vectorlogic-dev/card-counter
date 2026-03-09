package com.yourname.blackjackcounter.presentation.counter

import androidx.lifecycle.ViewModel
import com.yourname.blackjackcounter.data.counting.CountingSystem
import com.yourname.blackjackcounter.data.counting.HiLoCountingSystem
import com.yourname.blackjackcounter.data.counting.KOCountingSystem
import com.yourname.blackjackcounter.data.counting.OmegaIICountingSystem
import com.yourname.blackjackcounter.data.counting.WongHalvesSystem
import com.yourname.blackjackcounter.data.counting.ZenCountSystem
import com.yourname.blackjackcounter.domain.model.CardRank
import com.yourname.blackjackcounter.domain.model.CounterState
import kotlin.math.round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    // V1 exposes only Hi-Lo in the UI, while additional systems are already wired for later use.
    private val uiExposedSystems: List<CountingSystem> = listOf(HiLoCountingSystem)

    val availableSystems: List<CountingSystemOption> = uiExposedSystems.map { system ->
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

    fun incrementPositive() {
        updateRunningCount(delta = 1.0)
    }

    fun incrementNeutral() {
        // Included for explicit 0-value tracking button in training workflow.
        _uiState.update { current -> current.copy(trueCount = calculateTrueCount(current)) }
    }

    fun incrementNegative() {
        updateRunningCount(delta = -1.0)
    }

    fun onCardSeen(cardRank: CardRank) {
        val selectedSystem = getSelectedSystem(_uiState.value.selectedCountingSystemId)
        val delta = selectedSystem.getTagValue(cardRank)
        updateRunningCount(delta = delta)
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

    private fun updateRunningCount(delta: Double) {
        _uiState.update { current ->
            val nextState = current.copy(runningCount = current.runningCount + delta)
            withDerivedValues(nextState)
        }
    }

    private fun getSelectedSystem(systemId: String): CountingSystem {
        return systemsById[systemId] ?: HiLoCountingSystem
    }

    private fun calculateTrueCount(state: CounterState): Double {
        if (!state.selectedSystemUsesTrueCount) return 0.0

        // Safety guard in case deck control is ever changed and allows 0.
        val safeDecksRemaining = state.decksRemaining.coerceAtLeast(MIN_DECKS_REMAINING)
        val rawTrueCount = state.runningCount / safeDecksRemaining
        return roundToOneDecimal(rawTrueCount)
    }

    private fun roundToOneDecimal(value: Double): Double {
        return round(value * 10.0) / 10.0
    }

    private fun calculateDeckPenetrationPercent(state: CounterState): Double {
        val dealtDecks = (state.totalDecks - state.decksRemaining).coerceAtLeast(0.0)
        val penetration = (dealtDecks / state.totalDecks.toDouble()) * 100.0
        return roundToOneDecimal(penetration)
    }

    private fun withDerivedValues(state: CounterState): CounterState {
        return state.copy(
            trueCount = calculateTrueCount(state),
            deckPenetrationPercent = calculateDeckPenetrationPercent(state)
        )
    }

    private companion object {
        const val MIN_TOTAL_DECKS = 1
        const val MAX_TOTAL_DECKS = 8
        const val MIN_DECKS_REMAINING = 0.25
    }
}
