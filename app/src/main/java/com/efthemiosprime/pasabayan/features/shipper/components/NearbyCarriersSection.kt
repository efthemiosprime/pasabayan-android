package com.efthemiosprime.pasabayan.features.shipper.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarrier

/**
 * Horizontal "Top Carriers" chip row for shipper-explore. Parity with
 * iOS `TopCarriersSectionView` inside `ShipperHomeContent.swift`.
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
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            for (carrier in carriers) {
                TopCarrierChip(carrier = carrier, onClick = { onCarrierTap(carrier) })
            }
        }
    }
}

@Composable
private fun TopCarrierChip(
    carrier: NearbyCarrier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(CHIP_WIDTH)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(PasabayanRadius.card),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(PasabayanBorder.width, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CarrierAvatar(carrier = carrier)
            Text(
                text = carrier.name,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = null,
                    tint = PasabayanColors.BadgeBlue,
                    modifier = Modifier.size(PasabayanSpacing.md),
                )
                Text(
                    text = stringResource(
                        R.string.dashboard_shipper_top_carriers_deliveries,
                        carrier.completedDeliveries,
                    ),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            carrier.distanceKm?.let { distance ->
                Text(
                    text = stringResource(
                        R.string.dashboard_shipper_top_carriers_distance,
                        distance,
                    ),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CarrierAvatar(carrier: NearbyCarrier) {
    Box(
        modifier = Modifier
            .size(AVATAR_SIZE)
            .background(PasabayanColors.BadgeBlueLight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Avatar URL rendering is out of scope here — the existing codebase
        // hasn't standardized an image loader yet. iOS uses the carrier's
        // avatar URL; Android falls back to an outlined person glyph until
        // image-loading lands as a cross-cutting effort.
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = PasabayanColors.BadgeBlue,
            modifier = Modifier.size(AVATAR_GLYPH_SIZE),
        )
    }
}

// Structural width — not part of the spacing scale; tokens jump from xxxxxl (48) to layout-specific values.
private val CHIP_WIDTH = 140.dp
private val AVATAR_SIZE = PasabayanSpacing.xxxxl   // 40 dp — matches existing avatar tokens
private val AVATAR_GLYPH_SIZE = PasabayanSpacing.xxl // 24 dp

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
