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
            .height(if (compact) 38.dp else 42.dp)
    ) {
        Text(
            text = "Reset",
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

