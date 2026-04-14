package net.dodiya.signalman.ui.editrule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponent
import net.dodiya.signalman.data.UrlComponentReplacement
import net.dodiya.signalman.data.UrlComponents
import net.dodiya.signalman.ui.editrule.EditRuleEvent
import net.dodiya.signalman.ui.components.ComponentItem

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
                            .background(
                                if (isSimple) MaterialTheme.colorScheme.secondaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent,
                            )
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
                            .background(
                                if (!isSimple) MaterialTheme.colorScheme.secondaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent,
                            )
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