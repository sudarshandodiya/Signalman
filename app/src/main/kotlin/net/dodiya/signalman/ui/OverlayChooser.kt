package net.dodiya.signalman.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.R
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.ui.components.ExpressiveSectionHeader
import net.dodiya.signalman.ui.overlay.AppRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayChooser(
    uri: Uri,
    matchedRules: List<Rule>,
    browserActivities: List<ResolveInfo>,
    onRuleSelection: (Rule) -> Unit,
    onAppSelection: (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.title_signalman),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = uri.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip =
                                ClipData.newPlainText(
                                    context.getString(R.string.url_clipboard_label),
                                    uri.toString(),
                                )
                            clipboard.setPrimaryClip(clip)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.action_copy),
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareIntent =
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, uri.toString())
                                }
                            context.startActivity(
                                Intent.createChooser(shareIntent, context.getString(R.string.share_url_title)),
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_share),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp),
            ) {
                if (matchedRules.isNotEmpty()) {
                    item {
                        ExpressiveSectionHeader(
                            title = stringResource(R.string.suggested_rules),
                            icon = Icons.Default.AutoAwesome,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(matchedRules) { rule ->
                        AppRow(
                            name = rule.name,
                            pkg = rule.targetPackage ?: stringResource(R.string.system),
                            isSelected = false,
                            isSuggestion = true,
                            onClick = { onRuleSelection(rule) },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    ExpressiveSectionHeader(title = stringResource(R.string.apps_can_open))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(browserActivities) { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    AppRow(
                        name = resolveInfo.loadLabel(pm).toString(),
                        pkg = pkg,
                        isSelected = false,
                        icon = resolveInfo.loadIcon(pm),
                        onClick = {
                            val intent =
                                Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage(pkg)
                                }
                            context.startActivity(intent)
                            onDismiss()
                        },
                        onLongClick = { onAppSelection(pkg, 2) },
                    )
                }

                if (browserActivities.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = stringResource(R.string.no_apps_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.overlay_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
