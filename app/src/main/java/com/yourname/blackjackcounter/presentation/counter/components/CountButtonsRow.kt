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

@Composable
fun CountButtonsRow(
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
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "A",
                tone = CardButtonTone.Negative,
                onClick = { onCardSelected(CardRank.ACE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "2",
                tone = CardButtonTone.Positive,
                onClick = { onCardSelected(CardRank.TWO) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "3",
                tone = CardButtonTone.Positive,
                onClick = { onCardSelected(CardRank.THREE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "4",
                tone = CardButtonTone.Positive,
                onClick = { onCardSelected(CardRank.FOUR) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "5",
                tone = CardButtonTone.Positive,
                onClick = { onCardSelected(CardRank.FIVE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "6",
                tone = CardButtonTone.Positive,
                onClick = { onCardSelected(CardRank.SIX) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "7",
                tone = CardButtonTone.Neutral,
                onClick = { onCardSelected(CardRank.SEVEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "8",
                tone = CardButtonTone.Neutral,
                onClick = { onCardSelected(CardRank.EIGHT) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "9",
                tone = CardButtonTone.Neutral,
                onClick = { onCardSelected(CardRank.NINE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "10",
                tone = CardButtonTone.Negative,
                onClick = { onCardSelected(CardRank.TEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "T/J/Q/K",
                tone = CardButtonTone.Negative,
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
        CardButtonTone.Neutral -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        CardButtonTone.Negative -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
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
    Neutral,
    Negative
}
