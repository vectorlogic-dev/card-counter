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
                onClick = { onCardSelected(CardRank.ACE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "2",
                onClick = { onCardSelected(CardRank.TWO) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "3",
                onClick = { onCardSelected(CardRank.THREE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "4",
                onClick = { onCardSelected(CardRank.FOUR) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "5",
                onClick = { onCardSelected(CardRank.FIVE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "6",
                onClick = { onCardSelected(CardRank.SIX) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "7",
                onClick = { onCardSelected(CardRank.SEVEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "8",
                onClick = { onCardSelected(CardRank.EIGHT) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CardInputButton(
                label = "9",
                onClick = { onCardSelected(CardRank.NINE) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "10",
                onClick = { onCardSelected(CardRank.TEN) },
                modifier = Modifier.weight(1f)
            )
            CardInputButton(
                label = "T/J/Q/K",
                onClick = onFaceCardsSelected,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
private fun CardInputButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
