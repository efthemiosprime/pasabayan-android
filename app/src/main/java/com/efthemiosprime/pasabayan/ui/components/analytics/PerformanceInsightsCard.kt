package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardCompact

/**
 * Card displaying performance insights and recommendations
 */
@Composable
fun PerformanceInsightsCard(
    insights: List<String>,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier
    ) {
        Text(
            text = "Performance Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (insights.isEmpty()) {
            Text(
                text = "No insights available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        } else {
            insights.forEach { insight ->
                InsightItem(insight = insight)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Individual insight item display
 */
@Composable
private fun InsightItem(
    insight: String,
    modifier: Modifier = Modifier
) {
    PCardCompact(
        modifier = modifier,
        elevation = 2
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Insight",
                tint = Color(0xFFFF9800), // Orange
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
} 