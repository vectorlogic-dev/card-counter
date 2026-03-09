package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun BetRecommendationCard(
    units: Int,
    countLabel: String,
    countValue: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Bet Recommendation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$units units",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = unitsColor(units)
            )
            Text(
                text = "Based on $countLabel ${formatSignedCount(countValue)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ramp: 1-2-4-8-12 units",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun unitsColor(units: Int): Color {
    return when {
        units >= 8 -> MaterialTheme.colorScheme.tertiary
        units >= 4 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
}

private fun formatSignedCount(value: Double): String {
    val rounded = String.format(Locale.US, "%.1f", value)
    return if (value > 0.0) "+$rounded" else rounded
}
