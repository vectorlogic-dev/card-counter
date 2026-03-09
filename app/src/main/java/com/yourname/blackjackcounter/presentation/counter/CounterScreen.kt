package com.yourname.blackjackcounter.presentation.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.blackjackcounter.presentation.counter.components.CountButtonsRow
import com.yourname.blackjackcounter.presentation.counter.components.CountDisplayCard
import com.yourname.blackjackcounter.presentation.counter.components.DeckControlCard
import com.yourname.blackjackcounter.presentation.counter.components.ResetButton
import java.util.Locale
import kotlin.math.round

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Blackjack Counter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "System: ${state.selectedCountingSystemName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountDisplayCard(
                    title = "Running",
                    value = formatSignedCount(state.runningCount),
                    compact = true,
                    modifier = Modifier.weight(1f)
                )

                CountDisplayCard(
                    title = "True",
                    value = if (state.selectedSystemUsesTrueCount) {
                        formatSignedCount(state.trueCount, alwaysOneDecimal = true)
                    } else {
                        "N/A"
                    },
                    compact = true,
                    modifier = Modifier.weight(1f)
                )
            }

            DeckControlCard(
                totalDecks = state.totalDecks,
                decksRemaining = state.decksRemaining,
                deckPenetrationPercent = state.deckPenetrationPercent,
                onTotalDecksChanged = viewModel::setTotalDecks,
                onDecksRemainingChanged = viewModel::setDecksRemaining
            )

            Text(
                text = "Tap each card rank as it appears",
                style = MaterialTheme.typography.titleSmall
            )

            CountButtonsRow(
                onCardSelected = viewModel::onCardSeen,
                onFaceCardsSelected = viewModel::onFaceCardsSeen
            )

            ResetButton(onReset = viewModel::resetCount)
        }
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
