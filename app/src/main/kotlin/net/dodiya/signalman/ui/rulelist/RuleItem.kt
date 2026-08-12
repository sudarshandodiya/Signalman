package net.dodiya.signalman.ui.rulelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.ui.components.matchTypeIcon

@Composable
fun RuleItem(
    rule: Rule,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filter = rule.filters.firstOrNull()

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable { onEdit() },
    ) {
        Row(
            modifier =
                Modifier
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .alpha(if (rule.isEnabled) 1f else 0.5f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (filter != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = matchTypeIcon(filter.matchType),
                            contentDescription = filter.matchType.userFriendlyName,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                val badges =
                    buildList {
                        if (hasTransform(rule)) add(context.getString(R.string.badge_transform))
                        if (rule.isCleanUrl) add(context.getString(R.string.badge_clean))
                    }
                if (badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        badges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = filter?.pattern ?: context.getString(R.string.no_pattern),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = "→ ${rule.targetPackage ?: context.getString(R.string.system_chooser)}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onEnabledChange,
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = context.getString(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun hasTransform(rule: Rule): Boolean =
    when (val mode = rule.transformMode) {
        is TransformMode.Simple -> mode.replacePattern.isNotEmpty() || mode.replacement.isNotEmpty()
        is TransformMode.Advanced -> mode.urlComponentReplacements.any { it.isEnabled && it.replacement.isNotEmpty() }
    }
