package net.dodiya.signalman.ui.components.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun EditValueDialog(
    label: String,
    initialValue: String,
    hasReplacement: Boolean,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    onReset: () -> Unit,
) {
    var editValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $label") },
        text = {
            OutlinedTextField(
                value = editValue,
                onValueChange = { editValue = it },
                label = { Text("Replacement") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (editValue.isNotEmpty()) {
                        IconButton(onClick = { editValue = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = { onApply(editValue) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                if (hasReplacement) {
                    TextButton(onClick = onReset) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}
