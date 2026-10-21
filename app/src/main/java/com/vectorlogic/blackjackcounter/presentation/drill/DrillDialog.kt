package com.vectorlogic.blackjackcounter.presentation.drill

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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

private val CardListSaver: Saver<List<CardRank>, Any> = listSaver(
    save = { cards -> cards.map { it.ordinal } },
    restore = { ordinals -> ordinals.map { CardRank.entries[it] } }
)

@Composable
fun DrillDialog(
    system: CountingSystem,
    onDismiss: () -> Unit
) {
    // Everything is saveable so a rotation resumes the drill where it was instead of
    // dropping back to SETUP (the dialog itself is reopened from saved state).
    var phase by rememberSaveable { mutableStateOf(DrillPhase.SETUP) }
    var drillSize by rememberSaveable { mutableStateOf(DrillConfig().size) }
    var drillSpeed by rememberSaveable { mutableStateOf(DrillConfig().speed) }
    val config = DrillConfig(size = drillSize, speed = drillSpeed)
    var cards by rememberSaveable(stateSaver = CardListSaver) { mutableStateOf(emptyList()) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var countdownValue by rememberSaveable { mutableIntStateOf(3) }
    var startedAtMs by rememberSaveable { mutableLongStateOf(0L) }
    var elapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    var userInput by rememberSaveable { mutableStateOf("") }
    var submittedCount by rememberSaveable { mutableStateOf<Double?>(null) }
    // False during the short blank between cards (not saved: the deal loop restarts with it shown).
    var cardVisible by remember { mutableStateOf(true) }

    val result = submittedCount?.let { reported ->
        DrillResult(
            actualRunningCount = DrillEngine.runningCountFor(system, cards),
            userReportedCount = reported,
            cards = cards,
            elapsedMs = elapsedMs,
            systemDisplayName = system.displayName
        )
    }
    val drillInProgress = phase == DrillPhase.COUNTDOWN ||
        phase == DrillPhase.RUNNING ||
        phase == DrillPhase.INPUT
    val canSubmit = userInput.toDoubleOrNull() != null
    val submit = {
        if (canSubmit) {
            submittedCount = userInput.toDouble()
            phase = DrillPhase.RESULT
        }
    }

    AlertDialog(
        // Back or a stray tap outside the dialog shouldn't throw away a drill that is running or
        // waiting for its answer; the Close button still ends it on purpose.
        onDismissRequest = { if (!drillInProgress) onDismiss() },
        title = { DrillTitle(systemName = system.displayName) },
        text = {
            when (phase) {
                DrillPhase.SETUP -> SetupContent(
                    config = config,
                    onConfigChanged = {
                        drillSize = it.size
                        drillSpeed = it.speed
                    }
                )
                DrillPhase.COUNTDOWN -> CountdownContent(value = countdownValue)
                DrillPhase.RUNNING -> RunningContent(
                    rank = cards.getOrNull(currentIndex)?.takeIf { cardVisible },
                    position = currentIndex + 1,
                    total = cards.size
                )
                DrillPhase.INPUT -> InputContent(
                    userInput = userInput,
                    onUserInputChanged = { userInput = it },
                    onSubmit = submit
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
                    submittedCount = null
                    phase = DrillPhase.COUNTDOWN
                }) { Text("Start") }
                DrillPhase.INPUT -> Button(enabled = canSubmit, onClick = submit) { Text("Submit") }
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
                val gapMs = cardGapMs(config.speed.intervalMs)
                while (currentIndex < cards.size) {
                    cardVisible = true
                    delay(config.speed.intervalMs - gapMs)
                    // A short blank between cards, so the same rank twice in a row still
                    // reads as two cards.
                    cardVisible = false
                    delay(gapMs)
                    currentIndex += 1
                }
                cardVisible = true
                elapsedMs = System.currentTimeMillis() - startedAtMs
                phase = DrillPhase.INPUT
            }
            else -> {}
        }
    }
}

@Composable
private fun DrillTitle(systemName: String) {
    Column {
        Text("Drill mode")
        Text(
            text = systemName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
        // Chips keep their labels on one line and wrap to a new row if the dialog is narrow.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DrillSize.entries.forEach { size ->
                FilterChip(
                    selected = config.size == size,
                    onClick = { onConfigChanged(config.copy(size = size)) },
                    label = { ChipLabel(size.label) }
                )
            }
        }
        Text("Speed", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DrillSpeed.entries.forEach { speed ->
                FilterChip(
                    selected = config.speed == speed,
                    onClick = { onConfigChanged(config.copy(speed = speed)) },
                    label = { ChipLabel(speed.label) }
                )
            }
        }
    }
}

@Composable
private fun ChipLabel(text: String) {
    Text(text = text, maxLines = 1, softWrap = false)
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
private fun RunningContent(rank: CardRank?, position: Int, total: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp),
            contentAlignment = Alignment.Center
        ) {
            // Nothing is drawn during the gap between cards, so each card visibly arrives.
            if (rank != null) {
                Surface(
                    modifier = Modifier.size(width = 116.dp, height = 164.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = rank.toDrillLabel(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Text(
            text = "${position.coerceIn(1, total.coerceAtLeast(1))} / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InputContent(
    userInput: String,
    onUserInputChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Open the keyboard straight away; the answer is the only thing left to do.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("What was the final running count?", style = MaterialTheme.typography.bodyMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Some number keyboards have no '-' key, and many answers are negative.
            FilledTonalButton(onClick = { onUserInputChanged(toggleSign(userInput)) }) {
                Text("±", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
                value = userInput,
                onValueChange = { input -> onUserInputChanged(input.filter { it.isDigit() || it == '-' || it == '.' }) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }
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

/** Blank time at the end of each card's slot: a fifth of it, kept between 100 and 200 ms. */
private fun cardGapMs(intervalMs: Long): Long = (intervalMs / 5).coerceIn(100L, 200L)

private fun toggleSign(input: String): String =
    if (input.startsWith("-")) input.removePrefix("-") else "-$input"

private fun formatCount(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        "%.1f".format(rounded)
    }
}
