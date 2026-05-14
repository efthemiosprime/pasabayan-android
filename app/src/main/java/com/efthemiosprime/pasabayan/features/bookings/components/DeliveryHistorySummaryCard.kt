package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Header summary on the carrier delivery-history screen — total delivered
 * count + lifetime earned. iOS parity: `summaryCard` inside
 * `Features/Bookings/Views/History/DeliveryHistoryView.swift` (lines 72–113).
 *
 * Mirrors the shipper-side `PackageHistorySummaryCard` shape, but the
 * earnings column uses `tertiary` (green-leaning in the Pasabayan palette)
 * to echo iOS' `.green` accent and signal money-in vs money-out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeliveryHistorySummaryCard(
    deliveredCount: Int,
    totalEarned: Double,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.bookings_history_summary),
                style = PasabayanTextStyles.Heading.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg)) {
                StatColumn(
                    value = deliveredCount.toString(),
                    label = stringResource(R.string.bookings_history_deliveries),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                StatColumn(
                    value = stringResource(R.string.bookings_history_total_value, totalEarned),
                    label = stringResource(R.string.bookings_history_total_earned),
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
    valueColor: Color,
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

@Preview(showBackground = true, name = "DeliverySummary — light")
@Preview(showBackground = true, name = "DeliverySummary — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeliveryHistorySummaryCardPreview() {
    PasabayanTheme {
        DeliveryHistorySummaryCard(deliveredCount = 24, totalEarned = 1850.75)
    }
}
