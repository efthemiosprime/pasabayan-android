package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.LocalPasabayanNavigationTitleStyle

/**
 * Top app bar with **15 sp** navigation title from [LocalPasabayanNavigationTitleStyle].
 *
 * When rendered inside another [PScaffold] content slot, [windowInsets] defaults to
 * [WindowInsets] zero so the status-bar inset isn't applied a second time. At the root
 * (e.g. as the outer scaffold's `topBar`), it defaults to [TopAppBarDefaults.windowInsets].
 * Pass an explicit value to override.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    windowInsets: WindowInsets = defaultPTopBarWindowInsets(),
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = LocalPasabayanNavigationTitleStyle.current,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun defaultPTopBarWindowInsets(): WindowInsets {
    val nested = LocalPasabayanInsidePScaffold.current
    val zero = remember { WindowInsets(0, 0, 0, 0) }
    return if (nested) zero else TopAppBarDefaults.windowInsets
}
