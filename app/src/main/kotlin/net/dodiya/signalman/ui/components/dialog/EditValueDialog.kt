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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.dodiya.signalman.R

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
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.edit_label_format, label)) },
        text = {
            OutlinedTextField(
                value = editValue,
                onValueChange = { editValue = it },
                label = { Text(stringResource(R.string.replacement_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (editValue.isNotEmpty()) {
                        IconButton(onClick = { editValue = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = context.getString(R.string.action_clear))
                        }
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = { onApply(editValue) }) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            Row {
                if (hasReplacement) {
                    TextButton(onClick = onReset) {
                        Text(stringResource(R.string.action_reset), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        },
    )
}
