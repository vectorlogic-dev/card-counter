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
    ownedSystemIds: Set<String>,
    priceLabelsBySystemId: Map<String, String>,
    unlockAllPriceLabel: String,
    onSystemSelected: (String) -> Unit,
    onUnlockRequested: (String) -> Unit,
    onUnlockAllRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLockedPaidSystems = availableSystems.any { system ->
        !system.isFree && !ownedSystemIds.contains(system.id)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (hasLockedPaidSystems) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = false,
                        role = Role.Button,
                        onClick = onUnlockAllRequested
                    )
                    .padding(vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Unlock All Paid Systems",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "One-time unlock - $unlockAllPriceLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        availableSystems.forEach { system ->
            val isUnlocked = system.isFree || ownedSystemIds.contains(system.id)
            val isSelected = isUnlocked && system.id == selectedSystemId
            val priceLabel = priceLabelsBySystemId[system.id] ?: system.defaultPriceLabel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = {
                            if (isUnlocked) {
                                onSystemSelected(system.id)
                            } else {
                                onUnlockRequested(system.id)
                            }
                        }
                    )
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = isUnlocked
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = system.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    val levelSummary = when {
                        system.isFree -> "Level ${system.level} - Free"
                        isUnlocked -> "Level ${system.level} - Unlocked"
                        priceLabel != null -> "Level ${system.level} - Locked - $priceLabel"
                        else -> "Level ${system.level} - Locked"
                    }
                    Text(
                        text = levelSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUnlocked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        }
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
