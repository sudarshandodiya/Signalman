package net.dodiya.signalman.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.ui.theme.rememberExpressiveEffectsSpec

@Composable
fun LivePreviewHeader(
    exampleUrl: String,
    isMatch: Boolean,
    transformedUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colorSpec = rememberExpressiveEffectsSpec<Color>()
    val containerColor by animateColorAsState(
        targetValue =
            if (isMatch) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        animationSpec = colorSpec,
        label = "preview_container_color",
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (isMatch) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
        animationSpec = colorSpec,
        label = "preview_content_color",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = context.getString(R.string.live_preview_title),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text =
                        if (isMatch) {
                            context.getString(R.string.live_preview_matches)
                        } else {
                            context.getString(R.string.live_preview_no_match)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = context.getString(R.string.live_preview_original, exampleUrl),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
            if (transformedUrl != null && transformedUrl != exampleUrl) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = context.getString(R.string.live_preview_result, transformedUrl),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
            }
        }
    }
}
