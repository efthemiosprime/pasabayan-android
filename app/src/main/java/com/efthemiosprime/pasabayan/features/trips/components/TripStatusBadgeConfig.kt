package com.efthemiosprime.pasabayan.features.trips.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeConfig
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus

/**
 * Maps [TripStatus] to [StatusBadgeConfig] for use with [PStatusBadge].
 * Display text requires a resolved string resource — caller passes it.
 */
data class TripStatusBadgeConfig(
    private val status: TripStatus,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = label

    override val backgroundColor: Color
        get() = when (status) {
            TripStatus.PLANNING -> PasabayanColors.TripScheduled
            TripStatus.ACTIVE -> PasabayanColors.TripActive
            TripStatus.IN_TRANSIT -> PasabayanColors.StatusInTransit
            TripStatus.COMPLETED -> PasabayanColors.TripCompleted
            TripStatus.CANCELLED -> PasabayanColors.TripCancelled
        }

    override val textColor: Color get() = backgroundColor

    override val icon: ImageVector
        get() = when (status) {
            TripStatus.PLANNING -> Icons.Default.Edit
            TripStatus.ACTIVE -> Icons.Default.CheckCircle
            TripStatus.IN_TRANSIT -> Icons.Default.LocalShipping
            TripStatus.COMPLETED -> Icons.Default.Done
            TripStatus.CANCELLED -> Icons.Default.Cancel
        }
}
