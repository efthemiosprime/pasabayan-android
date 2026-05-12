package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles

/**
 * Tappable capsule chip for status/category filtering.
 * Parity with iOS filter chip patterns in trip/package browse.
 */
@Composable
fun PFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val shape = RoundedCornerShape(PasabayanRadius.capsule)
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (selected) {
        null
    } else {
        BorderStroke(PasabayanBorder.width, MaterialTheme.colorScheme.outline)
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PasabayanSpacing.md,
                vertical = PasabayanSpacing.sm,
            ),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = PasabayanTextStyles.Caption.large,
                color = contentColor,
            )
            if (count != null) {
                Text(
                    text = "($count)",
                    style = PasabayanTextStyles.Caption.regular,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}
