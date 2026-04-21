package net.dodiya.signalman.ui.rulelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo

@Composable
fun AppPickerDialog(
    currentDefault: String?,
    installedApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps =
        remember(searchQuery, installedApps) {
            installedApps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_default_app)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_apps_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                            ) { Icon(Icons.Default.Clear, contentDescription = null) }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.system_chooser_none)) },
                            modifier = Modifier.clickable { onSelect(null) },
                            trailingContent = { if (currentDefault == null) RadioButton(selected = true, onClick = null) },
                        )
                    }
                    items(filteredApps) { app ->
                        ListItem(
                            headlineContent = { Text(app.name) },
                            supportingContent = { Text(app.packageName) },
                            modifier = Modifier.clickable { onSelect(app.packageName) },
                            trailingContent = { if (currentDefault == app.packageName) RadioButton(selected = true, onClick = null) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
