package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.presentation.counter.CountingSystemOption

@Composable
fun SystemSelector(
    selectedSystemId: String,
    availableSystems: List<CountingSystemOption>,
    hasPro: Boolean,
    hasFullUnlock: Boolean,
    onSystemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableSystems.forEach { system ->
            val isSelected = system.id == selectedSystemId
            val isProLocked = system.requiresPro && !hasPro
            val isFullLocked = system.requiresFullUnlock && !hasFullUnlock
            val isLocked = isProLocked || isFullLocked
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
                    onClick = null,
                    enabled = !isLocked
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = when {
                            isFullLocked -> "${system.displayName} (Full)"
                            isProLocked -> "${system.displayName} (Pro)"
                            else -> system.displayName
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isLocked) {
                        Text(
                            text = if (isFullLocked) {
                                "Requires Full unlock"
                            } else {
                                "Locked in Free tier"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
