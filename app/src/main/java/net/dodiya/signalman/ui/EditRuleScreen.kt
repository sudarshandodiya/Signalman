package net.dodiya.signalman.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponent
import net.dodiya.signalman.data.UrlComponentReplacement
import net.dodiya.signalman.data.UrlComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuleScreen(
    uiState: EditRuleUiState,
    onEvent: (EditRuleEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Conditions", "Transformation", "Target App")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.name.isNotBlank()) uiState.name else "Edit Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onEvent(EditRuleEvent.SaveRule)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = uiState.isSaveEnabled,
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Header (only if example url exists)
                if (uiState.exampleUrl.isNotBlank()) {
                    LivePreviewHeader(
                        exampleUrl = uiState.exampleUrl,
                        isMatch = uiState.isPreviewMatch,
                        transformedUrl = uiState.previewTransformedUrl,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 ->
                            ConditionsSection(
                                filters = uiState.filters,
                                onEvent = onEvent,
                                logicalOperator = uiState.logicalOperator,
                                individualMatches = uiState.individualFilterMatches,
                            )
                        1 ->
                            TransformationSection(
                                isTransformEnabled = uiState.isTransformEnabled,
                                onEvent = onEvent,
                                replacePattern = uiState.replacePattern,
                                replacement = uiState.replacement,
                                exampleUrl = uiState.exampleUrl,
                                previewTransformedUrl = uiState.previewTransformedUrl,
                                urlComponentReplacements = uiState.urlComponentReplacements,
                                transformMode = uiState.transformMode,
                            )
                        2 ->
                            TargetAppSection(
                                targetPackage = uiState.targetPackage,
                                onEvent = onEvent,
                                installedApps = uiState.installedApps,
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun LivePreviewHeader(
    exampleUrl: String,
    isMatch: Boolean,
    transformedUrl: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Live Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMatch) "Matches" else "Does Not Match",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Original: $exampleUrl",
                style = MaterialTheme.typography.bodySmall,
            )
            if (transformedUrl != null && transformedUrl != exampleUrl) {
                Text(
                    text = "Result: $transformedUrl",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformationSection(
    isTransformEnabled: Boolean,
    onEvent: (EditRuleEvent) -> Unit,
    replacePattern: String,
    replacement: String,
    exampleUrl: String,
    previewTransformedUrl: String?,
    urlComponentReplacements: List<UrlComponentReplacement>,
    transformMode: TransformMode,
) {
    val parsedComponents =
        remember(exampleUrl) {
            if (exampleUrl.isNotBlank()) {
                UrlComponents.parse(exampleUrl)
            } else {
                null
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Enable Transformation",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = isTransformEnabled,
                onCheckedChange = { onEvent(EditRuleEvent.TransformEnabledChanged(it)) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simple / Regex Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                val isSimple = transformMode == TransformMode.SIMPLE
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSimple) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                            .clickable { onEvent(EditRuleEvent.TransformModeChanged(TransformMode.SIMPLE)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Simple",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSimple) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (!isSimple) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                            .clickable { onEvent(EditRuleEvent.TransformModeChanged(TransformMode.ADVANCED)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Regex",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isSimple) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (transformMode == TransformMode.SIMPLE) {
            val visibleComponents = listOf(UrlComponent.SCHEME, UrlComponent.HOST, UrlComponent.DOMAIN)
            visibleComponents.forEach { component ->
                val originalValue =
                    when (component) {
                        UrlComponent.SCHEME -> parsedComponents?.scheme ?: ""
                        UrlComponent.HOST -> parsedComponents?.host ?: ""
                        UrlComponent.DOMAIN -> parsedComponents?.domain ?: ""
                        else -> ""
                    }
                val replacementData = urlComponentReplacements.find { it.component == component }

                ComponentItem(
                    label = component.displayName,
                    originalValue = originalValue,
                    replacementValue = replacementData?.replacement,
                    onReplacementChange = { newValue ->
                        onEvent(
                            EditRuleEvent.UrlComponentReplacementChanged(
                                UrlComponentReplacement(
                                    component = component,
                                    replacement = newValue,
                                    isEnabled = newValue.isNotEmpty(),
                                ),
                            ),
                        )
                    },
                )
                HorizontalDivider()
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.height(40.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Component")
            }
        } else {
            OutlinedTextField(
                value = replacePattern,
                onValueChange = { onEvent(EditRuleEvent.ReplacePatternChanged(it)) },
                label = { Text("Match Pattern") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Hint Pattern Hint",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = replacement,
                onValueChange = { onEvent(EditRuleEvent.ReplacementChanged(it)) },
                label = { Text("Substitution") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Substitution Hint",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Composable
fun ComponentItem(
    label: String,
    originalValue: String,
    replacementValue: String?,
    onReplacementChange: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(replacementValue ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = originalValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (replacementValue != null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (replacementValue != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = replacementValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        IconButton(onClick = {
            editValue = replacementValue ?: originalValue
            showDialog = true
        }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit $label") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text("Replacement") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (editValue.isNotEmpty()) {
                            IconButton(onClick = { editValue = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onReplacementChange(editValue)
                    showDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
                if (replacementValue != null) {
                    TextButton(onClick = {
                        onReplacementChange("")
                        showDialog = false
                    }) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionsSection(
    filters: List<Filter>,
    onEvent: (EditRuleEvent) -> Unit,
    logicalOperator: LogicalOperator,
    individualMatches: List<Boolean?>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        if (filters.size > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Operator:", style = MaterialTheme.typography.bodyMedium)
                FilterChip(
                    selected = logicalOperator == LogicalOperator.AND,
                    onClick = { onEvent(EditRuleEvent.LogicalOperatorChanged(LogicalOperator.AND)) },
                    label = { Text("AND") },
                )
                FilterChip(
                    selected = logicalOperator == LogicalOperator.OR,
                    onClick = { onEvent(EditRuleEvent.LogicalOperatorChanged(LogicalOperator.OR)) },
                    label = { Text("OR") },
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
                    placeholder = { Text("Pattern", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = getMatchTypeIcon(filter.matchType),
                                    contentDescription = filter.matchType.userFriendlyName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                MatchType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.userFriendlyName) },
                                        leadingIcon = { Icon(getMatchTypeIcon(type), contentDescription = null, modifier = Modifier.size(18.dp)) },
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
                        Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        TextButton(
            onClick = { onEvent(EditRuleEvent.FilterAdded(Filter("", MatchType.CONTAINS))) },
            modifier = Modifier.align(Alignment.End),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Condition")
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

@Composable
fun TargetAppSection(
    targetPackage: String?,
    onEvent: (EditRuleEvent) -> Unit,
    installedApps: List<AppInfo>,
) {
    var searchQuery by remember { mutableStateOf("") }

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

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search App") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
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
                    name = "System Chooser (Default)",
                    packageName = "System will ask every time",
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
                )
            }
        }
    }
}

@Composable
fun AppListItem(
    name: String,
    packageName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        }
    }
}
