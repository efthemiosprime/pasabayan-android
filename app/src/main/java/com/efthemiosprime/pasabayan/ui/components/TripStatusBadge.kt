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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * TripStatusBadge component exactly matching iOS TripStatusBadge.swift
 * Displays trip status with different variants
 */
@Composable
fun TripStatusBadge(
    status: TripStatus,
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
                text = status.icon,
                style = when (variant) {
                    BadgeVariant.COMPACT -> MaterialTheme.typography.labelSmall
                    else -> MaterialTheme.typography.bodySmall
                },
                color = getTextColor(status)
            )
        }
        
        if (variant == BadgeVariant.STANDARD || variant == BadgeVariant.COMPACT) {
            Text(
                text = status.displayName,
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
 * Badge variant enum matching iOS implementation
 */
enum class BadgeVariant {
    STANDARD,
    COMPACT,
    ICON_ONLY
}

/**
 * Get background color for trip status
 */
@Composable
private fun getBackgroundColor(status: TripStatus): Color {
    return when (status) {
        TripStatus.PLANNING -> Color(0xFFFF9800).copy(alpha = 0.1f) // Orange
        TripStatus.SCHEDULED -> Color(0xFF2196F3).copy(alpha = 0.1f) // Blue
        TripStatus.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Green
        TripStatus.COMPLETED -> Color(0xFF9E9E9E).copy(alpha = 0.1f) // Gray
        TripStatus.CANCELLED -> Color(0xFFF44336).copy(alpha = 0.1f) // Red
    }
}

/**
 * Get text color for trip status
 */
@Composable
private fun getTextColor(status: TripStatus): Color {
    return when (status) {
        TripStatus.PLANNING -> Color(0xFFFF9800) // Orange
        TripStatus.SCHEDULED -> Color(0xFF2196F3) // Blue
        TripStatus.ACTIVE -> Color(0xFF4CAF50) // Green
        TripStatus.COMPLETED -> Color(0xFF9E9E9E) // Gray
        TripStatus.CANCELLED -> Color(0xFFF44336) // Red
    }
}

// MARK: - Preview
@Preview(showBackground = true, name = "Trip Status Badge - Standard Variant")
@Composable
fun TripStatusBadgeStandardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Standard Variant",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripStatusBadge(TripStatus.SCHEDULED)
                TripStatusBadge(TripStatus.ACTIVE)
                TripStatusBadge(TripStatus.COMPLETED)
                TripStatusBadge(TripStatus.CANCELLED)
            }
        }
    }
}

@Preview(showBackground = true, name = "Trip Status Badge - Compact Variant")
@Composable
fun TripStatusBadgeCompactPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Compact Variant",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripStatusBadge(TripStatus.SCHEDULED, BadgeVariant.COMPACT)
                TripStatusBadge(TripStatus.ACTIVE, BadgeVariant.COMPACT)
                TripStatusBadge(TripStatus.COMPLETED, BadgeVariant.COMPACT)
                TripStatusBadge(TripStatus.CANCELLED, BadgeVariant.COMPACT)
            }
        }
    }
}

@Preview(showBackground = true, name = "Trip Status Badge - Icon Only Variant")
@Composable
fun TripStatusBadgeIconOnlyPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Icon Only Variant",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripStatusBadge(TripStatus.SCHEDULED, BadgeVariant.ICON_ONLY)
                TripStatusBadge(TripStatus.ACTIVE, BadgeVariant.ICON_ONLY)
                TripStatusBadge(TripStatus.COMPLETED, BadgeVariant.ICON_ONLY)
                TripStatusBadge(TripStatus.CANCELLED, BadgeVariant.ICON_ONLY)
            }
        }
    }
}

@Preview(showBackground = true, name = "Trip Status Badge - All Variants")
@Composable
fun TripStatusBadgeAllVariantsPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Standard Variant",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripStatusBadge(TripStatus.SCHEDULED)
                    TripStatusBadge(TripStatus.ACTIVE)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Compact Variant",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripStatusBadge(TripStatus.COMPLETED, BadgeVariant.COMPACT)
                    TripStatusBadge(TripStatus.CANCELLED, BadgeVariant.COMPACT)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Icon Only Variant",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripStatusBadge(TripStatus.SCHEDULED, BadgeVariant.ICON_ONLY)
                    TripStatusBadge(TripStatus.ACTIVE, BadgeVariant.ICON_ONLY)
                    TripStatusBadge(TripStatus.COMPLETED, BadgeVariant.ICON_ONLY)
                    TripStatusBadge(TripStatus.CANCELLED, BadgeVariant.ICON_ONLY)
                }
            }
        }
    }
} 