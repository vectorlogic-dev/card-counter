package com.vectorlogic.blackjackcounter.presentation.counter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import java.util.Locale
import kotlin.math.round

/** Single label for a rank, shared by the count pad and the recent-cards strip. */
fun CardRank.toPadLabel(): String = when (this) {
    CardRank.ACE -> "A"
    CardRank.TWO -> "2"
    CardRank.THREE -> "3"
    CardRank.FOUR -> "4"
    CardRank.FIVE -> "5"
    CardRank.SIX -> "6"
    CardRank.SEVEN -> "7"
    CardRank.EIGHT -> "8"
    CardRank.NINE -> "9"
    CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING -> "10-K"
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

fun formatTagValue(value: Double): String {
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

@Composable
fun countColor(value: Double): Color = when {
    value > 0 -> MaterialTheme.colorScheme.tertiary
    value < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

/** Second line in the system selector, e.g. "Level 2 · Balanced · True count". */
fun systemTraitsLabel(level: Int, isBalanced: Boolean, usesTrueCount: Boolean): String {
    val balance = if (isBalanced) "Balanced" else "Unbalanced"
    val count = if (usesTrueCount) "True count" else "Running count only"
    return "Level $level · $balance · $count"
}

/**
 * Note shown after switching systems. Switching re-tags every card already seen, so the running
 * count can jump (Hi-Lo +13 becomes Omega II +22); say so rather than change it silently.
 * Returns null when nothing visible changed.
 */
fun systemSwitchMessage(
    previousSystemId: String,
    previousRunningCount: Double,
    newSystemId: String,
    newSystemName: String,
    newRunningCount: Double,
    cardsSeen: Int
): String? {
    if (previousSystemId == newSystemId) return null
    val before = formatSignedCount(previousRunningCount)
    val after = formatSignedCount(newRunningCount)
    return when {
        cardsSeen > 0 -> {
            val cards = if (cardsSeen == 1) "1 card" else "$cardsSeen cards"
            "Recounted $cards with $newSystemName: RC $before → $after"
        }
        // No cards yet, but unbalanced systems start from a non-zero IRC.
        before != after -> "$newSystemName starts at RC $after"
        else -> null
    }
}
