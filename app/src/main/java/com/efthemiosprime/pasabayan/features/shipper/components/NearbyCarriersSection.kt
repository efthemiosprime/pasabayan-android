package com.efthemiosprime.pasabayan.features.shipper.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PChip
import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarrier

/**
 * Horizontal "Top Carriers" chip row for shipper-explore. Parity with
 * iOS `TopCarriersSectionView` inside `ShipperHomeContent.swift` — small
 * inline chips (name + completed deliveries) instead of a full card per
 * carrier so the row stays compact.
 *
 * Caller is responsible for filtering to carriers with completed
 * deliveries — `NearbyCarriersUiState.carriersWithCompletedDeliveries`
 * does this. The section deliberately renders nothing when the input
 * list is empty so the caller can skip an empty title.
 */
@Composable
fun NearbyCarriersSection(
    carriers: List<NearbyCarrier>,
    onCarrierTap: (NearbyCarrier) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (carriers.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(NEARBY_CARRIERS_SECTION_TEST_TAG),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.dashboard_shipper_top_carriers_title),
            style = PasabayanTextStyles.Heading.h6,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            for (carrier in carriers) {
                PChip(
                    label = carrier.name,
                    secondary = if (carrier.completedDeliveries > 0) {
                        stringResource(
                            R.string.dashboard_shipper_top_carriers_deliveries,
                            carrier.completedDeliveries,
                        )
                    } else {
                        null
                    },
                    onClick = { onCarrierTap(carrier) },
                )
            }
        }
    }
}

internal const val NEARBY_CARRIERS_SECTION_TEST_TAG = "Shipper.NearbyCarriersSection"

@Preview(showBackground = true, name = "NearbyCarriers — light")
@Preview(showBackground = true, name = "NearbyCarriers — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NearbyCarriersSectionPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            NearbyCarriersSection(
                carriers = listOf(
                    NearbyCarrier(id = 1, name = "Alex Carrier", avatar = null, completedDeliveries = 12, distanceKm = 4.2),
                    NearbyCarrier(id = 2, name = "Sam Driver", avatar = null, completedDeliveries = 7, distanceKm = 11.0),
                    NearbyCarrier(id = 3, name = "Jordan Pilot", avatar = null, completedDeliveries = 1, distanceKm = null),
                ),
                onCarrierTap = {},
            )
        }
    }
}
