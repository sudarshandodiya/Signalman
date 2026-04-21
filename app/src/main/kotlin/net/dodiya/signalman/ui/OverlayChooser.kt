package net.dodiya.signalman.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.ui.overlay.AppRow

@Suppress("UNUSED_PARAMETER")
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
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var selectedRule by remember { mutableStateOf<Rule?>(null) }
    val context = LocalContext.current
    val pm = context.packageManager

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Signalman",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uri.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("URL", uri.toString())
                        clipboard.setPrimaryClip(clip)
                    },
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                IconButton(
                    onClick = {
                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, uri.toString())
                            }
                        context.startActivity(Intent.createChooser(shareIntent, "Share URL"))
                    },
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp),
            ) {
                if (matchedRules.isNotEmpty()) {
                    item {
                        Text(
                            "Suggested Rules",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(matchedRules) { rule ->
                        AppRow(
                            name = rule.name,
                            pkg = rule.targetPackage ?: "System",
                            isSelected = selectedRule == rule,
                            onClick = {
                                selectedRule = rule
                                selectedPackage = null
                            },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Text("Apps that can open this link", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(browserActivities) { res ->
                    val pkg = res.activityInfo.packageName
                    AppRow(
                        name = res.loadLabel(pm).toString(),
                        pkg = pkg,
                        isSelected = selectedPackage == pkg,
                        onClick = {
                            selectedPackage = pkg
                            selectedRule = null
                        },
                    )
                }

                if (browserActivities.isEmpty()) {
                    item {
                        Text(
                            "No apps found that can open this link.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        val pkg = selectedRule?.targetPackage ?: selectedPackage
                        pkg?.let {
                            val intent =
                                Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage(it)
                                }
                            context.startActivity(intent)
                            onDismiss()
                        }
                    },
                    enabled = selectedPackage != null || selectedRule != null,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text("Once", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { selectedPackage?.let { onAppSelection(it, 2) } },
                    enabled = selectedPackage != null && selectedRule == null,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text("Always", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
