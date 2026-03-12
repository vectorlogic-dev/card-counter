package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResetButton(
    onReset: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onReset,
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 40.dp else 44.dp)
    ) {
        Text(
            text = "Reset Count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

