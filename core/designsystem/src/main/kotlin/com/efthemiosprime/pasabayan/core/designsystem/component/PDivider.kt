package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder

@Composable
fun PDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = PasabayanBorder.width,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
