package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing

enum class PCardVariant {
    Primary,
    Secondary,
    Compact,
    Large,
}

/**
 * Flat card with 1 dp border — parity with iOS `DesignSystem.Card` (no shadow).
 */
@Composable
fun PCard(
    modifier: Modifier = Modifier,
    variant: PCardVariant = PCardVariant.Primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    val radius = when (variant) {
        PCardVariant.Primary -> PasabayanRadius.card
        PCardVariant.Secondary -> PasabayanRadius.sm
        PCardVariant.Compact -> PasabayanRadius.xs
        PCardVariant.Large -> PasabayanRadius.lg
    }
    val shape = RoundedCornerShape(radius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(PasabayanBorder.width, PasabayanColors.Border),
    ) {
        Column(
            Modifier.padding(PasabayanSpacing.cardPadding),
            content = content,
        )
    }
}
