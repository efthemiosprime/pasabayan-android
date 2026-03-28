package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/** Mirrors [14-design-system.md] `DesignSystem.Animation` — use with `animate*AsState`, `Crossfade`, etc. */
object PasabayanMotion {
    const val FAST_MS = 200
    const val MEDIUM_MS = 300
    const val SLOW_MS = 500

    fun <T> springDefault() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun <T> springBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
