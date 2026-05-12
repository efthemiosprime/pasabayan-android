package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Marks composition that runs inside a [PScaffold] content slot. [PTopBar] reads this
 * to drop its status-bar inset when nested under another scaffold's top bar — preventing
 * the duplicate-inset chamber that appears when one scaffold's content hosts another.
 */
val LocalPasabayanInsidePScaffold = compositionLocalOf { false }

/**
 * App scaffold with Pasabayan [PSnackbarHost]. Apply [PasabayanSpacing.screenPadding] inside [content] when needed.
 */
@Composable
fun PScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        snackbarHost = { PSnackbarHost(snackbarHostState) },
        content = { padding ->
            CompositionLocalProvider(LocalPasabayanInsidePScaffold provides true) {
                content(padding)
            }
        },
    )
}
