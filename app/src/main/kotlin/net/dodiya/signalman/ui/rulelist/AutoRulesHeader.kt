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
import androidx.compose.material3.Surface
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

/**
 * Auto-rule preferences. The section header and outer container are supplied by
 * [net.dodiya.signalman.ui.settings.SettingsSection] so every settings group looks the same.
 */
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

    Column(modifier = modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.auto_enable_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.auto_enable_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = isAutoEnabled, onCheckedChange = onAutoEnabledChange)
        }

        if (!isAutoEnabled) {
            Spacer(modifier = Modifier.height(20.dp))
            val selectedAppName =
                installedApps.find { it.packageName == globalDefault }?.name
                    ?: context.getString(R.string.system_chooser_none)

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { showAppPicker = true },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.default_app),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = selectedAppName,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (globalDefault != null) {
                        IconButton(onClick = { onSelectDefault(null) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = context.getString(R.string.cd_clear_default),
                            )
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
