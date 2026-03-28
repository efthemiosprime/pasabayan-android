package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius

/**
 * Themed snackbar for use inside [SnackbarHost] — inverse surface for contrast (Material baseline).
 */
@Composable
fun PSnackbar(snackbarData: SnackbarData) {
    val scheme = MaterialTheme.colorScheme
    Snackbar(
        snackbarData = snackbarData,
        shape = RoundedCornerShape(PasabayanRadius.sm),
        containerColor = scheme.inverseSurface,
        contentColor = scheme.inverseOnSurface,
        actionColor = scheme.inversePrimary,
    )
}

/**
 * Standard [SnackbarHost] wiring Pasabayan snackbar styling.
 */
@Composable
fun PSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data -> PSnackbar(data) },
    )
}
