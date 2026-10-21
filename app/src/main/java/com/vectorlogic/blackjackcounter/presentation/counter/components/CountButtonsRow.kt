package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.presentation.counter.formatTagValue
import com.vectorlogic.blackjackcounter.presentation.counter.toPadLabel
import com.vectorlogic.blackjackcounter.ui.theme.ChipGreenDark
import com.vectorlogic.blackjackcounter.ui.theme.ChipRedDark

@Composable
fun CountButtonsRow(
    buttonTagValues: Map<CardRank, Double>,
    onCardSelected: (CardRank) -> Unit,
    onFaceCardsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val sectionSpacing = if (compact) 4.dp else 6.dp
    // Users tap the pad without looking at the screen, so every card tap gets a tick.
    val haptics = LocalHapticFeedback.current
    val tap: (() -> Unit) -> Unit = { action ->
        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
        action()
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f),
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 7.dp else 9.dp,
                    vertical = if (compact) 6.dp else 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "COUNT PAD",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap cards as they land",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(CardRank.ACE, CardRank.TWO, CardRank.THREE, CardRank.FOUR, CardRank.FIVE).forEach { rank ->
                    val tagValue = buttonTagValues[rank] ?: 0.0
                    CardInputButton(
                        label = rank.toPadLabel(),
                        tone = tagValueToTone(tagValue),
                        tagValue = tagValue,
                        compact = compact,
                        onClick = { tap { onCardSelected(rank) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(CardRank.SIX, CardRank.SEVEN, CardRank.EIGHT, CardRank.NINE).forEach { rank ->
                    val tagValue = buttonTagValues[rank] ?: 0.0
                    CardInputButton(
                        label = rank.toPadLabel(),
                        tone = tagValueToTone(tagValue),
                        tagValue = tagValue,
                        compact = compact,
                        onClick = { tap { onCardSelected(rank) } },
                        modifier = Modifier.weight(1f)
                    )
                }
                val faceTagValue = buttonTagValues[CardRank.JACK] ?: 0.0
                CardInputButton(
                    label = CardRank.TEN.toPadLabel(),
                    tone = tagValueToTone(faceTagValue),
                    tagValue = faceTagValue,
                    compact = compact,
                    onClick = { tap(onFaceCardsSelected) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CardInputButton(
    label: String,
    tone: CardButtonTone,
    tagValue: Double,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = when (tone) {
        CardButtonTone.Positive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
        CardButtonTone.PositiveStrong -> ButtonDefaults.buttonColors(
            containerColor = ChipGreenDark,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
        CardButtonTone.Neutral -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        CardButtonTone.Negative -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        CardButtonTone.NegativeStrong -> ButtonDefaults.buttonColors(
            containerColor = ChipRedDark,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }

    val tagStr = formatTagValue(tagValue)

    Button(
        onClick = onClick,
        modifier = modifier.height(if (compact) 52.dp else 58.dp),
        colors = colors,
        shape = RoundedCornerShape(if (compact) 2.dp else 4.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = if (compact) 2.dp else 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                text = tagStr,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

private enum class CardButtonTone {
    Positive,
    PositiveStrong,
    Neutral,
    Negative,
    NegativeStrong
}

private fun tagValueToTone(tagValue: Double): CardButtonTone {
    return when {
        tagValue >= 2.0 -> CardButtonTone.PositiveStrong
        tagValue > 0.0 -> CardButtonTone.Positive
        tagValue <= -2.0 -> CardButtonTone.NegativeStrong
        tagValue < 0.0 -> CardButtonTone.Negative
        else -> CardButtonTone.Neutral
    }
}
