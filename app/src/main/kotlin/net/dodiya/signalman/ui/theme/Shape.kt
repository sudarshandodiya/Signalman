package net.dodiya.signalman.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * A deliberate shape vocabulary:
 * - small roles for chips and badges,
 * - medium/large roles for content groups,
 * - extra large for actions and high-emphasis surfaces.
 */
val ExpressiveShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )
