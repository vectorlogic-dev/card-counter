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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.formatSignedCount

private val BET_RAMP = listOf(1, 2, 4, 8, 12)

@Composable
fun BetRecommendationCard(
    units: Int,
    countLabel: String,
    countValue: Double,
    modifier: Modifier = Modifier,
    compact: Boolean = false
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
                BetRampPips(units = units, activeColor = unitColor, compact = compact)
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

/** One column per ramp step: the pip, with its unit count centred underneath. */
@Composable
private fun BetRampPips(units: Int, activeColor: Color, compact: Boolean) {
    val currentStep = BET_RAMP.lastOrNull { units >= it }
    val activePip = if (compact) 8.dp else 10.dp
    val idlePip = if (compact) 6.dp else 8.dp
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Bet ramp ${BET_RAMP.joinToString(", ")} units, at $units"
        },
        horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
    ) {
        BET_RAMP.forEach { step ->
            val active = units >= step
            val current = step == currentStep
            Column(
                modifier = Modifier.width(if (compact) 18.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 2.dp)
            ) {
                // Fixed-height slot so the numbers line up whether or not the pip is lit.
                Box(modifier = Modifier.size(activePip), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(if (active) activePip else idlePip)
                            .background(
                                color = if (active) {
                                    activeColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                },
                                shape = CircleShape
                            )
                    )
                }
                Text(
                    text = "$step",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                    color = if (current) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    },
                    maxLines = 1
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

