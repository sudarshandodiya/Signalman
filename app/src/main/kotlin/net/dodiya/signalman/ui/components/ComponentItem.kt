package net.dodiya.signalman.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import net.dodiya.signalman.R
import net.dodiya.signalman.ui.components.dialog.EditValueDialog

@Composable
fun ComponentItem(
    label: String,
    originalValue: String,
    replacementValue: String?,
    onReplacementChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = originalValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (replacementValue != null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (replacementValue != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = replacementValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        IconButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Edit, contentDescription = context.getString(R.string.cd_edit_component), modifier = Modifier.size(20.dp))
        }
    }

    if (showDialog) {
        EditValueDialog(
            label = label,
            initialValue = replacementValue ?: originalValue,
            hasReplacement = replacementValue != null,
            onDismiss = { showDialog = false },
            onApply = {
                onReplacementChange(it)
                showDialog = false
            },
            onReset = {
                onReplacementChange("")
                showDialog = false
            },
        )
    }
}
