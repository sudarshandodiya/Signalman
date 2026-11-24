@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package net.dodiya.signalman.ui.rulelist

import android.app.Activity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.ui.ImportExportState
import net.dodiya.signalman.ui.RuleViewModel
import net.dodiya.signalman.ui.SettingsContent
import net.dodiya.signalman.ui.SettingsViewModel
import net.dodiya.signalman.ui.components.ExpressiveEmptyState
import net.dodiya.signalman.ui.settings.DefaultBrowserStatusBanner
import net.dodiya.signalman.ui.settings.SettingsSection
import androidx.compose.material.icons.automirrored.filled.List as FilledListIcon
import androidx.compose.material.icons.automirrored.outlined.List as OutlinedListIcon
import androidx.compose.material.icons.filled.Settings as FilledSettingsIcon
import androidx.compose.material.icons.outlined.Settings as OutlinedSettingsIcon

private data class RuleListDestination(
    val label: String,
    val icon: ImageVector,
    val unselectedIcon: ImageVector,
)

private enum class RuleFilter {
    ALL,
    CUSTOM,
    AUTO,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    viewModel: RuleViewModel,
    settingsViewModel: SettingsViewModel,
    onAddRule: (String, String) -> Unit,
    onEditRule: (Int) -> Unit,
    onNavigateToImportExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rules by viewModel.allRules.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val globalDefault by viewModel.globalDefaultPackage.collectAsStateWithLifecycle()
    val isAutoEnabled by viewModel.isAutoRuleGenerationEnabled.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf(RuleFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var rulePendingDelete by remember { mutableStateOf<Rule?>(null) }

    val visibleRules =
        remember(rules, selectedFilter, searchQuery) {
            val query = searchQuery.trim()
            rules.filter { rule ->
                val matchesFilter =
                    when (selectedFilter) {
                        RuleFilter.ALL -> true
                        RuleFilter.CUSTOM -> !rule.isAutoGenerated
                        RuleFilter.AUTO -> rule.isAutoGenerated
                    }
                val searchableValues =
                    buildList {
                        add(rule.name)
                        add(rule.description)
                        add(rule.targetPackage.orEmpty())
                        addAll(rule.filters.map { it.pattern })
                        addAll(rule.filters.map { it.matchType.userFriendlyName })
                    }
                matchesFilter &&
                    (query.isBlank() || searchableValues.any { it.contains(query, ignoreCase = true) })
            }
        }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val importExportState by settingsViewModel.importExportState.collectAsStateWithLifecycle()
    val activity = context as? Activity
    val useNavigationRail =
        activity != null && calculateWindowSizeClass(activity).widthSizeClass != WindowWidthSizeClass.Compact

    val destinations =
        listOf(
            RuleListDestination(
                context.getString(R.string.nav_rules),
                Icons.AutoMirrored.Filled.FilledListIcon,
                Icons.AutoMirrored.Outlined.OutlinedListIcon,
            ),
            RuleListDestination(
                context.getString(R.string.nav_settings),
                Icons.Filled.FilledSettingsIcon,
                Icons.Outlined.OutlinedSettingsIcon,
            ),
        )

    LaunchedEffect(importExportState) {
        when (val state = importExportState) {
            is ImportExportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                settingsViewModel.resetImportExportState()
            }
            is ImportExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                settingsViewModel.resetImportExportState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = destinations.getOrNull(selectedTab)?.label ?: stringResource(R.string.nav_rules),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!useNavigationRail) {
                RuleListNavigationBar(
                    destinations = destinations,
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                )
            }
        },
        floatingActionButton = {
            if (!useNavigationRail && selectedTab == 0) {
                AddRuleFab(onClick = { showAddRuleDialog = true })
            }
        },
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (useNavigationRail) {
                RuleListNavigationRail(
                    destinations = destinations,
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    showAddRule = selectedTab == 0,
                    onAddRule = { showAddRuleDialog = true },
                )
            }

            when (selectedTab) {
                0 ->
                    RulesContent(
                        modifier = Modifier.weight(1f),
                        rules = rules,
                        visibleRules = visibleRules,
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onClearFilters = {
                            selectedFilter = RuleFilter.ALL
                            searchQuery = ""
                        },
                        onEditRule = onEditRule,
                        onDeleteRule = { rulePendingDelete = it },
                        onEnabledChange = { rule, enabled -> viewModel.update(rule.copy(isEnabled = enabled)) },
                    )
                1 ->
                    SettingsTabContent(
                        modifier = Modifier.weight(1f),
                        isAutoEnabled = isAutoEnabled,
                        globalDefault = globalDefault,
                        installedApps = installedApps,
                        onAutoEnabledChange = { viewModel.setAutoRuleGenerationEnabled(it) },
                        onSelectDefault = { viewModel.setGlobalDefault(it) },
                        settingsViewModel = settingsViewModel,
                        onNavigateToImportExport = onNavigateToImportExport,
                    )
            }
        }

        if (showAddRuleDialog) {
            AddRuleDialog(
                onDismiss = { showAddRuleDialog = false },
                onConfirm = { name, url ->
                    showAddRuleDialog = false
                    onAddRule(name, url)
                },
            )
        }
    }

    rulePendingDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { rulePendingDelete = null },
            title = { Text(stringResource(R.string.delete_rule_title)) },
            text = { Text(stringResource(R.string.delete_rule_message, rule.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(rule)
                        rulePendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { rulePendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun AddRuleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag("add_rule_fab"),
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
        },
        text = { Text(stringResource(R.string.action_new_rule)) },
    )
}

@Composable
private fun RuleListNavigationBar(
    destinations: List<RuleListDestination>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        destinations.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onSelectTab(index) },
                icon = {
                    Icon(
                        imageVector =
                            if (selectedTab == index) destination.icon else destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@Composable
private fun RuleListNavigationRail(
    destinations: List<RuleListDestination>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    showAddRule: Boolean,
    onAddRule: () -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = {
            if (showAddRule) {
                AddRuleFab(onClick = onAddRule)
            }
        },
    ) {
        destinations.forEachIndexed { index, destination ->
            NavigationRailItem(
                selected = selectedTab == index,
                onClick = { onSelectTab(index) },
                icon = {
                    Icon(
                        imageVector =
                            if (selectedTab == index) destination.icon else destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RulesContent(
    rules: List<Rule>,
    visibleRules: List<Rule>,
    selectedFilter: RuleFilter,
    onFilterChange: (RuleFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onEditRule: (Int) -> Unit,
    onDeleteRule: (Rule) -> Unit,
    onEnabledChange: (Rule, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterLabels =
        mapOf(
            RuleFilter.ALL to stringResource(R.string.filter_all),
            RuleFilter.CUSTOM to stringResource(R.string.filter_custom),
            RuleFilter.AUTO to stringResource(R.string.filter_auto),
        )

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_rules)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RuleFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filterLabels.getValue(filter)) },
                )
            }
        }

        when {
            rules.isEmpty() ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpressiveEmptyState(
                        icon = Icons.AutoMirrored.Filled.FilledListIcon,
                        title = stringResource(R.string.empty_rules_title),
                        message = stringResource(R.string.empty_rules_subtitle),
                    )
                }
            visibleRules.isEmpty() ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpressiveEmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.empty_no_results_title),
                        message = stringResource(R.string.empty_no_results_subtitle),
                        actionLabel = stringResource(R.string.action_clear_filters),
                        onAction = onClearFilters,
                    )
                }
            else ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(visibleRules, key = { it.id }) { rule ->
                        RuleItem(
                            rule = rule,
                            onDelete = { onDeleteRule(rule) },
                            onEdit = { onEditRule(rule.id) },
                            onEnabledChange = { enabled -> onEnabledChange(rule, enabled) },
                        )
                    }
                }
        }
    }
}

@Composable
private fun SettingsTabContent(
    isAutoEnabled: Boolean,
    globalDefault: String?,
    installedApps: List<AppInfo>,
    onAutoEnabledChange: (Boolean) -> Unit,
    onSelectDefault: (String?) -> Unit,
    settingsViewModel: SettingsViewModel,
    onNavigateToImportExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DefaultBrowserStatusBanner()
        SettingsSection(title = stringResource(R.string.title_auto_rules)) {
            AutoRulesHeader(
                isAutoEnabled = isAutoEnabled,
                onAutoEnabledChange = onAutoEnabledChange,
                globalDefault = globalDefault,
                installedApps = installedApps,
                onSelectDefault = onSelectDefault,
            )
        }
        SettingsContent(
            modifier = Modifier.fillMaxWidth().weight(1f),
            onNavigateToImportExport = onNavigateToImportExport,
            viewModel = settingsViewModel,
        )
    }
}
