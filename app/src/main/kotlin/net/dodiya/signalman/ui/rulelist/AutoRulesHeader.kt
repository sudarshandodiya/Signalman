package net.dodiya.signalman.ui.rulelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo

@Composable
fun AutoRulesHeader(
    isAutoEnabled: Boolean,
    onAutoEnabledChange: (Boolean) -> Unit,
    globalDefault: String?,
    installedApps: List<AppInfo>,
    onSelectDefault: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAppPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.auto_enable_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.auto_enable_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Switch(checked = isAutoEnabled, onCheckedChange = onAutoEnabledChange)
        }

        if (!isAutoEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.fallback_action), style = MaterialTheme.typography.labelLarge)

            val selectedAppName =
                installedApps.find { it.packageName == globalDefault }?.name
                    ?: context.getString(R.string.system_chooser_none)

            OutlinedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { showAppPicker = true },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.default_app), style = MaterialTheme.typography.labelSmall)
                        Text(selectedAppName, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (globalDefault != null) {
                        IconButton(onClick = { onSelectDefault(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = context.getString(R.string.cd_clear_default))
                        }
                    }
                }
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            currentDefault = globalDefault,
            installedApps = installedApps,
            onDismiss = { showAppPicker = false },
            onSelect = {
                onSelectDefault(it)
                showAppPicker = false
            },
        )
    }
}
