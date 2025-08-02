package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.MatchStatus

/**
 * MatchStatusBadge component for delivery match status display
 * Mirrors iOS MatchStatusBadge functionality with Material 3 design
 * Follows PCard standard patterns for consistent styling
 */
@Composable
fun MatchStatusBadge(
    status: MatchStatus,
    variant: BadgeVariant = BadgeVariant.STANDARD,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = getBackgroundColor(status),
                shape = RoundedCornerShape(
                    when (variant) {
                        BadgeVariant.ICON_ONLY -> 8.dp
                        else -> 12.dp
                    }
                )
            )
            .padding(
                horizontal = when (variant) {
                    BadgeVariant.ICON_ONLY -> 6.dp
                    else -> 8.dp
                },
                vertical = when (variant) {
                    BadgeVariant.ICON_ONLY -> 4.dp
                    else -> 6.dp
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (variant != BadgeVariant.ICON_ONLY) {
            Text(
                text = getIcon(status),
                style = when (variant) {
                    BadgeVariant.COMPACT -> MaterialTheme.typography.labelSmall
                    else -> MaterialTheme.typography.bodySmall
                },
                color = getTextColor(status)
            )
        }
        
        if (variant == BadgeVariant.STANDARD || variant == BadgeVariant.COMPACT) {
            Text(
                text = status.description,
                style = when (variant) {
                    BadgeVariant.COMPACT -> MaterialTheme.typography.labelSmall
                    else -> MaterialTheme.typography.bodySmall
                },
                fontWeight = FontWeight.Medium,
                color = getTextColor(status)
            )
        }
    }
}

/**
 * Get background color for match status following Material 3 color scheme
 */
@Composable
private fun getBackgroundColor(status: MatchStatus): Color {
    return when (status) {
        MatchStatus.PENDING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        MatchStatus.PICKED_UP -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        MatchStatus.IN_TRANSIT -> Color(0xFF4CAF50).copy(alpha = 0.12f)
        MatchStatus.DELIVERED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        MatchStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    }
}

/**
 * Get text color for match status
 */
@Composable
private fun getTextColor(status: MatchStatus): Color {
    return when (status) {
        MatchStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
        MatchStatus.PICKED_UP -> MaterialTheme.colorScheme.secondary
        MatchStatus.IN_TRANSIT -> Color(0xFF4CAF50)
        MatchStatus.DELIVERED -> MaterialTheme.colorScheme.outline
        MatchStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
}

/**
 * Get icon for match status
 */
private fun getIcon(status: MatchStatus): String {
    return when (status) {
        MatchStatus.PENDING -> "⏳"
        MatchStatus.CONFIRMED -> "✅"
        MatchStatus.PICKED_UP -> "📦"
        MatchStatus.IN_TRANSIT -> "🚛"
        MatchStatus.DELIVERED -> "✅"
        MatchStatus.CANCELLED -> "❌"
    }
}