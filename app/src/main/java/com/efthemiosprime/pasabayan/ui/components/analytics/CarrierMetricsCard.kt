package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.analytics.CarrierHistoricalStats
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.MetricDisplayCard

/**
 * Card displaying key carrier performance metrics
 */
@Composable
fun CarrierMetricsCard(
    stats: CarrierHistoricalStats,
    modifier: Modifier = Modifier
) {
    val totalEarnings = stats.monthlyPerformance.sumOf { it.earnings }
    val totalDeliveries = stats.monthlyPerformance.sumOf { it.deliveriesCompleted }
    val averageSuccessRate = stats.monthlyPerformance.map { it.successRate }.average()
    
    PCard(
        modifier = modifier
    ) {
        Text(
            text = "Key Metrics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricDisplayCard(
                title = "Total Earnings",
                value = "$${String.format("%.2f", totalEarnings)}",
                valueColor = Color(0xFF4CAF50), // Green
                modifier = Modifier.weight(1f)
            )
            
            MetricDisplayCard(
                title = "Deliveries",
                value = totalDeliveries.toString(),
                valueColor = Color(0xFF2196F3), // Blue
                modifier = Modifier.weight(1f)
            )
            
            MetricDisplayCard(
                title = "Avg Success Rate",
                value = "${String.format("%.1f", averageSuccessRate)}%",
                valueColor = Color(0xFFFF9800), // Orange
                modifier = Modifier.weight(1f)
            )
        }
    }
} 