package com.efthemiosprime.pasabayan.features.trips.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage

/**
 * iOS parity (`TripDetailsView.tripEarningsOverviewCard` lines 651–706): only render when total
 * earnings > 0, two-column rows (label left, value right), and locale-aware currency formatting.
 */
@Composable
internal fun CarrierEarningsSection(
    trip: Trip,
    matches: List<TripMatchPackage>,
) {
    val total = computeTripEarningsTotal(trip)
    if (total <= 0.0) return
    val currency = trip.tripEarningsCurrency
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            PDetailSectionTitle(text = stringResource(R.string.trips_detail_earnings_title))
            EarningsRow(
                label = stringResource(R.string.trips_detail_earnings_label_total),
                value = formatTripCurrency(total, currency),
            )
            trip.tripEarningsBreakdown?.let { breakdown ->
                EarningsRow(
                    label = stringResource(R.string.trips_detail_earnings_label_delivered),
                    value = formatTripCurrency(breakdown.deliveredAmount, breakdown.deliveredCurrency),
                )
                EarningsRow(
                    label = stringResource(R.string.trips_detail_earnings_label_pending),
                    value = formatTripCurrency(breakdown.pendingAmount, breakdown.pendingCurrency),
                )
                // iOS parity: show package counts when the breakdown reports them. Fall back to
                // local match counts so the UI stays informative on older API responses.
                val deliveredCount = breakdown.deliveredCount.takeIf { it > 0 }
                    ?: matches.count { it.matchStatus == MatchStatus.DELIVERED }
                val pendingCount = breakdown.pendingCount.takeIf { it > 0 }
                    ?: matches.count { it.matchStatus != MatchStatus.DELIVERED }
                if (deliveredCount > 0 || pendingCount > 0) {
                    EarningsRow(
                        label = stringResource(R.string.trips_detail_earnings_label_delivered_packages),
                        value = deliveredCount.toString(),
                    )
                    EarningsRow(
                        label = stringResource(R.string.trips_detail_earnings_label_pending_packages),
                        value = pendingCount.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EarningsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * iOS parity (`tripEarningsTotal` in TripDetailsView): prefer the server's total, otherwise fall
 * back to delivered + pending from the breakdown. Returns 0 when neither is available.
 */
internal fun computeTripEarningsTotal(trip: Trip): Double {
    trip.tripEarningsTotal?.let { return it }
    val breakdown = trip.tripEarningsBreakdown ?: return 0.0
    return breakdown.deliveredAmount + breakdown.pendingAmount
}

/**
 * iOS parity (`formatCurrency`): locale-aware currency formatting. Falls back to `<code> <amount>`
 * when the currency code is unknown or invalid.
 */
internal fun formatTripCurrency(amount: Double, currency: String?): String {
    val code = currency?.takeIf { it.isNotBlank() } ?: "CAD"
    return try {
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
        formatter.currency = java.util.Currency.getInstance(code)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        formatter.format(amount)
    } catch (_: IllegalArgumentException) {
        String.format(java.util.Locale.getDefault(), "%s %.2f", code, amount)
    }
}
