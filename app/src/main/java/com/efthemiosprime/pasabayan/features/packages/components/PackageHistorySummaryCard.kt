package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * Header summary on the shipper package history screen — total delivered
 * count + lifetime spent. iOS parity: `summaryCard` private view inside
 * `PackageHistoryView`.
 *
 * Pure / state-hoisted — parent passes already-computed totals.
 */
@Composable
internal fun PackageHistorySummaryCard(
    deliveredCount: Int,
    totalSpent: Double,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.packages_history_summary),
                style = PasabayanTextStyles.Heading.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
            ) {
                StatColumn(
                    value = deliveredCount.toString(),
                    label = stringResource(R.string.packages_history_packages),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                StatColumn(
                    value = stringResource(R.string.packages_history_total_value, totalSpent),
                    label = stringResource(R.string.packages_history_total_spent),
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Text(
            text = value,
            style = PasabayanTextStyles.Heading.h3,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "HistorySummary — light")
@Preview(showBackground = true, name = "HistorySummary — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageHistorySummaryCardPreview() {
    PasabayanTheme {
        PackageHistorySummaryCard(deliveredCount = 12, totalSpent = 482.50)
    }
}
