package net.dodiya.signalman.ui.editrule

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.ui.components.LivePreviewHeader
import net.dodiya.signalman.ui.theme.rememberExpressiveEffectsSpec

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
    val tabs =
        listOf(
            stringResource(R.string.tab_conditions),
            stringResource(R.string.tab_transformation),
            stringResource(R.string.tab_target_app),
        )
    val tabTransitionSpec = rememberExpressiveEffectsSpec<Float>()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.name.ifBlank { stringResource(R.string.title_edit_rule) },
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
                actions = {
                    Button(
                        onClick = {
                            onEvent(EditRuleEvent.SaveRule)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(end = 12.dp),
                        enabled = uiState.isSaveEnabled,
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
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
                    LivePreviewHeader(
                        exampleUrl = uiState.exampleUrl,
                        isMatch = uiState.isPreviewMatch,
                        transformedUrl = uiState.previewTransformedUrl,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(
                        targetState = selectedTabIndex,
                        animationSpec = tabTransitionSpec,
                        label = "edit_rule_tab_content",
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 ->
                                ConditionsSection(
                                    name = uiState.name,
                                    onNameChange = { onEvent(EditRuleEvent.NameChanged(it)) },
                                    filters = uiState.filters,
                                    onEvent = onEvent,
                                    logicalOperator = uiState.logicalOperator,
                                    individualMatches = uiState.individualFilterMatches,
                                )
                            1 ->
                                TransformationSection(
                                    isCleanUrl = uiState.isCleanUrl,
                                    onCleanUrlChange = { onEvent(EditRuleEvent.CleanUrlChanged(it)) },
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
}
