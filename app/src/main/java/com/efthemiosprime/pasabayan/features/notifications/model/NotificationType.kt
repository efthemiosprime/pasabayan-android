package com.efthemiosprime.pasabayan.features.notifications.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors

/**
 * iOS parity: `NotificationType` in `NotificationModels.swift`.
 *
 * Each case carries the Material Icon and [PasabayanColors] accent shown on the notification
 * card, plus a `@StringRes` localized display name. Unknown wire values fall back to [UNKNOWN].
 */
enum class NotificationType(
    val rawValue: String,
    val icon: ImageVector,
    val color: Color,
    @StringRes val displayNameRes: Int,
) {
    MATCH_REQUEST(
        rawValue = "match_request",
        icon = Icons.Filled.Inventory2,
        color = PasabayanColors.BadgeOrange,
        displayNameRes = R.string.notifications_type_match_request,
    ),
    MATCH_ACCEPTED(
        rawValue = "match_accepted",
        icon = Icons.Filled.CheckCircle,
        color = PasabayanColors.Success,
        displayNameRes = R.string.notifications_type_match_accepted,
    ),
    MATCH_DECLINED(
        rawValue = "match_declined",
        icon = Icons.Filled.Cancel,
        color = PasabayanColors.Error,
        displayNameRes = R.string.notifications_type_match_declined,
    ),
    CHAT_MESSAGE(
        rawValue = "chat_message",
        icon = Icons.Filled.Chat,
        color = PasabayanColors.Info,
        displayNameRes = R.string.notifications_type_chat_message,
    ),
    DELIVERY_PICKED_UP(
        rawValue = "delivery_picked_up",
        icon = Icons.Filled.LocalShipping,
        color = PasabayanColors.BadgePurple,
        displayNameRes = R.string.notifications_type_delivery_picked_up,
    ),
    DELIVERY_IN_TRANSIT(
        rawValue = "delivery_in_transit",
        icon = Icons.Filled.DirectionsCar,
        color = PasabayanColors.BadgePurple,
        displayNameRes = R.string.notifications_type_delivery_in_transit,
    ),
    DELIVERY_DELIVERED(
        rawValue = "delivery_delivered",
        icon = Icons.Filled.VerifiedUser,
        color = PasabayanColors.BadgePurple,
        displayNameRes = R.string.notifications_type_delivery_delivered,
    ),
    MATCH_AUTO_CANCELLED(
        rawValue = "match_auto_cancelled",
        icon = Icons.Filled.PauseCircle,
        color = PasabayanColors.BadgeOrange,
        displayNameRes = R.string.notifications_type_match_auto_cancelled,
    ),
    PAYMENT_RECEIVED(
        rawValue = "payment_received",
        icon = Icons.Filled.CreditCard,
        color = PasabayanColors.Success,
        displayNameRes = R.string.notifications_type_payment_received,
    ),
    PAYMENT_RELEASED(
        rawValue = "payment_released",
        icon = Icons.Filled.MonetizationOn,
        color = PasabayanColors.Success,
        displayNameRes = R.string.notifications_type_payment_released,
    ),
    RATING_RECEIVED(
        rawValue = "rating_received",
        icon = Icons.Filled.Star,
        color = PasabayanColors.BadgeGold,
        displayNameRes = R.string.notifications_type_rating_received,
    ),
    PREMIUM_APPROVED(
        rawValue = "premium_approved",
        icon = Icons.Filled.VerifiedUser,
        color = PasabayanColors.Success,
        displayNameRes = R.string.notifications_type_premium_approved,
    ),
    PREMIUM_REJECTED(
        rawValue = "premium_rejected",
        icon = Icons.Filled.Shield,
        color = PasabayanColors.Error,
        displayNameRes = R.string.notifications_type_premium_rejected,
    ),
    TEST(
        rawValue = "test",
        icon = Icons.Filled.Notifications,
        color = PasabayanColors.BadgeGray,
        displayNameRes = R.string.notifications_type_test,
    ),
    PAYOUT_COMPLETED(
        rawValue = "payout_completed",
        icon = Icons.Filled.AccountBalance,
        color = PasabayanColors.Success,
        displayNameRes = R.string.notifications_type_payout_completed,
    ),
    PAYOUT_SCHEDULED(
        rawValue = "payout_scheduled",
        icon = Icons.Filled.CalendarMonth,
        color = PasabayanColors.BadgeOrange,
        displayNameRes = R.string.notifications_type_payout_scheduled,
    ),
    REFUND_PROCESSED(
        rawValue = "refund_processed",
        icon = Icons.Filled.Replay,
        color = PasabayanColors.Info,
        displayNameRes = R.string.notifications_type_refund_processed,
    ),
    TIP_RECEIVED(
        rawValue = "tip_received",
        icon = Icons.Filled.CardGiftcard,
        color = PasabayanColors.BadgeOrange,
        displayNameRes = R.string.notifications_type_tip_received,
    ),
    PAYMENT_AUTO_CHARGE_FAILED(
        rawValue = "payment_auto_charge_failed",
        icon = Icons.Filled.Warning,
        color = PasabayanColors.Error,
        displayNameRes = R.string.notifications_type_payment_auto_charge_failed,
    ),
    COUNTER_OFFER(
        rawValue = "counter_offer",
        icon = Icons.Filled.SwapHoriz,
        color = PasabayanColors.BadgePurple,
        displayNameRes = R.string.notifications_type_counter_offer,
    ),
    UNKNOWN(
        rawValue = "",
        icon = Icons.Filled.Notifications,
        color = PasabayanColors.BadgeGray,
        displayNameRes = R.string.notifications_type_unknown,
    );

    companion object {
        private val byRaw: Map<String, NotificationType> by lazy {
            entries.associateBy { it.rawValue }.filterKeys { it.isNotEmpty() }
        }

        fun fromRaw(raw: String?): NotificationType =
            raw?.let { byRaw[it] } ?: UNKNOWN
    }
}
