package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * Pickup → delivery route card used on booking detail surfaces.
 * Mirrors iOS `RouteInformationCard.swift`.
 *
 * Stateless: caller supplies the two location strings. Differs from
 * `PRouteSection` (single-line origin → destination) by stacking the rows
 * vertically with a dotted connector — better readable for full addresses.
 */
@Composable
fun RouteInformationCard(
    pickupLocation: String,
    deliveryLocation: String,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = PasabayanColors.Success,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.bookings_route_card_title),
                    style = PasabayanTextStyles.Heading.h6,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            LocationRow(
                title = stringResource(R.string.bookings_route_card_pickup),
                address = pickupLocation,
            )
            DottedConnector()
            LocationRow(
                title = stringResource(R.string.bookings_route_card_delivery),
                address = deliveryLocation,
            )
        }
    }
}

@Composable
private fun LocationRow(title: String, address: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = address,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DottedConnector() {
    Box(
        modifier = Modifier
            .width(20.dp)
            .padding(start = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.height(20.dp),
        ) {
            repeat(3) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = CircleShape,
                    modifier = Modifier.size(4.dp),
                    content = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "RouteInformationCard — light")
@Preview(showBackground = true, name = "RouteInformationCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RouteInformationCardPreview() {
    PasabayanTheme {
        RouteInformationCard(
            pickupLocation = "123 Sample Street, Manila, Philippines",
            deliveryLocation = "456 Destination Avenue, Quezon City, Philippines",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
