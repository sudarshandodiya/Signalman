package net.dodiya.signalman.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val EFFECT_DURATION_MS = 250

/** True when the system requests that animations be removed. */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * App motion token for position and size changes. Material 3's expressive MotionScheme API is not
 * public in the library version pinned by this project, so these values are app-owned and use
 * Compose's named spring presets.
 */
@Composable
fun <T> rememberExpressiveSpatialSpec(): FiniteAnimationSpec<T> =
    if (rememberReducedMotion()) {
        snap()
    } else {
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    }

/** App motion token for color and opacity changes, with an immediate reduced-motion fallback. */
@Composable
fun <T> rememberExpressiveEffectsSpec(): FiniteAnimationSpec<T> =
    if (rememberReducedMotion()) {
        snap()
    } else {
        tween(durationMillis = EFFECT_DURATION_MS, easing = EaseOut)
    }
