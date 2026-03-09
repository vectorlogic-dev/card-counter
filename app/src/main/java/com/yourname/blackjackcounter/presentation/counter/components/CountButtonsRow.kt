package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.blackjackcounter.domain.model.CardRank
import com.yourname.blackjackcounter.ui.theme.ChipGreenDark
import com.yourname.blackjackcounter.ui.theme.ChipRedDark

@Composable
fun CountButtonsRow(
    buttonTagValues: Map<CardRank, Double>,
    onCardSelected: (CardRank) -> Unit,
    onFaceCardsSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val aceTone = buttonTagValues.toTone(CardRank.ACE)
        val twoTone = buttonTagValues.toTone(CardRank.TWO)
        val threeTone = buttonTagValues.toTone(CardRank.THREE)
        val fourTone = buttonTagValues.toTone(CardRank.FOUR)
        val fiveTone = buttonTagValues.toTone(CardRank.FIVE)
        val sixTone = buttonTagValues.toTone(CardRank.SIX)
        val sevenTone = buttonTagValues.toTone(CardRank.SEVEN)
        val eightTone = buttonTagValues.toTone(CardRank.EIGHT)
        val nineTone = buttonTagValues.toTone(CardRank.NINE)
        val tenTone = buttonTagValues.toTone(CardRank.TEN)
        val faceTone = buttonTagValues.toTone(CardRank.JACK)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "A",
                tone = aceTone,
                onClick = { onCardSelected(CardRank.ACE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "2",
                tone = twoTone,
                onClick = { onCardSelected(CardRank.TWO) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "3",
                tone = threeTone,
                onClick = { onCardSelected(CardRank.THREE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "4",
                tone = fourTone,
                onClick = { onCardSelected(CardRank.FOUR) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "5",
                tone = fiveTone,
                onClick = { onCardSelected(CardRank.FIVE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "6",
                tone = sixTone,
                onClick = { onCardSelected(CardRank.SIX) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "7",
                tone = sevenTone,
                onClick = { onCardSelected(CardRank.SEVEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "8",
                tone = eightTone,
                onClick = { onCardSelected(CardRank.EIGHT) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "9",
                tone = nineTone,
                onClick = { onCardSelected(CardRank.NINE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "10",
                tone = tenTone,
                onClick = { onCardSelected(CardRank.TEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "T/J/Q/K",
                tone = faceTone,
                onClick = onFaceCardsSelected,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
private fun CardInputButton(
    label: String,
    tone: CardButtonTone,
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

    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        colors = colors
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private enum class CardButtonTone {
    Positive,
    PositiveStrong,
    Neutral,
    Negative,
    NegativeStrong
}

private fun Map<CardRank, Double>.toTone(rank: CardRank): CardButtonTone {
    val tagValue = this[rank] ?: 0.0
    return tagValueToTone(tagValue)
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
