package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.formatSignedCount

private val BET_RAMP = listOf(1, 2, 4, 8, 12)

@Composable
fun BetRecommendationCard(
    units: Int,
    countLabel: String,
    countValue: Double,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val unitColor = unitsColor(units)
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.medium
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 8.dp else 12.dp,
                    vertical = if (compact) 6.dp else 9.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)) {
                Text(
                    text = "BET RAMP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$countLabel ${formatSignedCount(countValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BET_RAMP.forEach { threshold ->
                        val active = units >= threshold
                        Box(
                            modifier = Modifier
                                .size(
                                    if (active) {
                                        if (compact) 8.dp else 10.dp
                                    } else {
                                        if (compact) 6.dp else 8.dp
                                    }
                                )
                                .background(
                                    color = if (active) unitColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                    Text(
                        text = "1 2 4 8 12",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "$units",
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = unitColor
                )
                Text(
                    text = "units live",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

