package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles

/**
 * Configuration for [PStatusBadge]. Feature modules create instances mapping
 * their domain enums to display properties.
 */
interface StatusBadgeConfig {
    val displayText: String
    val backgroundColor: Color
    val textColor: Color
    val icon: ImageVector?
        get() = null
}

enum class StatusBadgeVariant {
    /** Colored background + icon + label text. */
    Standard,
    /** Smaller, no icon. */
    Compact,
    /** Just the colored icon. */
    IconOnly,
}

/**
 * Generic status badge — used for TripStatus, PackageRequestStatus, MatchStatus, etc.
 * Feature modules provide [StatusBadgeConfig] implementations for their enums.
 */
@Composable
fun PStatusBadge(
    config: StatusBadgeConfig,
    modifier: Modifier = Modifier,
    variant: StatusBadgeVariant = StatusBadgeVariant.Standard,
) {
    val shape = RoundedCornerShape(PasabayanRadius.badge)

    when (variant) {
        StatusBadgeVariant.Standard -> {
            Row(
                modifier = modifier
                    .background(config.backgroundColor.copy(alpha = 0.15f), shape)
                    .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                config.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = config.backgroundColor,
                    )
                }
                Text(
                    text = config.displayText,
                    style = PasabayanTextStyles.Caption.regular,
                    color = config.backgroundColor,
                )
            }
        }
        StatusBadgeVariant.Compact -> {
            Text(
                text = config.displayText,
                modifier = modifier
                    .background(config.backgroundColor.copy(alpha = 0.15f), shape)
                    .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
                style = PasabayanTextStyles.Caption.small,
                color = config.backgroundColor,
            )
        }
        StatusBadgeVariant.IconOnly -> {
            config.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = config.displayText,
                    modifier = modifier.size(16.dp),
                    tint = config.backgroundColor,
                )
            }
        }
    }
}
