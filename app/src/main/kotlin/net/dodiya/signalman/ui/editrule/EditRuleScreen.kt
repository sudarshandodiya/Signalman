package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.ui.editrule.EditRuleEvent
import net.dodiya.signalman.ui.editrule.EditRuleUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuleScreen(
    uiState: EditRuleUiState,
    installedApps: List<AppInfo>,
    onEvent: (EditRuleEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val tabs =
        listOf(
            stringResource(R.string.tab_conditions),
            stringResource(R.string.tab_transformation),
            stringResource(R.string.tab_target_app),
        )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.name.isNotBlank()) {
                            uiState.name
                        } else {
                            context.getString(R.string.title_edit_rule)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardBackspace,
                            contentDescription = context.getString(R.string.action_back),
                        )
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
                        Text(stringResource(R.string.action_save))
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

                if (uiState.exampleUrl.isNotBlank()) {
                    net.dodiya.signalman.ui.components.LivePreviewHeader(
                        exampleUrl = uiState.exampleUrl,
                        isMatch = uiState.isPreviewMatch,
                        transformedUrl = uiState.previewTransformedUrl,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
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
                                installedApps = installedApps,
                            )
                    }
                }
            }
        }
    }
}
