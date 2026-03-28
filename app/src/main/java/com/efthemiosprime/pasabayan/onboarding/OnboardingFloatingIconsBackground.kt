package com.efthemiosprime.pasabayan.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Parity with iOS `FloatingIconsBackground` — decorative SF Symbols replaced with [ImageVector]s.
 */
@Composable
fun OnboardingFloatingIconsBackground(
    icons: List<ImageVector>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    iconOpacity: Float = 0.08f,
) {
    val transition = rememberInfiniteTransition(label = "onboarding_float")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float_y",
    )

    val positions = onboardingFloatingIconPositions

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        icons.take(positions.size).forEachIndexed { index, icon ->
            val p = positions[index]
            val dir = if (index % 2 == 0) 1f else -0.5f
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = iconOpacity),
                modifier = Modifier
                    .size(p.sizeDp.dp)
                    .graphicsLayer { rotationZ = p.rotationDeg }
                    .offset(
                        x = w * p.xFraction - p.sizeDp.dp / 2,
                        y = h * p.yFraction - p.sizeDp.dp / 2 + (offsetY * dir).dp,
                    ),
            )
        }
    }
}

private data class FloatingIconSlot(
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Float,
    val rotationDeg: Float,
)

private val onboardingFloatingIconPositions = listOf(
    FloatingIconSlot(0.1f, 0.15f, 28f, -15f),
    FloatingIconSlot(0.85f, 0.1f, 24f, 20f),
    FloatingIconSlot(0.15f, 0.75f, 22f, 10f),
    FloatingIconSlot(0.9f, 0.65f, 26f, -25f),
    FloatingIconSlot(0.5f, 0.05f, 20f, 30f),
    FloatingIconSlot(0.75f, 0.85f, 24f, -10f),
)
