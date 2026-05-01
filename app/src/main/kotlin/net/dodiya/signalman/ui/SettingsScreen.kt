package net.dodiya.signalman.ui

import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.dodiya.signalman.R
import net.dodiya.signalman.ui.settings.SettingsClickableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val snackbarHostState = remember { SnackbarHostState() }
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()

    val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://"))
    val allBrowsers =
        remember {
            pm
                .queryIntentActivities(browserIntent, PackageManager.MATCH_ALL)
                .filter { it.activityInfo.packageName != context.packageName }
                .distinctBy { it.activityInfo.packageName }
                .sortedBy { it.loadLabel(pm).toString() }
        }

    val hiddenBrowsers by viewModel.hiddenBrowsers.collectAsStateWithLifecycle()
    var showBrowserDialog by remember { mutableStateOf(false) }

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
                title = { Text(context.getString(R.string.title_settings), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val settingsItems =
                listOf(
                    Triple(
                        context.getString(R.string.export_rules_title),
                        context.getString(R.string.export_rules_subtitle),
                        Icons.Default.FileUpload,
                    ) to { exportLauncher.launch("signalman_rules.json") },
                    Triple(
                        context.getString(R.string.import_rules_title),
                        context.getString(R.string.import_rules_subtitle),
                        Icons.Default.FileDownload,
                    ) to { importLauncher.launch(arrayOf("application/json")) },
                    Triple(
                        context.getString(R.string.browser_visibility_title),
                        context.getString(R.string.browser_visibility_subtitle),
                        Icons.Default.Language,
                    ) to { showBrowserDialog = true },
                )

            items(settingsItems.size) { index ->
                val (triple, onClick) = settingsItems[index]
                val (title, subtitle, icon) = triple
                SettingsClickableItem(
                    title = title,
                    subtitle = subtitle,
                    icon = icon,
                    onClick = onClick,
                    isLastItem = index == settingsItems.size - 1,
                )
            }

            if (importExportState is ImportExportState.Loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }

    if (showBrowserDialog) {
        AlertDialog(
            onDismissRequest = { showBrowserDialog = false },
            title = { Text(context.getString(R.string.title_browser_visibility)) },
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
                                    }.padding(vertical = 8.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = resolveInfo.loadLabel(pm).toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = pkg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBrowserDialog = false }) {
                    Text(context.getString(R.string.action_close))
                }
            },
        )
    }
}
