package com.vectorlogic.blackjackcounter.presentation.drill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.vectorlogic.blackjackcounter.data.counting.CountingSystem
import com.vectorlogic.blackjackcounter.domain.drill.DrillConfig
import com.vectorlogic.blackjackcounter.domain.drill.DrillEngine
import com.vectorlogic.blackjackcounter.domain.drill.DrillResult
import com.vectorlogic.blackjackcounter.domain.drill.DrillSize
import com.vectorlogic.blackjackcounter.domain.drill.DrillSpeed
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class DrillPhase { SETUP, COUNTDOWN, RUNNING, INPUT, RESULT }

@Composable
fun DrillDialog(
    system: CountingSystem,
    onDismiss: () -> Unit
) {
    var phase by remember { mutableStateOf(DrillPhase.SETUP) }
    var config by remember { mutableStateOf(DrillConfig()) }
    var cards by remember { mutableStateOf<List<CardRank>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var countdownValue by remember { mutableStateOf(3) }
    var startedAtMs by remember { mutableStateOf(0L) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var userInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DrillResult?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Drill mode — ${system.displayName}") },
        text = {
            when (phase) {
                DrillPhase.SETUP -> SetupContent(
                    config = config,
                    onConfigChanged = { config = it }
                )
                DrillPhase.COUNTDOWN -> CountdownContent(value = countdownValue)
                DrillPhase.RUNNING -> RunningContent(
                    rank = cards.getOrNull(currentIndex),
                    index = currentIndex,
                    total = cards.size
                )
                DrillPhase.INPUT -> InputContent(
                    userInput = userInput,
                    onUserInputChanged = { userInput = it }
                )
                DrillPhase.RESULT -> result?.let { ResultContent(it) }
            }
        },
        confirmButton = {
            when (phase) {
                DrillPhase.SETUP -> Button(onClick = {
                    cards = DrillEngine.generateCards(config.size)
                    currentIndex = 0
                    countdownValue = 3
                    userInput = ""
                    result = null
                    phase = DrillPhase.COUNTDOWN
                }) { Text("Start") }
                DrillPhase.INPUT -> Button(
                    enabled = userInput.toDoubleOrNull() != null,
                    onClick = {
                        val parsed = userInput.toDouble()
                        result = DrillResult(
                            actualRunningCount = DrillEngine.runningCountFor(system, cards),
                            userReportedCount = parsed,
                            cards = cards,
                            elapsedMs = elapsedMs,
                            systemDisplayName = system.displayName
                        )
                        phase = DrillPhase.RESULT
                    }
                ) { Text("Submit") }
                DrillPhase.RESULT -> Button(onClick = { phase = DrillPhase.SETUP }) {
                    Text("Run again")
                }
                else -> {}
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    LaunchedEffect(phase) {
        when (phase) {
            DrillPhase.COUNTDOWN -> {
                countdownValue = 3
                while (countdownValue > 0) {
                    delay(700L)
                    countdownValue -= 1
                }
                startedAtMs = System.currentTimeMillis()
                currentIndex = 0
                phase = DrillPhase.RUNNING
            }
            DrillPhase.RUNNING -> {
                while (currentIndex < cards.size) {
                    delay(config.speed.intervalMs)
                    currentIndex += 1
                }
                elapsedMs = System.currentTimeMillis() - startedAtMs
                phase = DrillPhase.INPUT
            }
            else -> {}
        }
    }
}

@Composable
private fun SetupContent(
    config: DrillConfig,
    onConfigChanged: (DrillConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Cards will flash one at a time. Track the running count " +
                "in your head, then enter the final value.",
            style = MaterialTheme.typography.bodySmall
        )
        Text("Length", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DrillSize.entries.forEach { size ->
                FilterChip(
                    selected = config.size == size,
                    onClick = { onConfigChanged(config.copy(size = size)) },
                    label = { Text(size.label) }
                )
            }
        }
        Text("Speed", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DrillSpeed.entries.forEach { speed ->
                FilterChip(
                    selected = config.speed == speed,
                    onClick = { onConfigChanged(config.copy(speed = speed)) },
                    label = { Text(speed.label) },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }
    }
}

@Composable
private fun CountdownContent(value: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RunningContent(rank: CardRank?, index: Int, total: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank?.toDrillLabel() ?: "",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "${index.coerceAtMost(total)} / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InputContent(userInput: String, onUserInputChanged: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("What was the final running count?", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = userInput,
            onValueChange = { input -> onUserInputChanged(input.filter { it.isDigit() || it == '-' || it == '.' }) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ResultContent(result: DrillResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ResultRow("Actual", formatCount(result.actualRunningCount))
        ResultRow("You said", formatCount(result.userReportedCount))
        ResultRow(
            label = if (result.isExact) "Result" else "Off by",
            value = if (result.isExact) "PERFECT" else formatCount(result.absoluteError),
            valueColor = if (result.isExact) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
        ResultRow("Cards", "${result.cards.size}")
        ResultRow(
            "Speed",
            "%.1f cards/sec".format(result.cardsPerSecond)
        )
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun CardRank.toDrillLabel(): String = when (this) {
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

private fun formatCount(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        "%.1f".format(rounded)
    }
}
