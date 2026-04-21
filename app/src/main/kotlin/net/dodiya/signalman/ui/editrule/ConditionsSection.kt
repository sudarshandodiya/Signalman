package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.ui.editrule.EditRuleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionsSection(
    filters: List<Filter>,
    onEvent: (EditRuleEvent) -> Unit,
    logicalOperator: LogicalOperator,
    individualMatches: List<Boolean?>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        if (filters.size > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text(stringResource(R.string.operator_label), style = MaterialTheme.typography.bodyMedium)
                FilterChip(
                    selected = logicalOperator == LogicalOperator.AND,
                    onClick = { onEvent(EditRuleEvent.LogicalOperatorChanged(LogicalOperator.AND)) },
                    label = { Text(stringResource(R.string.operator_and)) },
                )
                FilterChip(
                    selected = logicalOperator == LogicalOperator.OR,
                    onClick = { onEvent(EditRuleEvent.LogicalOperatorChanged(LogicalOperator.OR)) },
                    label = { Text(stringResource(R.string.operator_or)) },
                )
            }
        }

        filters.forEachIndexed { index, filter ->
            val filterPasses = individualMatches.getOrNull(index)
            var expanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = filter.pattern,
                    onValueChange = { onEvent(EditRuleEvent.FilterChanged(index, filter.copy(pattern = it))) },
                    placeholder = { Text(stringResource(R.string.pattern_placeholder), style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        androidx.compose.foundation.layout.Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = getMatchTypeIcon(filter.matchType),
                                    contentDescription = filter.matchType.userFriendlyName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            androidx.compose.material3.DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                MatchType.entries.forEach { type ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(type.userFriendlyName) },
                                        leadingIcon = {
                                            Icon(
                                                getMatchTypeIcon(type),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        },
                                        onClick = {
                                            onEvent(EditRuleEvent.FilterChanged(index, filter.copy(matchType = type)))
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }
                    },
                    trailingIcon = {
                        if (filterPasses != null) {
                            Icon(
                                if (filterPasses) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (filterPasses) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                )

                if (filters.size > 1) {
                    IconButton(onClick = { onEvent(EditRuleEvent.FilterRemoved(index)) }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = context.getString(R.string.cd_remove_condition),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = { onEvent(EditRuleEvent.FilterAdded(Filter("", MatchType.CONTAINS))) },
            modifier = Modifier.align(Alignment.End),
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.add_condition))
        }
    }
}

private fun getMatchTypeIcon(type: MatchType): ImageVector =
    when (type) {
        MatchType.CONTAINS -> Icons.Default.Language
        MatchType.EQUALS -> Icons.Default.TextFields
        MatchType.STARTS_WITH -> Icons.AutoMirrored.Filled.KeyboardArrowRight
        MatchType.ENDS_WITH -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        MatchType.REGEX -> Icons.Default.Code
    }
