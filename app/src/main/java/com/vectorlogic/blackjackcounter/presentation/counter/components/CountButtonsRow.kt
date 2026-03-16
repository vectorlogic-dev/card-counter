package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.presentation.counter.formatTagValue
import com.vectorlogic.blackjackcounter.ui.theme.ChipGreenDark
import com.vectorlogic.blackjackcounter.ui.theme.ChipRedDark

@Composable
fun CountButtonsRow(
    buttonTagValues: Map<CardRank, Double>,
    onCardSelected: (CardRank) -> Unit,
    onFaceCardsSelected: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sectionSpacing = if (compact) 4.dp else 6.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(CardRank.ACE, CardRank.TWO, CardRank.THREE, CardRank.FOUR, CardRank.FIVE).forEach { rank ->
                val tagValue = buttonTagValues[rank] ?: 0.0
                CardInputButton(
                    label = rank.toButtonLabel(),
                    tone = tagValueToTone(tagValue),
                    tagValue = tagValue,
                    compact = compact,
                    onClick = { onCardSelected(rank) },
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
                    label = rank.toButtonLabel(),
                    tone = tagValueToTone(tagValue),
                    tagValue = tagValue,
                    compact = compact,
                    onClick = { onCardSelected(rank) },
                    modifier = Modifier.weight(1f)
                )
            }
            val faceTagValue = buttonTagValues[CardRank.JACK] ?: 0.0
            CardInputButton(
                label = "10-K",
                tone = tagValueToTone(faceTagValue),
                tagValue = faceTagValue,
                compact = compact,
                onClick = onFaceCardsSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun CardRank.toButtonLabel(): String = when (this) {
    CardRank.ACE -> "A"
    CardRank.TWO -> "2"
    CardRank.THREE -> "3"
    CardRank.FOUR -> "4"
    CardRank.FIVE -> "5"
    CardRank.SIX -> "6"
    CardRank.SEVEN -> "7"
    CardRank.EIGHT -> "8"
    CardRank.NINE -> "9"
    CardRank.TEN -> "10"
    CardRank.JACK, CardRank.QUEEN, CardRank.KING -> "10-K"
}

@Composable
private fun CardInputButton(
    label: String,
    tone: CardButtonTone,
    tagValue: Double,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier.height(if (compact) 54.dp else 60.dp),
        colors = colors,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = if (compact) 2.dp else 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                text = tagStr,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
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
