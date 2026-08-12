package net.dodiya.signalman.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import net.dodiya.signalman.data.MatchType

internal fun matchTypeIcon(type: MatchType): ImageVector =
    when (type) {
        MatchType.CONTAINS -> Icons.Default.Language
        MatchType.EQUALS -> Icons.Default.TextFields
        MatchType.STARTS_WITH -> Icons.AutoMirrored.Filled.KeyboardArrowRight
        MatchType.ENDS_WITH -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        MatchType.REGEX -> Icons.Default.Code
    }
