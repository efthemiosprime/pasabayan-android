package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.domain.model.Coordinates
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Interactive map for picking a single coordinate — store or delivery location.
 *
 * iOS parity: `Views/Components/CreateServiceRequest/InteractiveMapView.swift`
 * (UIViewRepresentable wrapping MKMapView). Tap the map to drop a pin;
 * [onLocationSelected] fires with the new coordinates so the screen can
 * reverse-geocode and back-fill the address field.
 *
 * Reusable by construction — no Hilt or ViewModel dependencies, state is
 * fully external. The preview uses a placeholder tile (no live map) so the
 * Compose preview window doesn't require Google Play Services.
 */
@Composable
fun PackageLocationMap(
    title: String,
    selected: Coordinates?,
    onLocationSelected: (Coordinates) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Initial map center when [selected] is null. Defaults to Montreal — the
     * iOS implementation uses the same value so the preview region matches.
     */
    fallbackCenter: Coordinates = DEFAULT_CENTER,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Body.large,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val cameraCenter = selected ?: fallbackCenter
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    LatLng(cameraCenter.latitude, cameraCenter.longitude),
                    DEFAULT_ZOOM,
                )
            }
            RecenterOnSelectionChange(cameraPositionState, selected)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(PasabayanRadius.md)),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        onLocationSelected(
                            Coordinates(latitude = latLng.latitude, longitude = latLng.longitude),
                        )
                    },
                ) {
                    selected?.let {
                        Marker(
                            state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = title,
                        )
                    }
                }
            }

            Text(
                text = selected?.let {
                    stringResource(R.string.packages_service_map_coords, it.latitude, it.longitude)
                } ?: stringResource(R.string.packages_service_map_hint),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecenterOnSelectionChange(
    state: CameraPositionState,
    selected: Coordinates?,
) {
    // When the parent screen back-fills coordinates from a geocoded address,
    // pan the camera to the new center. Tap-to-pin updates the camera too,
    // but Google Maps' click handler already animates a small recenter on
    // tap so we only animate on external changes.
    LaunchedEffect(selected) {
        if (selected != null) {
            state.position = CameraPosition.fromLatLngZoom(
                LatLng(selected.latitude, selected.longitude),
                state.position.zoom,
            )
        }
    }
}

private val DEFAULT_CENTER = Coordinates(latitude = 45.5017, longitude = -73.5673)
private const val DEFAULT_ZOOM: Float = 12f

@Preview(showBackground = true, name = "Map — light")
@Preview(showBackground = true, name = "Map — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageLocationMapPreview() {
    PasabayanTheme {
        // GoogleMap can't render in the IDE preview without a real GMaps host
        // — show the surrounding chrome with a placeholder tile instead.
        PackageLocationMapPreviewStub(
            title = "Store location",
            selected = Coordinates(45.5088, -73.5878),
        )
    }
}

@Preview(showBackground = true, name = "Map (no selection) — light")
@Composable
private fun PackageLocationMapEmptyPreview() {
    PasabayanTheme {
        PackageLocationMapPreviewStub(
            title = "Delivery location",
            selected = null,
        )
    }
}

@Composable
private fun PackageLocationMapPreviewStub(
    title: String,
    selected: Coordinates?,
) {
    PCard {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Body.large,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(PasabayanRadius.md))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = selected?.let { "${it.latitude}, ${it.longitude}" }
                    ?: "Tap the map to drop a pin",
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
