package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * Summary card for compatible trips on a package detail.
 *
 * iOS parity: `PackageDetailView` shows a similar "X compatible trips" section
 * that opens [com.efthemiosprime.pasabayan.features.bookings.ui.CompatibleTripsView]
 * on tap. Android delegates to the bookings feature via [onViewCompatibleTrips].
 *
 * Hidden when [count] is `null` or zero.
 */
@Composable
fun PackageCompatibleTripsSummary(
    count: Int?,
    onViewCompatibleTrips: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeCount = count?.takeIf { it > 0 } ?: return
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.packages_detail_compatible_trips_count,
                        safeCount,
                        safeCount,
                    ),
                    style = PasabayanTextStyles.Body.large,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.packages_detail_compatible_trips_hint),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PButton(
                text = stringResource(R.string.packages_view_compatible_trips),
                onClick = onViewCompatibleTrips,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "CompatibleTrips — light")
@Preview(showBackground = true, name = "CompatibleTrips — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageCompatibleTripsSummaryPreview() {
    PasabayanTheme {
        PackageCompatibleTripsSummary(count = 3, onViewCompatibleTrips = {})
    }
}
