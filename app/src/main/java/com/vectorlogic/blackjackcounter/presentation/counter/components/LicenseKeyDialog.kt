package com.vectorlogic.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LicenseKeyDialog(
    isValidating: Boolean,
    onBuyClicked: () -> Unit,
    onVerifyPayment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var referenceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isValidating) onDismiss() },
        title = { Text("Unlock Paid Systems", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Step 1: Buy
                Text(
                    text = "Step 1: Purchase",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Pay via GCash, Maya, or credit/debit card.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onBuyClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buy Now")
                }

                // Step 2: Verify
                Text(
                    text = "Step 2: Verify Payment",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "After payment, enter the reference number shown on the confirmation page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Reference Number") },
                    singleLine = true,
                    enabled = !isValidating,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (referenceText.isNotBlank() && !isValidating) {
                                onVerifyPayment(referenceText)
                            }
                        }
                    )
                )
                if (isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = { onVerifyPayment(referenceText) },
                enabled = referenceText.isNotBlank() && !isValidating
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isValidating
            ) {
                Text("Cancel")
            }
        }
    )
}
