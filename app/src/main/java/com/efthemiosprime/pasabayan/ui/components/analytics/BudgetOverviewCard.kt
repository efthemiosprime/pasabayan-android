package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.analytics.BudgetTracking
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.MetricDisplayCard

/**
 * Card displaying budget overview and tracking information
 */
@Composable
fun BudgetOverviewCard(
    budgetTracking: BudgetTracking,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier
    ) {
        Text(
            text = "Budget Summary",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricDisplayCard(
                title = "Allocated",
                value = "$${String.format("%.0f", budgetTracking.totalBudgetAllocated)}",
                valueColor = Color(0xFF2196F3), // Blue
                modifier = Modifier.weight(1f)
            )
            
            MetricDisplayCard(
                title = "Spent",
                value = "$${String.format("%.0f", budgetTracking.totalAmountSpent)}",
                valueColor = Color(0xFFFF9800), // Orange
                modifier = Modifier.weight(1f)
            )
            
            MetricDisplayCard(
                title = "Saved",
                value = "$${String.format("%.0f", budgetTracking.totalSavings)}",
                valueColor = Color(0xFF4CAF50), // Green
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Overall Efficiency:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${String.format("%.1f", budgetTracking.overallEfficiency)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50) // Green
            )
        }
    }
} 