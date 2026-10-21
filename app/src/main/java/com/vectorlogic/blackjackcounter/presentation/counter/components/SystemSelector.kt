package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.CountingSystemGuide
import com.vectorlogic.blackjackcounter.presentation.counter.systemTraitsLabel

@Composable
fun SystemSelector(
    selectedSystemId: String,
    systems: List<CountingSystemGuide>,
    cardsSeen: Int,
    onSystemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        // Seven systems with three lines each don't fit a landscape dialog without scrolling.
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (cardsSeen > 0) {
            Text(
                text = "Switching recounts the $cardsSeen ${if (cardsSeen == 1) "card" else "cards"} " +
                    "seen this shoe with the new system's tags.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        systems.forEach { system ->
            val isSelected = system.id == selectedSystemId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSystemSelected(system.id) }
                    )
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = system.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = systemTraitsLabel(
                            level = system.level,
                            isBalanced = system.isBalanced,
                            usesTrueCount = system.usesTrueCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (system.recommendsAceSideCount) {
                        Text(
                            text = "Uses Ace side count",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
