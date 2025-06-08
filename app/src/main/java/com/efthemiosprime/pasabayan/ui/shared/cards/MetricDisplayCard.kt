package com.efthemiosprime.pasabayan.ui.shared.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * MetricDisplayCard - For displaying metrics with optional icons
 * Following Global Card Standards - flat version for use inside analytics cards
 * Equivalent to Swift's MetricDisplayCard
 */
@Composable
fun MetricDisplayCard(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    isNested: Boolean = true // Default to flat for analytics usage
) {
    if (isNested) {
        // Flat version for nested usage inside parent cards (analytics)
        Column(
            modifier = modifier.padding(PasabayanDesignSystem.Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
        ) {
            // Icon (optional)
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Card version for standalone usage
        PCardCompact(
            modifier = modifier,
            elevation = PCardElevation.Small
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                // Icon (optional)
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Value
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    textAlign = TextAlign.Center
                )
                
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * MetricDisplayCardStandalone - Standalone version with elevation for non-nested usage
 */
@Composable
fun MetricDisplayCardStandalone(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    MetricDisplayCard(
        title = title,
        value = value,
        valueColor = valueColor,
        icon = icon,
        iconColor = iconColor,
        modifier = modifier,
        isNested = false
    )
}

/**
 * Horizontal MetricDisplayCard variant - follows same pattern
 */
@Composable
fun MetricDisplayCardHorizontal(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    isNested: Boolean = true
) {
    if (isNested) {
        // Flat version for nested usage
        Row(
            modifier = modifier.padding(PasabayanDesignSystem.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            // Icon (optional)
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // Card version for standalone usage
        PCardCompact(
            modifier = modifier,
            elevation = PCardElevation.Small
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                // Icon (optional)
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = valueColor
                    )
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 