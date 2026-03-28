package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeConfig
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus

data class PackageStatusBadgeConfig(
    private val status: PackageRequestStatus,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = label

    override val backgroundColor: Color
        get() = when (status) {
            PackageRequestStatus.OPEN -> PasabayanColors.PackageOpen
            PackageRequestStatus.PENDING_REQUEST -> PasabayanColors.StatusPending
            PackageRequestStatus.MATCHED -> PasabayanColors.PackageMatched
            PackageRequestStatus.PICKED_UP -> PasabayanColors.StatusPending
            PackageRequestStatus.DELIVERED -> PasabayanColors.PackageDelivered
            PackageRequestStatus.CANCELLED -> PasabayanColors.StatusCancelled
            PackageRequestStatus.PENDING -> PasabayanColors.StatusPending
            PackageRequestStatus.BOOKED -> PasabayanColors.PackageBooked
            PackageRequestStatus.IN_TRANSIT -> PasabayanColors.PackageInTransit
        }

    override val textColor: Color get() = backgroundColor

    override val icon: ImageVector
        get() = when (status) {
            PackageRequestStatus.OPEN -> Icons.Default.Inventory2
            PackageRequestStatus.PENDING_REQUEST -> Icons.Default.Schedule
            PackageRequestStatus.MATCHED -> Icons.Default.CheckCircle
            PackageRequestStatus.PICKED_UP -> Icons.Default.LocalShipping
            PackageRequestStatus.DELIVERED -> Icons.Default.Done
            PackageRequestStatus.CANCELLED -> Icons.Default.Cancel
            PackageRequestStatus.PENDING -> Icons.Default.Schedule
            PackageRequestStatus.BOOKED -> Icons.Default.CheckCircle
            PackageRequestStatus.IN_TRANSIT -> Icons.Default.LocalShipping
        }
}
