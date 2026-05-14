package com.efthemiosprime.pasabayan.features.trips.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.components.matchStatusLabel
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage

/**
 * iOS parity (`TripMatchPackageCard` in `TripDetailsView.swift` lines 1196–1395): header row with
 * description + colored status badge, pickup→delivery city row, shipper name + rating, code state
 * rows, optional chat pill. The whole card is tap-to-chat when a conversation id is present, and
 * dimmed to ~85% otherwise.
 */
@Composable
internal fun TripMatchPackageCard(
    match: TripMatchPackage,
    onChatTap: ((Int) -> Unit)?,
) {
    val conversationId = match.chatConversationId
    val isChatAvailable = conversationId != null && onChatTap != null
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { base ->
            if (isChatAvailable) {
                base.clickable { onChatTap?.invoke(conversationId!!) }
            } else {
                base
            }
        }
        .padding(vertical = PasabayanSpacing.xs)
    Column(
        modifier = cardModifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        MatchHeaderRow(
            description = match.packageDescription
                ?: stringResource(R.string.trips_detail_package_fallback),
            status = match.matchStatus,
            isDimmed = !isChatAvailable,
        )
        MatchRouteRow(
            pickupCity = match.packagePickupCity,
            deliveryCity = match.packageDeliveryCity,
        )
        MatchShipperRow(
            name = match.shipper?.name,
            rating = match.shipper?.ratingValue,
        )
        MatchCodeStateRow(
            title = stringResource(R.string.trips_detail_pickup_code),
            state = pickupCodeState(match),
        )
        MatchCodeStateRow(
            title = stringResource(R.string.trips_detail_delivery_code),
            state = deliveryCodeState(match),
        )
        if (isChatAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                MatchChatPill(onClick = { onChatTap?.invoke(conversationId!!) })
            }
        }
    }
}

@Composable
private fun MatchHeaderRow(
    description: String,
    status: MatchStatus,
    isDimmed: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = description,
            style = PasabayanTextStyles.Body.medium,
            color = if (isDimmed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        MatchStatusBadge(status = status)
    }
}

@Composable
private fun MatchStatusBadge(status: MatchStatus) {
    val color = matchStatusBadgeColor(status)
    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    ) {
        Text(
            text = matchStatusLabel(status),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
        )
    }
}

@Composable
private fun MatchRouteRow(pickupCity: String?, deliveryCity: String?) {
    val unknown = stringResource(R.string.trips_detail_match_route_unknown)
    val routeText = stringResource(
        R.string.trips_detail_match_route_format,
        pickupCity?.takeIf { it.isNotBlank() } ?: unknown,
        deliveryCity?.takeIf { it.isNotBlank() } ?: unknown,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = routeText,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MatchShipperRow(name: String?, rating: Double?) {
    if (name.isNullOrBlank() && rating == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        if (!name.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = name,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (rating != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = PasabayanColors.Warning,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = stringResource(R.string.trips_detail_match_rating_format, rating),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MatchCodeStateRow(
    title: String,
    state: CodeState,
) {
    val (color, label) = when (state) {
        CodeState.Requested -> PasabayanColors.Warning to stringResource(R.string.trips_code_state_requested)
        is CodeState.Verified -> {
            val verified = stringResource(R.string.trips_code_state_verified)
            val labelText = state.dateText?.takeIf { it.isNotBlank() }
                ?.let { "$verified · $it" }
                ?: verified
            PasabayanColors.Success to labelText
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Text(
            text = title,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun MatchChatPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = PasabayanSpacing.sm, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Outlined.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.trips_detail_match_chat_pill),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun matchStatusBadgeColor(status: MatchStatus): Color = when (status) {
    MatchStatus.CONFIRMED,
    MatchStatus.SHIPPER_ACCEPTED,
    MatchStatus.CARRIER_ACCEPTED -> PasabayanColors.BadgeBlue
    MatchStatus.PICKED_UP -> PasabayanColors.BadgeOrange
    MatchStatus.IN_TRANSIT -> PasabayanColors.BadgePurple
    MatchStatus.DELIVERED -> PasabayanColors.BadgeGreen
    else -> PasabayanColors.BadgeGray
}

