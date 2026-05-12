package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage

/**
 * Carrier-side explore card — iOS parity with `CarrierPackageCard` (standard variant).
 * Mirrors the information density of the shipper-side [ShipperExploreTripCard]:
 * shipper header, title + urgency, route, weight + budget, pickup/delivery timeline,
 * and an inline "Request to Carry" CTA in the footer.
 */
@Composable
fun CarrierExplorePackageCard(
    pkg: AvailablePackage,
    onViewDetails: () -> Unit,
    onRequestToCarry: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenShipperProfile: (() -> Unit)? = null,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            pkg.shipper?.let { shipper ->
                ShipperHeader(shipper = shipper, onOpenProfile = onOpenShipperProfile)
                PDivider()
            }

            TitleRow(pkg = pkg)

            PRouteSection(
                origin = pkg.pickupCity,
                destination = pkg.deliveryCity,
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

@Composable
private fun ShipperHeader(
    shipper: UserSummary,
    onOpenProfile: (() -> Unit)?,
) {
    val rowModifier = if (onOpenProfile != null) {
        Modifier
            .fillMaxWidth()
            .padding(end = PasabayanSpacing.xs)
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarInitials(name = shipper.name)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = shipper.name,
                    style = PasabayanTextStyles.Body.large,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                VerificationDot(level = shipper.verificationLevel)
            }
            RatingRow(formattedRating = shipper.formattedRating, totalRatings = shipper.totalRatings)
        }
    }
}

@Composable
private fun TitleRow(pkg: AvailablePackage) {
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
                text = titleForPackage(pkg),
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PackageTypeChip(label = packageTypeLabel(pkg.packageType))
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
private fun titleForPackage(pkg: AvailablePackage): String {
    val description = pkg.packageDescription
    return if (!description.isNullOrBlank()) description else packageTypeLabel(pkg.packageType)
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
private fun RatingRow(formattedRating: String, totalRatings: Int?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (totalRatings != null && totalRatings > 0) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = PasabayanColors.BadgeGold,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = formattedRating,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "($totalRatings)",
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = stringResource(R.string.carrier_package_card_no_ratings_yet),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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

@Composable
private fun AvatarInitials(name: String) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = PasabayanTextStyles.Heading.h6,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun VerificationDot(level: String?) {
    val normalized = level?.lowercase()?.trim() ?: return
    val tint = when (normalized) {
        "verified" -> PasabayanColors.Success
        "premium" -> PasabayanColors.BadgeGold
        else -> return
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color = tint, shape = CircleShape),
    )
}

@Preview(showBackground = true, name = "CarrierExplorePackageCard — light")
@Preview(showBackground = true, name = "CarrierExplorePackageCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePackageCardPreview() {
    PasabayanTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        ) {
            CarrierExplorePackageCard(
                pkg = previewPackage(),
                onViewDetails = {},
                onRequestToCarry = {},
            )
            CarrierExplorePackageCard(
                pkg = previewPackage().copy(
                    packageDescription = null,
                    fragile = false,
                    urgencyLevel = UrgencyLevel.URGENT,
                    shipper = previewPackage().shipper?.copy(totalRatings = 0, rating = null),
                ),
                onViewDetails = {},
                onRequestToCarry = {},
            )
        }
    }
}

private fun previewPackage() = AvailablePackage(
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
)
