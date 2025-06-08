package com.efthemiosprime.pasabayan.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * StatCard component following Global Card Standards
 * Displays statistics with icon, value, and title
 * Used across dashboard for various metrics display
 * 
 * Note: When used inside other cards (like ProfileStatsSection),
 * it automatically removes elevation to prevent nested shadows
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isChildCard: Boolean = false // When true, removes elevation for nested usage
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isChildCard) {
                PasabayanDesignSystem.CardStandards.childElevation
            } else {
                PasabayanDesignSystem.CardStandards.elevation
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = PasabayanDesignSystem.CardStandards.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(PasabayanDesignSystem.CardStandards.padding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xs)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * StatItem - Completely flat version for use inside cards (NO elevation/shadows)
 * Uses Surface instead of Card to ensure no elevation at all
 */
@Composable
fun StatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shadowElevation = 0.dp, // Explicitly no elevation
        tonalElevation = 0.dp   // Explicitly no tonal elevation
    ) {
        Column(
            modifier = Modifier
                .padding(PasabayanDesignSystem.Spacing.lg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xs)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// MARK: - Previews
@Preview("Default Stat Card - Standalone")
@Composable
fun StatCardStandalonePreview() {
    PasabayanTheme {
        StatCard(
            title = "Total Earnings",
            value = "$248.20",
            icon = Icons.Default.Star,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview("Stat Card - Child (No Elevation)")
@Composable
fun StatCardChildPreview() {
    PasabayanTheme {
        StatItem(
            title = "Total Earnings",
            value = "$248.20",
            icon = Icons.Default.Star,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(16.dp)
        )
    }
} 