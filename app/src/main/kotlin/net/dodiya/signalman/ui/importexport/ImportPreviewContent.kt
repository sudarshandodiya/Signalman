package net.dodiya.signalman.ui.importexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.Rule

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

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.import_select_rules),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = context.getString(R.string.import_rules_found, rules.size),
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
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
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
        ) {
            itemsIndexed(rules) { index, rule ->
                val isSelected = selectedIndices.contains(index)

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    onClick = { onToggleSelection(index) },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection(index) },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            if (rule.description.isNotEmpty()) {
                                Text(
                                    text = rule.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            val targetLabel = rule.targetPackage ?: context.getString(R.string.system_chooser_none)
                            Text(
                                text = context.getString(R.string.import_filter_count, rule.filters.size, targetLabel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
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
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(context.getString(R.string.import_count, selectedIndices.size))
                }
            }
        }
    }
}
