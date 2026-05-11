package com.efthemiosprime.pasabayan.features.notifications.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors

/**
 * Locally-computed actionable item types shown in the **Action Required** section of the
 * notifications screen (see spec section "Section 2"). Distinct from [NotificationType],
 * which represents server-pushed events.
 */
enum class ActionableItemType(
    val icon: ImageVector,
    val color: Color,
) {
    BOOKING_REQUEST(Icons.Filled.Description, PasabayanColors.BadgeOrange),
    STATUS_UPDATE(Icons.Filled.Refresh, PasabayanColors.Info),
    PACKAGE_REQUEST(Icons.Filled.Inventory2, PasabayanColors.Success),
    CARRIER_RESPONSE(Icons.Filled.Person, PasabayanColors.BadgePurple),
    PICKUP_READY(Icons.Filled.Inventory2, PasabayanColors.Info),
    UNREAD_MESSAGES(Icons.Filled.Forum, PasabayanColors.Info),
    INACTIVE_TRIPS(Icons.Filled.Warning, PasabayanColors.BadgeOrange),
    UPGRADE(Icons.Filled.Star, PasabayanColors.BadgeGold),
}
