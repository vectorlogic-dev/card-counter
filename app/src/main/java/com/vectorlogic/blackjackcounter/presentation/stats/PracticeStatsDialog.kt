package com.vectorlogic.blackjackcounter.presentation.stats

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.domain.stats.DrillRecord
import com.vectorlogic.blackjackcounter.domain.stats.DrillSummary
import com.vectorlogic.blackjackcounter.domain.stats.ExactTally
import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistory
import com.vectorlogic.blackjackcounter.domain.stats.QuizSummary
import com.vectorlogic.blackjackcounter.domain.stats.RECENT_WINDOW
import com.vectorlogic.blackjackcounter.domain.stats.drillSummary
import com.vectorlogic.blackjackcounter.domain.stats.quizSummary
import com.vectorlogic.blackjackcounter.presentation.counter.components.LabelValueRow
import com.vectorlogic.blackjackcounter.presentation.drill.formatCount
import com.vectorlogic.blackjackcounter.presentation.drill.labelRes

@Composable
fun PracticeStatsDialog(
    history: PracticeHistory,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val drills = history.drillSummary()
    val quiz = history.quizSummary()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stats_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (history.isEmpty) {
                    Text(stringResource(R.string.stats_empty), style = MaterialTheme.typography.bodyMedium)
                } else {
                    SectionHeading(stringResource(R.string.stats_drills_heading))
                    if (drills == null) {
                        EmptyNote(stringResource(R.string.stats_drills_none))
                    } else {
                        DrillSection(drills, totalDrills = history.drills.size)
                    }
                    HorizontalDivider()
                    SectionHeading(stringResource(R.string.stats_quiz_heading))
                    if (quiz == null) {
                        EmptyNote(stringResource(R.string.stats_quiz_none))
                    } else {
                        QuizSection(quiz)
                    }
                }
                Text(
                    stringResource(R.string.stats_on_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        dismissButton = {
            if (!history.isEmpty) {
                TextButton(onClick = { confirmClear = true }) {
                    Text(stringResource(R.string.stats_clear), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.stats_clear_title)) },
            text = { Text(stringResource(R.string.stats_clear_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onClear()
                    confirmClear = false
                }) {
                    Text(stringResource(R.string.stats_clear_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun DrillSection(summary: DrillSummary, totalDrills: Int) {
    TileRow {
        StatTile(totalDrills.toString(), stringResource(R.string.stats_tile_drills), Modifier.weight(1f))
        StatTile(percentText(summary.all), stringResource(R.string.stats_tile_exact), Modifier.weight(1f))
        StatTile(
            percentText(summary.recent),
            stringResource(R.string.stats_tile_last_n, minOf(RECENT_WINDOW, totalDrills)),
            Modifier.weight(1f)
        )
    }

    Streak(summary.streak)

    summary.averageMiss?.let { miss ->
        LabelValueRow(
            label = stringResource(R.string.stats_average_miss),
            value = formatCount(miss),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    SubHeading(stringResource(R.string.stats_by_speed))
    summary.bySpeed.forEach { (speed, tally) ->
        LabelValueRow(
            label = stringResource(speed.labelRes),
            value = tallyText(tally),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    SubHeading(stringResource(R.string.stats_recent))
    summary.latest.forEach { drill -> DrillEntry(drill) }
}

@Composable
private fun DrillEntry(drill: DrillRecord) {
    val setup = stringResource(
        R.string.stats_drill_setup,
        pluralStringResource(R.plurals.stats_cards, drill.cardCount, drill.cardCount),
        stringResource(drill.speed.labelRes)
    )
    LabelValueRow(
        label = stringResource(
            R.string.stats_entry_three,
            formatDate(drill.finishedAtMs),
            systemName(drill.systemId),
            setup
        ),
        value = if (drill.isExact) {
            stringResource(R.string.stats_exact)
        } else {
            stringResource(R.string.stats_off_by, formatCount(drill.absoluteError))
        },
        valueColor = if (drill.isExact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        labelFraction = 0.7f
    )
}

@Composable
private fun QuizSection(summary: QuizSummary) {
    TileRow {
        StatTile(summary.rounds.toString(), stringResource(R.string.stats_tile_rounds), Modifier.weight(1f))
        StatTile(percentText(summary.answers), stringResource(R.string.stats_tile_correct), Modifier.weight(1f))
        StatTile(
            summary.bestRound?.let {
                stringResource(R.string.stats_score, it.correctCount, it.questionCount)
            }.orEmpty(),
            stringResource(R.string.stats_tile_best),
            Modifier.weight(1f)
        )
    }

    if (summary.mostMissed.isNotEmpty()) {
        SubHeading(stringResource(R.string.stats_most_missed))
        summary.mostMissed.forEach { (hand, times) ->
            LabelValueRow(
                label = stringResource(R.string.hand_vs_dealer, hand.hand, hand.dealer),
                value = pluralStringResource(R.plurals.stats_times, times, times),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    SubHeading(stringResource(R.string.stats_recent))
    summary.latest.forEach { round ->
        LabelValueRow(
            label = stringResource(
                R.string.stats_entry_two,
                formatDate(round.finishedAtMs),
                systemName(round.systemId)
            ),
            value = stringResource(R.string.stats_score, round.correctCount, round.questionCount),
            valueColor = if (round.correctCount == round.questionCount) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Unspecified
            },
            labelFraction = 0.7f
        )
    }
}

/** A dot per recent drill, oldest first: filled when exact, hollow when missed. */
@Composable
private fun Streak(results: List<Boolean>) {
    val exact = results.count { it }
    val description = pluralStringResource(R.plurals.stats_streak_description, results.size, exact, results.size)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description }
    ) {
        Text(
            pluralStringResource(R.plurals.stats_streak_label, results.size, results.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            results.forEach { wasExact ->
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = if (wasExact) MaterialTheme.colorScheme.primary else Color.Transparent,
                    border = if (wasExact) null else BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {}
            }
        }
    }
}

@Composable
private fun TileRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = content
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    // Read as "Exact, 64%" rather than two separate items.
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun SubHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun percentText(tally: ExactTally): String =
    tally.percent?.let { stringResource(R.string.stats_percent, it) } ?: "–"

@Composable
private fun tallyText(tally: ExactTally): String =
    pluralStringResource(R.plurals.stats_tally, tally.total, tally.exact, tally.total)

@Composable
private fun formatDate(ms: Long): String =
    DateUtils.formatDateTime(LocalContext.current, ms, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH)

private fun systemName(systemId: String): String =
    CountingSystemsCatalog.systemsById[systemId]?.displayName ?: systemId
