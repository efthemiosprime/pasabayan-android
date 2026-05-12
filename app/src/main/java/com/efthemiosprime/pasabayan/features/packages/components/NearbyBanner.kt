package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Surfaces the `/packages/available` envelope `nearby` flag. Rendered
 * only when the server has fallen back to non-proximity results
 * (`nearby == false`) so the user knows the list expanded beyond their
 * home city. iOS captures the same flag in
 * `CarrierBrowsePackagesViewModel.responseNearby` but its `nearbyBanner`
 * is referenced in the view layer and never implemented — Android wires
 * it here.
 *
 * `nearby == true` (the expected default) and `nearby == null` (legacy
 * response shape) deliberately render nothing.
 */
@Composable
fun NearbyFallbackBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                PasabayanColors.BadgeAmberLight,
                RoundedCornerShape(PasabayanRadius.sm),
            )
            .padding(PasabayanSpacing.md)
            .testTag(NEARBY_FALLBACK_BANNER_TEST_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOff,
            contentDescription = null,
            tint = PasabayanColors.Warning,
            modifier = Modifier.size(PasabayanSpacing.lg),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.dashboard_carrier_not_nearby),
                style = PasabayanTextStyles.Caption.large,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

internal const val NEARBY_FALLBACK_BANNER_TEST_TAG = "Packages.NearbyFallbackBanner"

@Preview(showBackground = true, name = "NearbyBanner — light")
@Preview(showBackground = true, name = "NearbyBanner — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NearbyFallbackBannerPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            NearbyFallbackBanner()
        }
    }
}
