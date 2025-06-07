package com.efthemiosprime.pasabayan.ui.components.analytics

import androidx.compose.foundation.layout.*
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

/**
 * Card displaying cost optimization recommendations and budget alerts
 */
@Composable
fun CostOptimizationCard(
    optimization: CostOptimization,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
}

/**
 * Individual recommendation item display
 */
@Composable
private fun RecommendationItem(
    recommendation: OptimizationRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Recommendation",
                tint = Color(0xFF4CAF50), // Green
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
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
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (recommendation.priority) {
                                "high" -> Color(0xFFF44336) // Red
                                "medium" -> Color(0xFFFF9800) // Orange
                                else -> Color(0xFF4CAF50) // Green
                            }
                        )
                    ) {
                        Text(
                            text = recommendation.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
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
 * Individual budget alert item display
 */
@Composable
private fun BudgetAlertItem(
    alert: BudgetAlert,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                "high" -> Color(0xFFFFEBEE) // Light red
                "medium" -> Color(0xFFFFF3E0) // Light orange
                else -> Color(0xFFE8F5E8) // Light green
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
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
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
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
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.severity) {
                                "high" -> Color(0xFFF44336) // Red
                                "medium" -> Color(0xFFFF9800) // Orange
                                else -> Color(0xFF4CAF50) // Green
                            }
                        )
                    ) {
                        Text(
                            text = alert.severity.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
} 