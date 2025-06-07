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
import com.efthemiosprime.pasabayan.ui.common.BadgeData
import com.efthemiosprime.pasabayan.ui.common.PureComposable
import com.efthemiosprime.pasabayan.ui.common.UiUtils
import com.efthemiosprime.pasabayan.ui.common.badgeData
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Pure Badge component following functional composable patterns
 * Phase 4: Refactored to use immutable data structures and pure functions
 * Maintains exact visual compatibility with original TripStatusBadge
 */

// MARK: - Pure Badge Component

/**
 * Pure composable function that takes immutable badge data and renders UI
 * No internal state, no side effects - purely functional
 */
val PureBadge: PureComposable<BadgeData> = { data, modifier ->
    Row(
        modifier = modifier
            .background(
                color = data.backgroundColor,
                shape = RoundedCornerShape(
                    when (data.variant) {
                        BadgeVariant.ICON_ONLY -> 8.dp
                        else -> 12.dp
                    }
                )
            )
            .padding(
                horizontal = when (data.variant) {
                    BadgeVariant.ICON_ONLY -> 6.dp
                    else -> 8.dp
                },
                vertical = when (data.variant) {
                    BadgeVariant.ICON_ONLY -> 4.dp
                    else -> 6.dp
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render icon if present and not icon-only variant
        if (data.variant != BadgeVariant.ICON_ONLY && data.icon != null) {
            BadgeIcon(
                icon = data.icon,
                color = data.textColor,
                variant = data.variant
            )
        }
        
        // Render text for standard and compact variants
        if (data.variant == BadgeVariant.STANDARD || data.variant == BadgeVariant.COMPACT) {
            BadgeText(
                text = data.text,
                color = data.textColor,
                variant = data.variant
            )
        }
    }
}

/**
 * Pure composable for badge icon rendering
 */
@Composable
private fun BadgeIcon(
    icon: String,
    color: Color,
    variant: BadgeVariant,
    modifier: Modifier = Modifier
) {
    Text(
        text = icon,
        style = when (variant) {
            BadgeVariant.COMPACT -> MaterialTheme.typography.labelSmall
            else -> MaterialTheme.typography.bodySmall
        },
        color = color,
        modifier = modifier
    )
}

/**
 * Pure composable for badge text rendering
 */
@Composable
private fun BadgeText(
    text: String,
    color: Color,
    variant: BadgeVariant,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = when (variant) {
            BadgeVariant.COMPACT -> MaterialTheme.typography.labelSmall
            else -> MaterialTheme.typography.bodySmall
        },
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = modifier
    )
}

// MARK: - Higher-Order Badge Components

/**
 * Status badge using functional composition
 */
@Composable
fun StatusBadge(
    status: String,
    variant: BadgeVariant = BadgeVariant.STANDARD,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = UiUtils.calculateBadgeColors(status, variant)
    
    val badgeData = BadgeData(
        text = status.replaceFirstChar { it.uppercase() },
        backgroundColor = backgroundColor.copy(alpha = 0.1f),
        textColor = backgroundColor,
        variant = variant
    )
    
    PureBadge(badgeData, modifier)
}

/**
 * Trip status badge using pure functions (alternative implementation)
 */
@Composable
fun PureTripStatusBadge(
    status: TripStatus,
    variant: BadgeVariant = BadgeVariant.STANDARD,
    modifier: Modifier = Modifier
) {
    val badgeData = createTripStatusBadgeData(status, variant)
    PureBadge(badgeData, modifier)
}

// MARK: - Pure Helper Functions

/**
 * Pure function to create badge data from trip status
 */
fun createTripStatusBadgeData(
    status: TripStatus,
    variant: BadgeVariant = BadgeVariant.STANDARD
): BadgeData {
    val (backgroundColor, textColor) = when (status) {
        TripStatus.SCHEDULED -> Color(0xFF2196F3) to Color.White
        TripStatus.ACTIVE -> Color(0xFF4CAF50) to Color.White
        TripStatus.COMPLETED -> Color(0xFF9E9E9E) to Color.White
        TripStatus.CANCELLED -> Color(0xFFF44336) to Color.White
    }
    
    return BadgeData(
        text = status.displayName,
        icon = if (variant != BadgeVariant.ICON_ONLY) status.icon else null,
        backgroundColor = backgroundColor.copy(alpha = 0.1f),
        textColor = backgroundColor,
        variant = variant
    )
}

/**
 * Pure function to create custom badge data
 */
fun createCustomBadgeData(
    text: String,
    icon: String? = null,
    status: String = "default",
    variant: BadgeVariant = BadgeVariant.STANDARD
): BadgeData {
    val (backgroundColor, textColor) = UiUtils.calculateBadgeColors(status, variant)
    
    return BadgeData(
        text = text,
        icon = icon,
        backgroundColor = backgroundColor.copy(alpha = 0.1f),
        textColor = backgroundColor,
        variant = variant
    )
}

// MARK: - Higher-Order Components

/**
 * Badge list using functional composition
 */
@Composable
fun BadgeList(
    badges: List<BadgeData>,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        badges.forEach { badgeData ->
            PureBadge(badgeData, Modifier)
        }
    }
}

/**
 * Conditional badge rendering
 */
@Composable
fun ConditionalBadge(
    condition: Boolean,
    badgeData: BadgeData,
    modifier: Modifier = Modifier
) {
    if (condition) {
        PureBadge(badgeData, modifier)
    }
}

// MARK: - Extension Functions

/**
 * Convert TripStatus to BadgeData for functional composition
 */
fun TripStatus.toBadgeData(variant: BadgeVariant = BadgeVariant.STANDARD): BadgeData {
    return createTripStatusBadgeData(this, variant)
}

/**
 * Batch convert trip statuses to badge data
 */
fun List<TripStatus>.toBadgeDataList(variant: BadgeVariant = BadgeVariant.STANDARD): List<BadgeData> {
    return map { it.toBadgeData(variant) }
}

// MARK: - Previews

@Preview("Pure Badge - Standard")
@Composable
fun PureBadgeStandardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pure Badges - Standard Variant",
                style = MaterialTheme.typography.headlineSmall
            )
            
            val badges = listOf(
                createTripStatusBadgeData(TripStatus.SCHEDULED),
                createTripStatusBadgeData(TripStatus.ACTIVE),
                createTripStatusBadgeData(TripStatus.COMPLETED),
                createTripStatusBadgeData(TripStatus.CANCELLED)
            )
            
            BadgeList(badges = badges)
        }
    }
}

@Preview("Badge All Variants")
@Composable
fun BadgeAllVariantsPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "All Badge Variants",
                style = MaterialTheme.typography.headlineSmall
            )
            
                         // Standard variant
             Text("Standard", style = MaterialTheme.typography.titleMedium)
             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 TripStatus.values().forEach { status ->
                     PureTripStatusBadge(status, BadgeVariant.STANDARD)
                 }
             }
             
             // Compact variant
             Text("Compact", style = MaterialTheme.typography.titleMedium)
             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 TripStatus.values().forEach { status ->
                     PureTripStatusBadge(status, BadgeVariant.COMPACT)
                 }
             }
             
             // Icon only variant
             Text("Icon Only", style = MaterialTheme.typography.titleMedium)
             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 TripStatus.values().forEach { status ->
                     PureTripStatusBadge(status, BadgeVariant.ICON_ONLY)
                 }
             }
        }
    }
}

@Preview("Custom Badges")
@Composable
fun CustomBadgesPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Custom Pure Badges",
                style = MaterialTheme.typography.headlineSmall
            )
            
            val customBadges = listOf(
                badgeData {
                    text("Priority")
                    icon("⚡")
                    colors(Color(0xFFFF9800).copy(alpha = 0.1f), Color(0xFFFF9800))
                },
                badgeData {
                    text("Express")
                    icon("🚀")
                    colors(Color(0xFF9C27B0).copy(alpha = 0.1f), Color(0xFF9C27B0))
                    variant(BadgeVariant.COMPACT)
                },
                badgeData {
                    text("Fragile")
                    icon("⚠️")
                    colors(Color(0xFFF44336).copy(alpha = 0.1f), Color(0xFFF44336))
                }
            )
            
            BadgeList(badges = customBadges)
        }
    }
} 