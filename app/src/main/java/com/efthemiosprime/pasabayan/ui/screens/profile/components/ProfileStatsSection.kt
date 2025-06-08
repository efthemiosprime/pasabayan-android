package com.efthemiosprime.pasabayan.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.ui.components.cards.StatItem
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Profile Stats Section Component - Following Global Card Standards
 * Displays role-specific statistics using consistent elevation and styling
 * Uses StatItem (no elevation) inside parent card to prevent nested shadows
 */
@Composable
fun ProfileStatsSection(
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card),
        elevation = CardDefaults.cardElevation(
            defaultElevation = PasabayanDesignSystem.CardStandards.elevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = PasabayanDesignSystem.CardStandards.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(PasabayanDesignSystem.CardStandards.padding),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            ProfileStatsGrid(currentRole = currentRole)
        }
    }
}

@Composable
private fun ProfileStatsGrid(
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val stats = remember(currentRole) {
        if (currentRole == UserRole.CARRIER) {
            carrierStats
        } else {
            shipperStats
        }
    }
    
    // Use Row-Column layout instead of LazyVerticalGrid to avoid nested scrolling
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            StatItem( // Using StatItem (no elevation) instead of StatCard
                title = stats[0].title,
                value = stats[0].value,
                icon = stats[0].icon,
                color = stats[0].color,
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (no elevation) instead of StatCard
                title = stats[1].title,
                value = stats[1].value,
                icon = stats[1].icon,
                color = stats[1].color,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row (centered single item)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            StatItem( // Using StatItem (no elevation) instead of StatCard
                title = stats[2].title,
                value = stats[2].value,
                icon = stats[2].icon,
                color = stats[2].color,
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

// Configuration-based stats data
private data class StatItemData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

private val carrierStats = listOf(
    StatItemData("Deliveries", "147", Icons.Default.LocalShipping, Color.Blue),
    StatItemData("Rating", "4.9", Icons.Default.Star, Color(0xFFFFD700)),
    StatItemData("Earnings", "$298.80", Icons.Default.AttachMoney, Color(0xFF4CAF50))
)

private val shipperStats = listOf(
    StatItemData("Packages", "28", Icons.Default.Inventory, Color(0xFFE91E63)),
    StatItemData("Rating", "4.7", Icons.Default.Star, Color(0xFFFFC107)),
    StatItemData("Monthly Spent", "$76.80", Icons.Default.CreditCard, Color.Blue)
) 