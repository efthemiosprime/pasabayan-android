package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoCard
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

/**
 * Shipper info card on the package detail screen — iOS parity for the
 * `shipperInfoSection` in `PackageDetailView`.
 *
 * Wraps the shared design-system [PUserInfoCard]. Hidden when [shipper] is null.
 */
@Composable
fun PackageShipperInfoCard(
    shipper: UserSummary?,
    modifier: Modifier = Modifier,
) {
    if (shipper == null) return
    PUserInfoCard(
        name = shipper.name,
        title = stringResource(R.string.packages_detail_shipper_info_title),
        rating = shipper.formattedRating,
        totalRatings = shipper.totalRatings,
        verificationLevel = shipper.verificationLevel,
        noRatingsLabel = stringResource(R.string.packages_detail_shipper_no_ratings),
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "Shipper — light")
@Preview(showBackground = true, name = "Shipper — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageShipperInfoCardPreview() {
    PasabayanTheme {
        PackageShipperInfoCard(
            shipper = UserSummary(
                id = 42,
                name = "Jordan T.",
                rating = "4.7",
                totalRatings = 24,
                verificationLevel = "verified",
            ),
        )
    }
}
