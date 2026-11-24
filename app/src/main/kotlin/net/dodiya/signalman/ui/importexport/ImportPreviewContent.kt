package net.dodiya.signalman.ui.importexport

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.ui.theme.rememberExpressiveEffectsSpec

@Composable
internal fun ImportPreviewContent(
    rules: List<Rule>,
    selectedIndices: Set<Int>,
    isLoading: Boolean,
    onImport: () -> Unit,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onToggleSelection: (Int) -> Unit,
) {
    val context = LocalContext.current
    val colorSpec = rememberExpressiveEffectsSpec<Color>()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.import_select_rules),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.import_rules_found, rules.size),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text(stringResource(R.string.action_select_all))
                    }
                    TextButton(onClick = onDeselectAll) {
                        Text(stringResource(R.string.action_deselect_all))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(rules) { index, rule ->
                val isSelected = selectedIndices.contains(index)
                val containerColor by animateColorAsState(
                    targetValue =
                        if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                    animationSpec = colorSpec,
                    label = "import_item_color",
                )
                val contentColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onToggleSelection(index) },
                    shape = MaterialTheme.shapes.large,
                    colors =
                        CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection(index) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (rule.description.isNotEmpty()) {
                                Text(
                                    text = rule.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            val targetLabel = rule.targetPackage ?: context.getString(R.string.system_chooser_none)
                            Text(
                                text = stringResource(R.string.import_filter_count, rule.filters.size, targetLabel),
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = onImport,
                modifier = Modifier.weight(1f),
                enabled = !isLoading && selectedIndices.isNotEmpty(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.import_count, selectedIndices.size))
                }
            }
        }
    }
}
