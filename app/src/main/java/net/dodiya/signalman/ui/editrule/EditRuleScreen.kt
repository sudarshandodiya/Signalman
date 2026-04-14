package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.ui.editrule.EditRuleEvent
import net.dodiya.signalman.ui.editrule.EditRuleUiState

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
                        Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                                installedApps = uiState.installedApps,
                            )
                    }
                }
            }
        }
    }
}