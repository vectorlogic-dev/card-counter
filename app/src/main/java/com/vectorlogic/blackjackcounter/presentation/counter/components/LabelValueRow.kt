package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * A label with its value right-aligned beside it, always with a gap between them.
 *
 * When both fit on one line each keeps its natural width. When they don't, the label keeps up to
 * [labelFraction] of the row and the value wraps, right-aligned, in the space left over, so a long
 * value never runs into the label or squeezes it into a sliver.
 */
@Composable
fun LabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = Color.Unspecified,
    valueWeight: FontWeight = FontWeight.Medium,
    labelFraction: Float = 0.45f
) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            Text(text = label, style = style, color = labelColor)
            Text(
                text = value,
                style = style,
                fontWeight = valueWeight,
                color = valueColor,
                textAlign = TextAlign.End
            )
        }
    ) { measurables, constraints ->
        val (labelMeasurable, valueMeasurable) = measurables
        val gap = LabelValueGap.roundToPx()
        val labelNeeds = labelMeasurable.maxIntrinsicWidth(constraints.maxHeight)
        val valueNeeds = valueMeasurable.maxIntrinsicWidth(constraints.maxHeight)
        val rowWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            labelNeeds + gap + valueNeeds
        }
        val available = (rowWidth - gap).coerceAtLeast(0)
        val labelWidth = if (labelNeeds + valueNeeds <= available) {
            labelNeeds
        } else {
            maxOf(minOf(labelNeeds, (available * labelFraction).toInt()), available - valueNeeds)
        }.coerceIn(0, available)
        val valueWidth = available - labelWidth

        val labelPlaceable = labelMeasurable.measure(Constraints.fixedWidth(labelWidth))
        val valuePlaceable = valueMeasurable.measure(Constraints.fixedWidth(valueWidth))
        layout(rowWidth, maxOf(labelPlaceable.height, valuePlaceable.height)) {
            labelPlaceable.placeRelative(0, 0)
            valuePlaceable.placeRelative(rowWidth - valueWidth, 0)
        }
    }
}

private val LabelValueGap = 12.dp
