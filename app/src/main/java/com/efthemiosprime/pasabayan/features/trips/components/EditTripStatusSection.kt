package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.CarrierTripActionPolicy

/**
 * Editable status section for `EditTripSheet`. Mirrors iOS `EditTripSheet.swift:307-1043`
 * (status picker + activate confirmation). Currently the only transition exposed inside the
 * sheet is `PLANNING → ACTIVE` (via the activate endpoint, iOS-parity). `ACTIVE → IN_TRANSIT`
 * and cancellation transitions remain in `TripStatusUpdateSheet` / Slice B6.
 */
@Composable
fun EditTripStatusSection(
    status: TripStatus,
    onActivateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PDetailSectionTitle(text = stringResource(R.string.trips_edit_status_section))

        PStatusBadge(
            config = TripStatusBadgeConfig(
                status = status,
                label = stringResource(tripStatusLabelRes(status)),
            ),
        )

        if (CarrierTripActionPolicy.shouldOfferActivateTrip(status)) {
            Text(
                text = stringResource(R.string.trips_action_activate_trip_confirm_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PButton(
                text = stringResource(R.string.trips_action_activate_trip),
                onClick = onActivateClick,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Surfacing other transitions (active → in_transit, cancel) lives in
            // `TripStatusUpdateSheet`; we keep this surface read-only when not planning.
            Text(
                text = stringResource(R.string.trips_edit_status_locked_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun tripStatusLabelRes(status: TripStatus): Int = when (status) {
    TripStatus.PLANNING -> R.string.trips_status_planning
    TripStatus.ACTIVE -> R.string.trips_status_active
    TripStatus.IN_TRANSIT -> R.string.trips_status_in_transit
    TripStatus.COMPLETED -> R.string.trips_status_completed
    TripStatus.CANCELLED -> R.string.trips_status_cancelled
}

@Preview(showBackground = true, name = "EditTripStatusSection — planning, light")
@Preview(showBackground = true, name = "EditTripStatusSection — planning, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripStatusPlanningPreview() {
    PasabayanTheme {
        EditTripStatusSection(
            status = TripStatus.PLANNING,
            onActivateClick = {},
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripStatusSection — active, light")
@Preview(showBackground = true, name = "EditTripStatusSection — active, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripStatusActivePreview() {
    PasabayanTheme {
        EditTripStatusSection(
            status = TripStatus.ACTIVE,
            onActivateClick = {},
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
