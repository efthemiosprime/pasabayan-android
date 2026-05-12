package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoCard
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.packages.components.packageTypeLabel
import com.efthemiosprime.pasabayan.features.packages.components.urgencyLevelLabel
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage

/**
 * Carrier-side package details — iOS parity with `CarrierPackageDetailSheet`. Uses the
 * same `PDetailSheetScaffold` + `PDetailSheetCard` pattern as the shipper-explore
 * `TripDetailsScreen` so both sheets look and feel identical across roles.
 *
 * Sections mirror iOS exactly: Header, Route, Info, Schedule, Notes, primary CTA.
 * Reads from [AvailablePackage] directly — no detail endpoint round-trip needed.
 */
@Composable
fun CarrierPackageDetailSheet(
    pkg: AvailablePackage,
    onRequestToCarry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PDetailSheetScaffold(
        title = stringResource(R.string.carrier_package_detail_title),
        closeContentDescription = stringResource(R.string.carrier_package_detail_close),
        onClose = onClose,
        modifier = modifier,
    ) {
        HeaderCard(pkg = pkg)
        pkg.shipper?.let { ShipperInfoCard(shipper = it) }
        RouteCard(pkg = pkg)
        InfoCard(pkg = pkg)
        ScheduleCard(pkg = pkg)
        NotesCard(pkg = pkg)
        PButton(
            text = stringResource(R.string.carrier_package_detail_request_to_carry),
            onClick = onRequestToCarry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ShipperInfoCard(shipper: UserSummary) {
    PUserInfoCard(
        name = shipper.name,
        title = stringResource(R.string.carrier_package_detail_sender_information),
        rating = shipper.rating?.takeIf { it.isNotBlank() },
        totalRatings = shipper.totalRatings,
        verificationLevel = shipper.verificationLevel,
        noRatingsLabel = stringResource(R.string.carrier_package_card_no_ratings_yet),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun HeaderCard(pkg: AvailablePackage) {
    val title = pkg.packageDescription
        ?.takeIf { it.isNotBlank() }
        ?: packageTypeLabel(pkg.packageType)
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${pkg.packageType.icon} ${packageTypeLabel(pkg.packageType)} · ${pkg.urgencyLevel.icon} ${urgencyLevelLabel(pkg.urgencyLevel)}",
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RouteCard(pkg: AvailablePackage) {
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailSectionTitle(text = stringResource(R.string.carrier_package_detail_route))
            PRouteSection(
                origin = pkg.pickupCity,
                destination = pkg.deliveryCity,
            )
        }
    }
}

@Composable
private fun InfoCard(pkg: AvailablePackage) {
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            pkg.packageWeightKg?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_weight),
                    value = stringResource(R.string.packages_detail_weight_value, it),
                )
            }
            pkg.packageDimensions?.formatted?.let { dims ->
                PDetailRow(
                    label = stringResource(R.string.packages_detail_dimensions),
                    value = dims,
                )
            }
            pkg.maxPriceBudget?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_budget),
                    value = stringResource(R.string.packages_detail_budget_value, it),
                )
            }
            if (pkg.fragile) {
                PDetailRow(
                    label = stringResource(R.string.carrier_package_detail_handling),
                    value = stringResource(R.string.carrier_package_detail_fragile_item),
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(pkg: AvailablePackage) {
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailSectionTitle(text = stringResource(R.string.carrier_package_detail_schedule))
            PDetailRow(
                label = stringResource(R.string.carrier_package_detail_pickup_date),
                value = pkg.pickupDatePreferred,
            )
            PDetailRow(
                label = stringResource(R.string.carrier_package_detail_delivery_needed),
                value = pkg.deliveryDateNeeded,
            )
        }
    }
}

@Composable
private fun NotesCard(pkg: AvailablePackage) {
    val description = pkg.packageDescription
    if (description.isNullOrBlank()) return
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailSectionTitle(text = stringResource(R.string.carrier_package_detail_notes))
            Text(
                text = description,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "CarrierPackageDetailSheet — light", heightDp = 900)
@Preview(
    showBackground = true,
    name = "CarrierPackageDetailSheet — dark",
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CarrierPackageDetailSheetPreview() {
    PasabayanTheme {
        CarrierPackageDetailSheet(
            pkg = AvailablePackage(
                id = 1,
                packageRequestId = 1,
                pickupCity = "Montreal",
                pickupCountry = "Canada",
                deliveryCity = "Manila",
                deliveryCountry = "Philippines",
                packageWeightKg = 5.0,
                packageDimensions = null,
                urgencyLevel = UrgencyLevel.FLEXIBLE,
                maxPriceBudget = 100.0,
                pickupDatePreferred = "2026-05-15",
                pickupDateFlexible = false,
                deliveryDateNeeded = "2026-05-20",
                fragile = true,
                packageType = PackageType.GIFTS,
                packageDescription = "Alcohol — handle with care",
                createdAt = "2026-05-10T12:00:00Z",
                daysSincePosted = 2.0,
                distanceKm = null,
                shipper = UserSummary(
                    id = 42,
                    name = "Jane Shipper",
                    avatar = null,
                    verificationLevel = "verified",
                    rating = "4.7",
                    totalRatings = 12,
                ),
                serviceType = null,
            ),
            onRequestToCarry = {},
            onClose = {},
        )
    }
}
