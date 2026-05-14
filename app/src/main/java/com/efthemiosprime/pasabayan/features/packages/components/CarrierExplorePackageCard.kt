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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardActionFooter
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeVariant
import com.efthemiosprime.pasabayan.core.domain.`enum`.ErrandDirection
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.ShoppingItem

/**
 * Carrier-side explore card — iOS parity with `CarrierPackageCard` (standard variant).
 *
 * Branches on `(serviceType, direction)` into four card bodies — see
 * [CarrierExploreCardVariant]:
 *  - **Parcel** (legacy delivery): description + weight + budget + route + schedule.
 *  - **Receive**: shopping list preview + store + estimated cost; route shows
 *    `Store → Deliver to`. Used for grocery and any other carrier-pickup errand.
 *  - **Send**: recipient + phone; route shows `From → Recipient` (or `From → To`
 *    when recipient name is missing).
 *  - **Task**: task name as title + description as body; no route, no schedule
 *    (task errands have no pickup/delivery pair).
 */
@Composable
fun CarrierExplorePackageCard(
    pkg: AvailablePackage,
    onViewDetails: () -> Unit,
    onRequestToCarry: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenShipperProfile: (() -> Unit)? = null,
) {
    val variant = pkg.exploreVariant()
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            pkg.shipper?.let { shipper ->
                UserCardHeader(user = shipper, onClick = onOpenShipperProfile)
                PDivider()
            }

            TitleRow(pkg = pkg, variant = variant)

            when (variant) {
                CarrierExploreCardVariant.PARCEL -> ParcelBody(pkg)
                CarrierExploreCardVariant.RECEIVE -> ReceiveBody(pkg)
                CarrierExploreCardVariant.SEND -> SendBody(pkg)
                CarrierExploreCardVariant.TASK -> TaskBody(pkg)
            }

            PCardActionFooter(
                onViewDetails = onViewDetails,
                directTrailingAction = CardMenuAction(
                    title = stringResource(R.string.carrier_package_detail_request_to_carry),
                    onClick = onRequestToCarry,
                ),
            )
        }
    }
}

/** Resolved variant for the carrier explore card. Pure function of (service_type, direction). */
internal enum class CarrierExploreCardVariant { PARCEL, RECEIVE, SEND, TASK }

internal fun AvailablePackage.exploreVariant(): CarrierExploreCardVariant {
    if (!isServiceRequest) return CarrierExploreCardVariant.PARCEL
    return when (errandDirection) {
        ErrandDirection.TASK -> CarrierExploreCardVariant.TASK
        ErrandDirection.SEND -> CarrierExploreCardVariant.SEND
        ErrandDirection.RECEIVE -> CarrierExploreCardVariant.RECEIVE
    }
}

@Composable
private fun TitleRow(pkg: AvailablePackage, variant: CarrierExploreCardVariant) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = titleForCard(pkg, variant),
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PackageTypeChip(label = packageTypeLabel(pkg.packageType))
                // iOS parity: item-count caption only for receive errands with a list.
                if (variant == CarrierExploreCardVariant.RECEIVE && pkg.shoppingItems.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.carrier_package_card_items_count,
                            pkg.shoppingItems.size,
                        ),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            PStatusBadge(
                config = UrgencyBadgeConfig(pkg.urgencyLevel, urgencyLevelLabel(pkg.urgencyLevel)),
                variant = StatusBadgeVariant.Compact,
            )
            if (pkg.fragile) {
                FragileChip()
            }
        }
    }
}

@Composable
private fun titleForCard(pkg: AvailablePackage, variant: CarrierExploreCardVariant): String {
    val taskName = pkg.taskName?.takeIf { it.isNotBlank() }
    val description = pkg.packageDescription?.takeIf { it.isNotBlank() }
    return when (variant) {
        CarrierExploreCardVariant.TASK -> taskName ?: description ?: packageTypeLabel(pkg.packageType)
        else -> description ?: packageTypeLabel(pkg.packageType)
    }
}

// -- Variant bodies --

@Composable
private fun ParcelBody(pkg: AvailablePackage) {
    PRouteSection(
        origin = pkg.pickupCity,
        destination = pkg.deliveryCity,
        originCaption = stringResource(R.string.carrier_package_card_from),
        destinationCaption = stringResource(R.string.carrier_package_card_to),
    )
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        pkg.packageWeightKg?.let {
            PDetailRow(
                label = stringResource(R.string.packages_detail_weight),
                value = stringResource(R.string.packages_detail_weight_value, it),
            )
        }
        pkg.maxPriceBudget?.let {
            PDetailRow(
                label = stringResource(R.string.packages_detail_budget),
                value = stringResource(R.string.packages_detail_budget_value, it),
                valueColor = PasabayanColors.Success,
            )
        }
    }
    ScheduleRow(pkg = pkg)
}

@Composable
private fun ReceiveBody(pkg: AvailablePackage) {
    // iOS parity (originValue): storeName → storeAddress → "Carrier's choice".
    val storeLabel = pkg.storeName?.takeIf { it.isNotBlank() }
        ?: pkg.storeAddress?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.carrier_package_card_store_carrier_choice)
    PRouteSection(
        origin = storeLabel,
        destination = pkg.deliveryCity,
        originCaption = stringResource(R.string.carrier_package_card_store),
        destinationCaption = stringResource(R.string.carrier_package_card_deliver_to),
    )
    if (pkg.shoppingItems.isNotEmpty()) {
        ShoppingListPreview(items = pkg.shoppingItems)
    }
    pkg.estimatedCost?.takeIf { it.isNotBlank() }?.let { cost ->
        PDetailRow(
            label = stringResource(R.string.carrier_package_card_estimated_cost),
            value = stringResource(R.string.packages_detail_budget_value, cost.toDoubleOrNull() ?: 0.0),
            valueColor = PasabayanColors.Success,
        )
    }
    ScheduleRow(pkg = pkg)
}

/** Receive-errand shopping list preview — iOS parity: up to 3 items + "+N more". */
@Composable
private fun ShoppingListPreview(items: List<ShoppingItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.carrier_package_card_shopping_list),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        items.take(SHOPPING_PREVIEW_LIMIT).forEach { item ->
            val line = if (item.quantity.isBlank()) "• ${item.item}" else "• ${item.quantity} ${item.item}"
            Text(
                text = line,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val overflow = items.size - SHOPPING_PREVIEW_LIMIT
        if (overflow > 0) {
            Text(
                text = stringResource(R.string.carrier_package_card_more_items, overflow),
                style = PasabayanTextStyles.Caption.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val SHOPPING_PREVIEW_LIMIT = 3

@Composable
private fun SendBody(pkg: AvailablePackage) {
    // iOS parity (destinationLabel + destinationValue): when recipient name is
    // present the right cell flips to "Recipient" + name; otherwise it stays on
    // the place-based "To" + delivery_city pair so the cell remains self-consistent.
    val recipientName = pkg.recipientName?.takeIf { it.isNotBlank() }
    val destinationValue = recipientName ?: pkg.deliveryCity
    val destinationCaption = if (recipientName != null) {
        stringResource(R.string.carrier_package_card_recipient)
    } else {
        stringResource(R.string.carrier_package_card_to)
    }
    PRouteSection(
        origin = pkg.pickupCity,
        destination = destinationValue,
        originCaption = stringResource(R.string.carrier_package_card_from),
        destinationCaption = destinationCaption,
    )
    // iOS parity (sendErrandSection): name + phone rendered as body rows in
    // addition to the right-cell value. Both rows hidden when empty.
    recipientName?.let { name ->
        PDetailRow(
            label = stringResource(R.string.carrier_package_card_recipient),
            value = name,
        )
    }
    pkg.recipientPhone?.takeIf { it.isNotBlank() }?.let { phone ->
        PDetailRow(
            label = stringResource(R.string.carrier_package_card_recipient_phone),
            value = phone,
        )
    }
    ScheduleRow(pkg = pkg)
}

@Composable
private fun TaskBody(pkg: AvailablePackage) {
    // Task errands have no pickup/delivery pair — skip route and schedule.
    val description = pkg.taskDescription?.takeIf { it.isNotBlank() }
        ?: pkg.packageDescription?.takeIf { it.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.carrier_package_card_task_details),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        description?.let {
            Text(
                text = it,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PackageTypeChip(label: String) {
    Text(
        text = label,
        style = PasabayanTextStyles.Caption.small,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                color = PasabayanColors.BadgeGrayLight,
                shape = RoundedCornerShape(PasabayanRadius.sm),
            )
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    )
}

@Composable
private fun FragileChip() {
    Row(
        modifier = Modifier
            .background(
                color = PasabayanColors.Warning.copy(alpha = 0.15f),
                shape = RoundedCornerShape(PasabayanRadius.sm),
            )
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = PasabayanColors.Warning,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(R.string.carrier_package_card_fragile),
            style = PasabayanTextStyles.Caption.small,
            color = PasabayanColors.Warning,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ScheduleRow(pkg: AvailablePackage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.carrier_package_detail_pickup_date),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pkg.pickupDatePreferred,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.carrier_package_detail_delivery_needed),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pkg.deliveryDateNeeded,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}


@Preview(showBackground = true, name = "Card variants — light")
@Preview(showBackground = true, name = "Card variants — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePackageCardPreview() {
    PasabayanTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        ) {
            // Parcel — standard delivery
            CarrierExplorePackageCard(
                pkg = previewParcelPackage(),
                onViewDetails = {},
                onRequestToCarry = {},
            )
            // Receive — carrier picks up and delivers to shipper
            CarrierExplorePackageCard(
                pkg = previewReceivePackage(),
                onViewDetails = {},
                onRequestToCarry = {},
            )
            // Send — recipient
            CarrierExplorePackageCard(
                pkg = previewSendPackage(),
                onViewDetails = {},
                onRequestToCarry = {},
            )
            // Task — custom errand, no route
            CarrierExplorePackageCard(
                pkg = previewTaskPackage(),
                onViewDetails = {},
                onRequestToCarry = {},
            )
        }
    }
}

private fun previewParcelPackage() = AvailablePackage(
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
    shipper = previewShipper(),
    serviceType = null,
)

private fun previewReceivePackage() = previewParcelPackage().copy(
    id = 2,
    packageRequestId = 2,
    packageDescription = "Weekly groceries",
    packageType = PackageType.GENERAL,
    fragile = false,
    urgencyLevel = UrgencyLevel.NORMAL,
    serviceType = "grocery_shopping",
    direction = "receive",
    storeName = "Metro Plus",
    estimatedCost = "78.50",
    shoppingItems = listOf(
        ShoppingItem(item = "Milk 2L", quantity = "2"),
        ShoppingItem(item = "Eggs", quantity = "1"),
        ShoppingItem(item = "Bread", quantity = "2"),
        ShoppingItem(item = "Bananas", quantity = "1"),
    ),
)

private fun previewSendPackage() = previewParcelPackage().copy(
    id = 3,
    packageRequestId = 3,
    packageDescription = "Documents",
    packageType = PackageType.GENERAL,
    fragile = false,
    urgencyLevel = UrgencyLevel.HIGH,
    serviceType = "general_errand",
    direction = "send",
    recipientName = "Jane Doe",
    recipientPhone = "+1 514 555 1234",
)

private fun previewTaskPackage() = previewParcelPackage().copy(
    id = 4,
    packageRequestId = 4,
    packageDescription = null,
    packageType = PackageType.GENERAL,
    fragile = false,
    urgencyLevel = UrgencyLevel.URGENT,
    serviceType = "general_errand",
    direction = "task",
    taskName = "Walk my dog",
    taskDescription = "Daily walk, 30 min. Leash by the door, treats in the kitchen jar.",
)

private fun previewShipper() = UserSummary(
    id = 42,
    name = "Jane Shipper",
    avatar = null,
    verificationLevel = "verified",
    rating = "4.7",
    totalRatings = 12,
)
