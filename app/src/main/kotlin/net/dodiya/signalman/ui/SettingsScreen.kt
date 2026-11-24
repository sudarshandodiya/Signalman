package net.dodiya.signalman.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.dodiya.signalman.R
import net.dodiya.signalman.ui.settings.SettingsClickableItem
import net.dodiya.signalman.ui.settings.SettingsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()
    val pendingImportRules by viewModel.pendingImportRules.collectAsStateWithLifecycle()

    LaunchedEffect(pendingImportRules) {
        if (pendingImportRules != null) {
            onNavigateToImportExport()
        }
    }

    LaunchedEffect(importExportState) {
        when (val state = importExportState) {
            is ImportExportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetImportExportState()
            }
            is ImportExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetImportExportState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        SettingsContent(
            modifier = Modifier.padding(paddingValues),
            onNavigateToImportExport = onNavigateToImportExport,
            viewModel = viewModel,
        )
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onNavigateToImportExport: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()

    val allBrowsers =
        remember {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
            pm
                .queryIntentActivities(browserIntent, PackageManager.MATCH_ALL)
                .filter { it.activityInfo.packageName != context.packageName }
                .distinctBy { it.activityInfo.packageName }
                .sortedBy { it.loadLabel(pm).toString() }
        }

    val hiddenBrowsers by viewModel.hiddenBrowsers.collectAsStateWithLifecycle()
    val isCleanUrlsEnabled by viewModel.isCleanUrlsEnabled.collectAsStateWithLifecycle()
    var showBrowserDialog by remember { mutableStateOf(false) }
    val versionName =
        remember(context) {
            try {
                context.packageManager
                    .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                    .versionName
                    .orEmpty()
            } catch (_: PackageManager.NameNotFoundException) {
                ""
            }
        }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            uri?.let { viewModel.exportRules(it) }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { viewModel.loadImportPreview(it) }
        }

    val pendingImportRules by viewModel.pendingImportRules.collectAsStateWithLifecycle()

    LaunchedEffect(pendingImportRules) {
        if (pendingImportRules != null) {
            onNavigateToImportExport()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            SettingsSection(title = stringResource(R.string.section_general)) {
                SettingsClickableItem(
                    title = stringResource(R.string.clean_urls_title),
                    subtitle = stringResource(R.string.clean_urls_subtitle),
                    icon = Icons.Default.CleaningServices,
                    onClick = { viewModel.setCleanUrlsEnabled(!isCleanUrlsEnabled) },
                    isLastItem = true,
                    trailing = {
                        Switch(
                            checked = isCleanUrlsEnabled,
                            onCheckedChange = { viewModel.setCleanUrlsEnabled(it) },
                        )
                    },
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.section_browsers)) {
                SettingsClickableItem(
                    title = stringResource(R.string.browser_visibility_title),
                    subtitle = stringResource(R.string.browser_visibility_subtitle),
                    icon = Icons.Default.Language,
                    onClick = { showBrowserDialog = true },
                    isLastItem = true,
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.section_data)) {
                SettingsClickableItem(
                    title = stringResource(R.string.export_rules_title),
                    subtitle = stringResource(R.string.export_rules_subtitle),
                    icon = Icons.Default.FileUpload,
                    onClick = { exportLauncher.launch("signalman_rules.json") },
                )
                SettingsClickableItem(
                    title = stringResource(R.string.import_rules_title),
                    subtitle = stringResource(R.string.import_rules_subtitle),
                    icon = Icons.Default.FileDownload,
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    isLastItem = true,
                )
            }
        }
        if (importExportState is ImportExportState.Loading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.about_footer, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 28.dp),
            )
        }
    }

    if (showBrowserDialog) {
        AlertDialog(
            onDismissRequest = { showBrowserDialog = false },
            title = { Text(stringResource(R.string.title_browser_visibility)) },
            text = {
                LazyColumn {
                    items(allBrowsers) { resolveInfo ->
                        val pkg = resolveInfo.activityInfo.packageName
                        val isVisible = !hiddenBrowsers.contains(pkg)
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newHidden =
                                            if (isVisible) {
                                                hiddenBrowsers + pkg
                                            } else {
                                                hiddenBrowsers - pkg
                                            }
                                        viewModel.setHiddenBrowsers(newHidden)
                                    }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isVisible,
                                onCheckedChange = { checked ->
                                    val newHidden =
                                        if (checked) {
                                            hiddenBrowsers - pkg
                                        } else {
                                            hiddenBrowsers + pkg
                                        }
                                    viewModel.setHiddenBrowsers(newHidden)
                                },
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = resolveInfo.loadLabel(pm).toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = pkg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBrowserDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            },
        )
    }
}
