package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.ui.components.AppListItem
import net.dodiya.signalman.ui.editrule.EditRuleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetAppSection(
    targetPackage: String?,
    onEvent: (EditRuleEvent) -> Unit,
    installedApps: List<AppInfo>,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredApps =
        remember(searchQuery, installedApps) {
            if (searchQuery.isBlank()) {
                installedApps
            } else {
                installedApps.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.search_app_label)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = context.getString(R.string.cd_clear_search))
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AppListItem(
                    name = context.getString(R.string.system_chooser_default),
                    packageName = context.getString(R.string.system_will_ask),
                    isSelected = targetPackage == null,
                    onClick = { onEvent(EditRuleEvent.TargetPackageChanged(null)) },
                )
            }

            items(filteredApps, key = { it.packageName }) { app ->
                AppListItem(
                    name = app.name,
                    packageName = app.packageName,
                    isSelected = app.packageName == targetPackage,
                    onClick = { onEvent(EditRuleEvent.TargetPackageChanged(app.packageName)) },
                    icon = app.icon,
                )
            }
        }
    }
}
