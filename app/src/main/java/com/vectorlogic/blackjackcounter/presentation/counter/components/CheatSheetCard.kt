package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.CheatSheetRow
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyDeviationRow
import java.util.Locale
import kotlin.math.abs

/**
 * @param trueCountScale the selected system's Hi-Lo conversion factor; 1.0 means the indices are
 *   shown as published.
 * @param decisionCount the count the strategy chart decides with right now (TC, or RC for
 *   running-count systems), used to highlight the rows that apply.
 */
@Composable
fun CheatSheetCard(
    systemName: String,
    rows: List<CheatSheetRow>,
    strategyRows: List<StrategyDeviationRow>,
    usesTrueCount: Boolean,
    trueCountScale: Double,
    decisionCountLabel: String,
    decisionCount: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$systemName Cheat Sheet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // The deviation list (more so with surrender on) is taller than a phone-sized dialog,
            // so everything under the title scrolls.
            Column(
                modifier = Modifier
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { row ->
                    CheatSheetRow(label = row.cardsLabel, value = row.tagValueLabel)
                }

                if (strategyRows.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    StrategySectionHeader(
                        title = if (usesTrueCount) "Strategy Deviations" else "Key Counts",
                        currentCount = "$decisionCountLabel ${formatSignedCount(decisionCount)}"
                    )
                    StrategyCaption(
                        systemName = systemName,
                        usesTrueCount = usesTrueCount,
                        trueCountScale = trueCountScale
                    )
                    strategyRows.forEach { row ->
                        CheatSheetRow(
                            label = row.playLabel,
                            value = row.indexLabel,
                            active = row.isActiveAt(decisionCount)
                        )
                    }
                } else if (!usesTrueCount) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Strategy deviations hidden for running-count systems.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategySectionHeader(title: String, currentCount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = currentCount,
            modifier = Modifier
                .background(activeRowColor(), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StrategyCaption(systemName: String, usesTrueCount: Boolean, trueCountScale: Double) {
    val text = if (usesTrueCount) {
        val conversion = if (abs(trueCountScale - 1.0) > SCALE_TOLERANCE) {
            String.format(
                Locale.US,
                "Hi-Lo indices converted to %s true counts (×%.2f). ",
                systemName,
                trueCountScale
            )
        } else {
            ""
        }
        conversion + "Highlighted plays apply at the current true count. " +
            "Verify rules and deck count before using live."
    } else {
        "Running-count reference points for this deck count. Highlighted ones have been reached."
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CheatSheetRow(label: String, value: String, active: Boolean = false) {
    val rowModifier = if (active) {
        Modifier
            .background(activeRowColor(), RoundedCornerShape(6.dp))
            .semantics { stateDescription = "Applies now" }
    } else {
        Modifier
    }
    LabelValueRow(
        label = label,
        value = value,
        modifier = rowModifier.padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.bodyLarge,
        labelColor = if (active) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        valueColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        valueWeight = FontWeight.Bold
    )
}

@Composable
private fun activeRowColor() = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

private fun formatSignedCount(value: Double): String {
    val text = if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return if (value > 0) "+$text" else text
}

private const val SCALE_TOLERANCE = 1e-6
