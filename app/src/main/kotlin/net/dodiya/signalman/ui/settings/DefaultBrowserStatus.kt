package net.dodiya.signalman.ui.settings

import android.app.role.RoleManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import net.dodiya.signalman.R

/** Compact status for the single most important setup step of a link-routing app. */
@Composable
fun DefaultBrowserStatusBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isDefaultBrowser by remember { mutableStateOf(false) }

    fun refreshStatus() {
        val roleManager = context.getSystemService(RoleManager::class.java)
        isDefaultBrowser = roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) == true
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshStatus()
    }

    val containerColor =
        if (isDefaultBrowser) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    val contentColor =
        if (isDefaultBrowser) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        shape = SettingsCardShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isDefaultBrowser) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.status_default_browser),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text =
                        stringResource(
                            if (isDefaultBrowser) {
                                R.string.status_default_browser_on
                            } else {
                                R.string.status_default_browser_off
                            },
                        ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        stringResource(
                            if (isDefaultBrowser) {
                                R.string.status_default_browser_on_hint
                            } else {
                                R.string.status_default_browser_off_hint
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!isDefaultBrowser) {
                Spacer(modifier = Modifier.width(12.dp))
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        val target =
                            if (intent.resolveActivity(context.packageManager) != null) {
                                intent
                            } else {
                                Intent(Settings.ACTION_SETTINGS)
                            }
                        context.startActivity(target)
                    },
                ) {
                    Text(stringResource(R.string.action_set_default))
                }
            }
        }
    }
}
