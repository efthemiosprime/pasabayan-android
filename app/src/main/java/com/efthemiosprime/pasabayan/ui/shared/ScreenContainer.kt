package com.efthemiosprime.pasabayan.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * ScreenContainer - Global screen background wrapper
 * Applies consistent background color (#faf8fe) to all screens
 * Use this wrapper for all screen composables to ensure consistency
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PasabayanDesignSystem.Colors.screenBackground)
    ) {
        content()
    }
} 