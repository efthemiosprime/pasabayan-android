package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.components.PackageDetailsSection
import com.efthemiosprime.pasabayan.features.packages.components.PackageStatusBadgeConfig
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

@Composable
fun PackageDetailScreen(
    pkg: PackageRequest,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (pkg.status) {
        PackageRequestStatus.OPEN -> stringResource(R.string.packages_status_open)
        PackageRequestStatus.PENDING_REQUEST -> stringResource(R.string.packages_status_pending_request)
        PackageRequestStatus.MATCHED -> stringResource(R.string.packages_status_matched)
        PackageRequestStatus.PICKED_UP -> stringResource(R.string.packages_status_picked_up)
        PackageRequestStatus.DELIVERED -> stringResource(R.string.packages_status_delivered)
        PackageRequestStatus.CANCELLED -> stringResource(R.string.packages_status_cancelled)
        PackageRequestStatus.PENDING -> stringResource(R.string.packages_status_pending_request)
        PackageRequestStatus.BOOKED -> stringResource(R.string.packages_status_matched)
        PackageRequestStatus.IN_TRANSIT -> stringResource(R.string.packages_status_picked_up)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        PStatusBadge(config = PackageStatusBadgeConfig(pkg.status, statusLabel))

        Text(
            text = pkg.title,
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Route
        if (pkg.pickupLocation.isNotEmpty() && pkg.deliveryLocation.isNotEmpty()) {
            PCard {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.packages_detail_pickup),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PRouteSection(
                        origin = pkg.pickupCity ?: "",
                        destination = pkg.deliveryCity ?: "",
                        originAddress = pkg.pickupAddress,
                        destinationAddress = pkg.deliveryAddress,
                    )
                }
            }
        }

        // Package details
        PackageDetailsSection(
            weightKg = pkg.packageWeightKg,
            dimensions = pkg.packageDimensions?.formatted,
            packageType = pkg.packageType?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
            isFragile = pkg.isFragile,
            description = pkg.packageDescription,
            specialHandling = pkg.specialHandlingRequirements,
        )

        // Budget & urgency
        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                pkg.maxPriceBudget?.let {
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_budget),
                        value = String.format("$%.2f", it),
                    )
                }
                pkg.urgencyLevel?.let {
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_urgency),
                        value = "${it.icon} ${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }}",
                    )
                }
            }
        }

        // Actions
        if (pkg.status == PackageRequestStatus.OPEN || pkg.status == PackageRequestStatus.PENDING_REQUEST) {
            PButton(
                text = stringResource(R.string.packages_edit_package),
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.packages_cancel_package),
                onClick = onCancel,
                style = PButtonStyle.Destructive,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "PackageDetail — light", heightDp = 900)
@Preview(showBackground = true, name = "PackageDetail — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageDetailPreview() {
    PasabayanTheme {
        PackageDetailScreen(
            pkg = PackageRequest(
                id = 1, shipperId = 5,
                pickupAddress = "123 Rue St-Laurent", pickupCity = "Montreal", pickupCountry = "Canada",
                deliveryAddress = "456 Bank St", deliveryCity = "Ottawa", deliveryCountry = "Canada",
                packageWeightKg = 12.5, packageDimensions = null,
                packageType = PackageType.ELECTRONICS, fragile = true,
                packageValue = 500.0, packageDescription = "Laptop and accessories",
                urgencyLevel = UrgencyLevel.HIGH, maxPriceBudget = 150.0,
                pickupDatePreferred = "2026-04-05", pickupTimePreferred = "10:00",
                pickupDateFlexible = false, deliveryDateNeeded = "2026-04-07",
                deliveryTimeNeeded = null, specialHandlingRequirements = "Keep upright",
                requestStatus = PackageRequestStatus.OPEN, createdAt = null, updatedAt = null,
                compatibleTripsCount = 3, shipper = null,
                images = null, imagesProcessing = null,
                serviceType = null, shoppingList = null,
                storeName = null, storeAddress = null, receiptRequired = null,
            ),
            onEdit = {},
            onCancel = {},
            onBack = {},
        )
    }
}
