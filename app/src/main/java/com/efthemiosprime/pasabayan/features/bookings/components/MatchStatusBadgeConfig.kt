package com.efthemiosprime.pasabayan.features.bookings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeConfig
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus

data class MatchStatusBadgeConfig(
    private val status: MatchStatus,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = label

    override val backgroundColor: Color
        get() = when (status) {
            MatchStatus.PENDING -> PasabayanColors.StatusPending
            MatchStatus.CONFIRMED, MatchStatus.SHIPPER_ACCEPTED, MatchStatus.CARRIER_ACCEPTED -> PasabayanColors.StatusCompleted
            MatchStatus.PICKED_UP -> PasabayanColors.StatusPending
            MatchStatus.IN_TRANSIT -> PasabayanColors.StatusInTransit
            MatchStatus.DELIVERED -> PasabayanColors.StatusCompleted
            MatchStatus.CANCELLED, MatchStatus.SHIPPER_DECLINED, MatchStatus.CARRIER_DECLINED -> PasabayanColors.StatusCancelled
            MatchStatus.CARRIER_REQUESTED -> PasabayanColors.StatusActive
            MatchStatus.SHIPPER_REQUESTED -> PasabayanColors.StatusMatched
        }

    override val textColor: Color get() = backgroundColor

    override val icon: ImageVector
        get() = when (status) {
            MatchStatus.PENDING -> Icons.Default.Schedule
            MatchStatus.CONFIRMED, MatchStatus.SHIPPER_ACCEPTED, MatchStatus.CARRIER_ACCEPTED -> Icons.Default.CheckCircle
            MatchStatus.PICKED_UP -> Icons.Default.LocalShipping
            MatchStatus.IN_TRANSIT -> Icons.Default.LocalShipping
            MatchStatus.DELIVERED -> Icons.Default.Done
            MatchStatus.CANCELLED, MatchStatus.SHIPPER_DECLINED, MatchStatus.CARRIER_DECLINED -> Icons.Default.Cancel
            MatchStatus.CARRIER_REQUESTED, MatchStatus.SHIPPER_REQUESTED -> Icons.Default.SwapHoriz
        }
}
