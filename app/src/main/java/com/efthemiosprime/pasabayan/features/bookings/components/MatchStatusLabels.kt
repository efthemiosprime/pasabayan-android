package com.efthemiosprime.pasabayan.features.bookings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus

/**
 * Localized label for a [MatchStatus]. Mirrors iOS `MatchStatus.displayName`.
 *
 * Acceptance/decline shipper/carrier variants collapse to the same user-facing
 * label as the canonical confirmed/cancelled status, matching iOS behaviour.
 */
@Composable
@ReadOnlyComposable
fun matchStatusLabel(status: MatchStatus): String = when (status) {
    MatchStatus.PENDING -> stringResource(R.string.bookings_status_pending)
    MatchStatus.CONFIRMED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.PICKED_UP -> stringResource(R.string.bookings_status_picked_up)
    MatchStatus.IN_TRANSIT -> stringResource(R.string.bookings_status_in_transit)
    MatchStatus.DELIVERED -> stringResource(R.string.bookings_status_delivered)
    MatchStatus.CANCELLED -> stringResource(R.string.bookings_status_cancelled)
    MatchStatus.CARRIER_REQUESTED -> stringResource(R.string.bookings_status_carrier_requested)
    MatchStatus.SHIPPER_REQUESTED -> stringResource(R.string.bookings_status_shipper_requested)
    MatchStatus.SHIPPER_ACCEPTED,
    MatchStatus.CARRIER_ACCEPTED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.SHIPPER_DECLINED,
    MatchStatus.CARRIER_DECLINED -> stringResource(R.string.bookings_status_cancelled)
}
