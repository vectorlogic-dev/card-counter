package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import java.util.Locale
import kotlin.math.round

fun CardRank.toHistoryLabel(): String = when (this) {
    CardRank.ACE -> "A"
    CardRank.TWO -> "2"
    CardRank.THREE -> "3"
    CardRank.FOUR -> "4"
    CardRank.FIVE -> "5"
    CardRank.SIX -> "6"
    CardRank.SEVEN -> "7"
    CardRank.EIGHT -> "8"
    CardRank.NINE -> "9"
    CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> "T"
}

fun formatSignedCount(value: Double, alwaysOneDecimal: Boolean = false): String {
    val rounded = round(value * 10.0) / 10.0
    val formatted = if (alwaysOneDecimal || rounded % 1.0 != 0.0) {
        String.format(Locale.US, "%.1f", rounded)
    } else {
        rounded.toInt().toString()
    }
    return if (rounded > 0) "+$formatted" else formatted
}

fun formatSignedPercent(value: Double): String {
    val rounded = round(value * 10.0) / 10.0
    val prefix = if (rounded > 0.0) "+" else ""
    return "$prefix${String.format(Locale.US, "%.1f", rounded)}"
}

@Composable
fun countColor(value: Double): Color = when {
    value > 0 -> MaterialTheme.colorScheme.tertiary
    value < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}
