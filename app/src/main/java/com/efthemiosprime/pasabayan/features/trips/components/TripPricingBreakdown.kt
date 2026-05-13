package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Small breakdown visualization rendered under the headline price in `TripDetailsScreen`.
 * iOS parity (`TripDetailsView` pricing section): when the carrier exposed both a base price
 * and a calculated price (typically derived from distance × multiplier), this widget surfaces
 * those inputs so the shipper can see how the final price was built.
 *
 * The composable is fully reusable — pass already-formatted strings; no Trip dependency.
 * Returns nothing when there's no breakdown to show (host can call unconditionally; we no-op
 * via `Box`/no-emit when all three fields are missing).
 */
@Composable
fun TripPricingBreakdown(
    basePriceText: String?,
    distanceText: String?,
    multiplierText: String?,
    calculatedPriceText: String?,
    modifier: Modifier = Modifier,
) {
    val rows = listOfNotNull(
        basePriceText?.let { stringResource(R.string.trips_detail_pricing_base_label) to it },
        distanceText?.let { stringResource(R.string.trips_detail_pricing_distance_label) to it },
        multiplierText?.let { stringResource(R.string.trips_detail_pricing_multiplier_label) to it },
        calculatedPriceText?.let { stringResource(R.string.trips_detail_pricing_calculated_label) to it },
    )
    if (rows.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(PasabayanRadius.card),
            )
            .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        rows.forEach { (label, value) -> BreakdownRow(label, value) }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = PasabayanTextStyles.Body.small.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, name = "TripPricingBreakdown — light")
@Preview(showBackground = true, name = "TripPricingBreakdown — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripPricingBreakdownPreview() {
    PasabayanTheme {
        TripPricingBreakdown(
            basePriceText = "$50.00",
            distanceText = "541 km",
            multiplierText = "1.25×",
            calculatedPriceText = "$62.50",
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "TripPricingBreakdown — partial, light")
@Composable
private fun TripPricingBreakdownPartialPreview() {
    PasabayanTheme {
        TripPricingBreakdown(
            basePriceText = "$50.00",
            distanceText = "541 km",
            multiplierText = null,
            calculatedPriceText = null,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
