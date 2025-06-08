package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.analytics.CostOptimization
import com.efthemiosprime.pasabayan.data.model.analytics.OptimizationRecommendation
import com.efthemiosprime.pasabayan.data.model.analytics.BudgetAlert
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardCompact
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardElevation
import com.efthemiosprime.pasabayan.ui.shared.PButtonSmall

/**
 * Card displaying cost optimization recommendations and budget alerts
 */
@Composable
fun CostOptimizationCard(
    optimization: CostOptimization,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier
    ) {
        Text(
            text = "Cost Optimization",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommendations Section
        if (optimization.recommendations.isNotEmpty()) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            optimization.recommendations.forEach { recommendation ->
                RecommendationItem(recommendation = recommendation)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Budget Alerts Section
        if (optimization.budgetAlerts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Budget Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            optimization.budgetAlerts.forEach { alert ->
                BudgetAlertItem(alert = alert)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        if (optimization.recommendations.isEmpty() && optimization.budgetAlerts.isEmpty()) {
            Text(
                text = "No optimization data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

/**
 * Individual recommendation item display - Flat version (no elevation)
 */
@Composable
private fun RecommendationItem(
    recommendation: OptimizationRecommendation,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Recommendation",
                tint = Color(0xFF4CAF50), // Green
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = recommendation.suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    PButtonSmall(
                        text = recommendation.priority.uppercase(),
                        onClick = { /* Handle priority action */ },
                        backgroundColor = when (recommendation.priority) {
                            "high" -> Color(0xFFF44336) // Red
                            "medium" -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFF4CAF50) // Green
                        },
                        textColor = Color.White
                    )
                }
                
                Text(
                    text = "Potential savings: ${recommendation.potentialSavings}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50), // Green
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Individual budget alert item display - Flat version (no elevation)
 */
@Composable
private fun BudgetAlertItem(
    alert: BudgetAlert,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (alert.severity) {
        "high" -> Color(0xFFFFEBEE) // Light red
        "medium" -> Color(0xFFFFF3E0) // Light orange
        else -> Color(0xFFE8F5E8) // Light green
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                tint = when (alert.severity) {
                    "high" -> Color(0xFFF44336) // Red
                    "medium" -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFF4CAF50) // Green
                },
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    PButtonSmall(
                        text = alert.severity.uppercase(),
                        onClick = { /* Handle alert action */ },
                        backgroundColor = when (alert.severity) {
                            "high" -> Color(0xFFF44336) // Red
                            "medium" -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFF4CAF50) // Green
                        },
                        textColor = Color.White
                    )
                }
            }
        }
    }
} 