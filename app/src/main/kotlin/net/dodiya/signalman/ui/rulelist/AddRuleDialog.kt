package net.dodiya.signalman.ui.rulelist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var ruleName by remember { mutableStateOf("") }
    var exampleUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_new_rule)) },
        text = {
            Column {
                Text(stringResource(R.string.rule_details_hint), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text(stringResource(R.string.rule_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rule_name_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = exampleUrl,
                    onValueChange = { exampleUrl = it },
                    label = { Text(stringResource(R.string.example_url_label)) },
                    placeholder = { Text(stringResource(R.string.example_url_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("example_url_input"),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (ruleName.isNotBlank()) onConfirm(ruleName, exampleUrl) },
                enabled = ruleName.isNotBlank(),
            ) {
                Text(stringResource(R.string.action_next))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
