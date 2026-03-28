package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanLayout

@Composable
fun PCircularProgress(
    modifier: Modifier = Modifier,
    size: Dp = PasabayanLayout.iconSizeMedium,
    strokeWidth: Dp = 2.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = strokeWidth,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
