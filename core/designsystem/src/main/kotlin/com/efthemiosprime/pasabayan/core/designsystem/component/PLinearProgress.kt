package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius

/**
 * Themed linear progress bar. Pass [progress] for determinate mode,
 * or `null` for indeterminate.
 */
@Composable
fun PLinearProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val shape = RoundedCornerShape(PasabayanRadius.xs)
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier.clip(shape),
            color = indicatorColor,
            trackColor = trackColor,
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier.clip(shape),
            color = indicatorColor,
            trackColor = trackColor,
        )
    }
}
