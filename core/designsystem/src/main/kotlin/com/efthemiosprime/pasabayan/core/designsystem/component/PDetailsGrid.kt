package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing

/**
 * Flow-based grid for detail displays. Each child takes equal weight
 * based on [columns]. Replaces iOS `DetailsGrid` in DeliveryCard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PDetailsGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        maxItemsInEachRow = columns,
    ) {
        content()
    }
}
