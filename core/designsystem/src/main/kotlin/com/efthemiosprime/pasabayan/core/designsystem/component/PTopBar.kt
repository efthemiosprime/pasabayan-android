package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.LocalPasabayanNavigationTitleStyle

/**
 * Top app bar with **15 sp** navigation title from [LocalPasabayanNavigationTitleStyle].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
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
    )
}
