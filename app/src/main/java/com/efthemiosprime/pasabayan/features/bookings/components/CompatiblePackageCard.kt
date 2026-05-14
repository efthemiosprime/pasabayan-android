package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

/**
 * Carrier-side card on the "compatible packages for trip" flow. Mirrors iOS
 * `CompatiblePackageCard` (`CompatiblePackagesForTripView.swift` line 349).
 *
 * Stateless by construction: takes the package, the carrier's estimated
 * earnings for *this* trip (computed by the caller from trip pricing), and an
 * `onRequestToCarry` callback. No `ViewModel` or business logic — the screen
 * orchestrates loading + actions.
 */
@Composable
fun CompatiblePackageCard(
    pkg: PackageRequest,
    estimatedEarnings: Double?,
    onRequestToCarry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            HeaderRow(pkg)

            if (pkg.pickupCity?.isNotBlank() == true && pkg.deliveryCity?.isNotBlank() == true) {
                PRouteSection(
                    origin = pkg.pickupCity ?: "",
                    destination = pkg.deliveryCity ?: "",
                    originAddress = pkg.pickupAddress,
                    destinationAddress = pkg.deliveryAddress,
                )
            }

            pkg.packageDescription?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            pkg.pickupDatePreferred?.takeIf { it.isNotBlank() }?.let { date ->
                PDetailRow(
                    label = stringResource(R.string.bookings_compatible_packages_pickup_date),
                    value = if (pkg.pickupDateFlexible == true) {
                        "$date ${stringResource(R.string.bookings_compatible_packages_flexible_suffix)}"
                    } else {
                        date
                    },
                )
            }

            pkg.maxPriceBudget?.let { budget ->
                PDetailRow(
                    label = stringResource(R.string.bookings_compatible_packages_sender_budget),
                    value = stringResource(R.string.bookings_compatible_packages_budget_max, budget),
                )
            }

            estimatedEarnings?.let { earnings ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.bookings_compatible_packages_estimated_earnings),
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.bookings_compatible_packages_price_cad, earnings),
                        style = PasabayanTextStyles.Heading.h5,
                        fontWeight = FontWeight.Bold,
                        color = PasabayanColors.Success,
                    )
                }
            }

            PDivider()

            PButton(
                text = stringResource(R.string.bookings_compatible_packages_request_to_carry),
                onClick = onRequestToCarry,
                size = PButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HeaderRow(pkg: PackageRequest) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        val shipper = pkg.shipper
        if (shipper != null) {
            UserCardHeader(user = shipper, modifier = Modifier.weight(1f))
        } else {
            Text(
                text = pkg.title.ifBlank { stringResource(R.string.bookings_compatible_packages_title) },
                style = PasabayanTextStyles.Heading.h6,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            pkg.packageType?.let { type ->
                Text(
                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            pkg.packageWeightKg?.let { weight ->
                Text(
                    text = stringResource(R.string.bookings_compatible_packages_weight_kg, weight),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// -- Previews -----------------------------------------------------------------

@Preview(showBackground = true, name = "CompatiblePackageCard — light")
@Preview(showBackground = true, name = "CompatiblePackageCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompatiblePackageCardPreview() {
    PasabayanTheme {
        CompatiblePackageCard(
            pkg = previewPackage(),
            estimatedEarnings = 37.50,
            onRequestToCarry = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "CompatiblePackageCard — no shipper")
@Composable
private fun CompatiblePackageCardNoShipperPreview() {
    PasabayanTheme {
        CompatiblePackageCard(
            pkg = previewPackage().copy(shipper = null, packageDescription = null),
            estimatedEarnings = null,
            onRequestToCarry = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
        )
    }
}

private fun previewPackage(): PackageRequest = PackageRequest(
    id = 642,
    shipperId = 5,
    pickupAddress = "1234 Testing",
    pickupCity = "Boucherville",
    pickupCountry = "Canada",
    deliveryAddress = "4321 Example",
    deliveryCity = "Repentigny",
    deliveryCountry = "Canada",
    packageWeightKg = 5.0,
    packageDimensions = null,
    packageType = PackageType.ELECTRONICS,
    fragile = false,
    packageValue = 1000.0,
    packageDescription = "Flat-screen TV in original packaging",
    urgencyLevel = UrgencyLevel.NORMAL,
    maxPriceBudget = 200.0,
    pickupDatePreferred = "2026-05-24",
    pickupTimePreferred = null,
    pickupDateFlexible = true,
    deliveryDateNeeded = "2026-05-25",
    deliveryTimeNeeded = null,
    specialHandlingRequirements = null,
    requestStatus = null,
    createdAt = null,
    updatedAt = null,
    compatibleTripsCount = null,
    shipper = UserSummary(
        id = 5,
        name = "Joy Marie Blanco",
        rating = "4.7",
        totalRatings = 32,
        avatar = null,
    ),
    images = null,
    imagesProcessing = null,
    serviceType = null,
    shoppingList = null,
    storeName = null,
    storeAddress = null,
    receiptRequired = null,
)

