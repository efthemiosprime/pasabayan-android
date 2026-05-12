package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardActionFooter
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

@Composable
fun PackageRequestCard(
    pkg: PackageRequest,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
    menuActions: List<CardMenuAction> = emptyList(),
    /**
     * When `true` (default, shipper-packages-tab behavior), tapping "View Details" expands
     * the card fullscreen via [PExpandableCardHost]. When `false`, expansion is skipped and
     * only [onViewDetails] fires — used by the carrier-explore view, which opens its own
     * [PModalBottomSheet] with package details (mirrors the shipper-explore trip-details
     * sheet pattern).
     */
    expandable: Boolean = true,
) {
    val statusLabel = packageStatusLabel(pkg.status)
    var expanded by rememberSaveable { mutableStateOf(false) }

    PExpandableCard(
        expanded = expanded,
        onExpandChange = { expanded = it },
        modifier = modifier,
        collapsedContent = {
            // Summary view
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = packageDisplayTitle(pkg),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    PStatusBadge(config = PackageStatusBadgeConfig(pkg.status, statusLabel))
                }

                if (pkg.pickupLocation.isNotEmpty() && pkg.deliveryLocation.isNotEmpty()) {
                    PRouteSection(
                        origin = pkg.pickupCity ?: "",
                        destination = pkg.deliveryCity ?: "",
                        originAddress = pkg.pickupAddress,
                        destinationAddress = pkg.deliveryAddress,
                    )
                }

                pkg.packageWeightKg?.let { weight ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_weight),
                        value = stringResource(R.string.packages_detail_weight_value, weight),
                    )
                }

                PCardActionFooter(
                    onViewDetails = {
                        if (expandable) {
                            expanded = true
                        }
                        onViewDetails()
                    },
                    menuActions = menuActions,
                )
            }
        },
        expandedContent = {
            // Full detail view inside expanded card
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = packageDisplayTitle(pkg),
                        style = PasabayanTextStyles.Heading.h4,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    PStatusBadge(config = PackageStatusBadgeConfig(pkg.status, statusLabel))
                }

                // Route
                if (pkg.pickupLocation.isNotEmpty() && pkg.deliveryLocation.isNotEmpty()) {
                    PRouteSection(
                        origin = pkg.pickupCity ?: "",
                        destination = pkg.deliveryCity ?: "",
                        originAddress = pkg.pickupAddress,
                        destinationAddress = pkg.deliveryAddress,
                    )
                }

                // Package details
                pkg.packageWeightKg?.let { weight ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_weight),
                        value = stringResource(R.string.packages_detail_weight_value, weight),
                    )
                }
                pkg.packageDimensions?.let { dims ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_dimensions),
                        value = dims.formatted,
                    )
                }
                pkg.packageType?.let { type ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_type),
                        value = "${type.icon} ${packageTypeLabel(type)}",
                    )
                }
                pkg.urgencyLevel?.let { urgency ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_urgency),
                        value = "${urgency.icon} ${urgencyLevelLabel(urgency)}",
                    )
                }
                if (pkg.isFragile) {
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_fragile),
                        value = stringResource(R.string.packages_detail_fragile_yes),
                    )
                }
                pkg.maxPriceBudget?.let { budget ->
                    PDetailRow(
                        label = stringResource(R.string.packages_detail_budget),
                        value = stringResource(R.string.packages_detail_budget_value, budget),
                    )
                }
                pkg.packageDescription?.let { desc ->
                    Text(
                        text = stringResource(R.string.packages_detail_description),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = desc,
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                pkg.specialHandlingRequirements?.let { handling ->
                    Text(
                        text = stringResource(R.string.packages_detail_handling),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = handling,
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun packageStatusLabel(status: PackageRequestStatus): String = when (status) {
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

@Preview(showBackground = true, name = "PackageCard — light")
@Preview(showBackground = true, name = "PackageCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestCardPreview() {
    PasabayanTheme {
        PackageRequestCard(
            pkg = previewPackage(),
            onViewDetails = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

private fun previewPackage() = PackageRequest(
    id = 1, shipperId = 5,
    pickupAddress = "123 Rue St-Laurent", pickupCity = "Montreal", pickupCountry = "Canada",
    deliveryAddress = "456 Bank St", deliveryCity = "Ottawa", deliveryCountry = "Canada",
    packageWeightKg = 12.5, packageDimensions = null,
    packageType = PackageType.ELECTRONICS, fragile = true,
    packageValue = 500.0, packageDescription = "Laptop and accessories",
    urgencyLevel = UrgencyLevel.HIGH, maxPriceBudget = 150.0,
    pickupDatePreferred = "2026-04-05", pickupTimePreferred = "10:00",
    pickupDateFlexible = false, deliveryDateNeeded = "2026-04-07",
    deliveryTimeNeeded = null, specialHandlingRequirements = null,
    requestStatus = PackageRequestStatus.OPEN, createdAt = null, updatedAt = null,
    compatibleTripsCount = 3, shipper = null,
    images = null, imagesProcessing = null,
    serviceType = null, shoppingList = null,
    storeName = null, storeAddress = null, receiptRequired = null,
)
