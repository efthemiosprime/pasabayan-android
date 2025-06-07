package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.analytics.RouteAnalytics
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardCompact

/**
 * Card displaying route analytics and performance data
 */
@Composable
fun RouteAnalyticsCard(
    routes: List<RouteAnalytics>,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier
    ) {
        Text(
            text = "Top Routes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (routes.isEmpty()) {
            Text(
                text = "No route data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        } else {
            routes.forEach { route ->
                RouteItem(route = route)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Individual route item display
 */
@Composable
private fun RouteItem(
    route: RouteAnalytics,
    modifier: Modifier = Modifier
) {
    PCardCompact(
        modifier = modifier,
        elevation = 2
    ) {
        Text(
            text = route.route,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${route.totalTrips} trips",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "$${String.format("%.2f", route.avgPricePerKg)}/kg avg",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 