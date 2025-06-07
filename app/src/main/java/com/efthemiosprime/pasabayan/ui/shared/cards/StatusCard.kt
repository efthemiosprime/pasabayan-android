package com.efthemiosprime.pasabayan.ui.shared.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * StatusCard - For displaying status indicators
 * Equivalent to Swift's StatusCard
 */
@Composable
fun StatusCard(
    title: String,
    status: String,
    statusType: StatusType = StatusType.INFO,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (statusType) {
        StatusType.SUCCESS -> Triple(
            Color(0xFFE8F5E8), // Light green
            Color(0xFF4CAF50), // Green
            Icons.Default.CheckCircle
        )
        StatusType.WARNING -> Triple(
            Color(0xFFFFF3E0), // Light orange
            Color(0xFFFF9800), // Orange
            Icons.Default.Warning
        )
        StatusType.ERROR -> Triple(
            Color(0xFFFFEBEE), // Light red
            Color(0xFFF44336), // Red
            Icons.Default.Error
        )
        StatusType.INFO -> Triple(
            Color(0xFFE3F2FD), // Light blue
            Color(0xFF2196F3), // Blue
            Icons.Default.Info
        )
    }
    
    PCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = 4
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = statusType.name,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Status types for StatusCard
 */
enum class StatusType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
} 