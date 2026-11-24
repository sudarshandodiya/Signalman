package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponent
import net.dodiya.signalman.data.UrlComponentReplacement
import net.dodiya.signalman.data.UrlComponents
import net.dodiya.signalman.ui.components.ComponentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformationSection(
    isCleanUrl: Boolean,
    onCleanUrlChange: (Boolean) -> Unit,
    isTransformEnabled: Boolean,
    onEvent: (EditRuleEvent) -> Unit,
    replacePattern: String,
    replacement: String,
    exampleUrl: String,
    @Suppress("UNUSED_PARAMETER") previewTransformedUrl: String?,
    urlComponentReplacements: List<UrlComponentReplacement>,
    transformMode: TransformMode,
    modifier: Modifier = Modifier,
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
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.clean_urls_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(R.string.clean_urls_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isCleanUrl,
                        onCheckedChange = onCleanUrlChange,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.enable_transformation),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isTransformEnabled,
                        onCheckedChange = { onEvent(EditRuleEvent.TransformEnabledChanged(it)) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isSimple = transformMode is TransformMode.Simple
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = isSimple,
                onClick = { onEvent(EditRuleEvent.TransformModeChanged(TransformMode.Simple())) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text(stringResource(R.string.mode_simple)) },
            )
            SegmentedButton(
                selected = !isSimple,
                onClick = { onEvent(EditRuleEvent.TransformModeChanged(TransformMode.Advanced())) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text(stringResource(R.string.mode_regex)) },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (transformMode is TransformMode.Simple) {
            val allComponents =
                listOf(
                    UrlComponent.SCHEME to (parsedComponents?.scheme ?: ""),
                    UrlComponent.HOST to (parsedComponents?.host ?: ""),
                    UrlComponent.DOMAIN to (parsedComponents?.domain ?: ""),
                    UrlComponent.PORT to (parsedComponents?.port ?: ""),
                    UrlComponent.PATH to (parsedComponents?.path ?: ""),
                    UrlComponent.QUERY to (parsedComponents?.query ?: ""),
                    UrlComponent.FRAGMENT to (parsedComponents?.fragment ?: ""),
                    UrlComponent.USER_INFO to (parsedComponents?.userInfo ?: ""),
                )

            val visibleComponents = allComponents.filter { it.second.isNotEmpty() }

            visibleComponents.forEach { (component, originalValue) ->
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

            if (visibleComponents.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_components_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            OutlinedTextField(
                value = replacePattern,
                onValueChange = { onEvent(EditRuleEvent.ReplacePatternChanged(it)) },
                label = { Text(stringResource(R.string.match_pattern_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = replacement,
                onValueChange = { onEvent(EditRuleEvent.ReplacementChanged(it)) },
                label = { Text(stringResource(R.string.substitution_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
