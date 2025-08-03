package com.efthemiosprime.pasabayan.ui.components.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Status chip component for package/trip status
 * Provides consistent status visualization across the app
 * Uses the new enum display properties from synchronized models
 * NOW LEVERAGES: status.displayName, status.color from the updated PackageRequestStatus enum
 */
@Composable
fun StatusChip(
    status: PackageRequestStatus,
    modifier: Modifier = Modifier
) {
    // Parse hex color from enum (iOS style: "#FF5722" -> Color)
    fun parseColor(colorString: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            // Fallback colors for existing statuses
            when (status) {
                PackageRequestStatus.OPEN -> Color(0xFF1565C0)
                PackageRequestStatus.PENDING_REQUEST -> Color(0xFFE65100)
                PackageRequestStatus.MATCHED -> Color(0xFF1565C0)
                PackageRequestStatus.DELIVERED -> Color(0xFF388E3C)
                PackageRequestStatus.CANCELLED -> Color(0xFFD32F2F)
                PackageRequestStatus.PENDING -> Color(0xFFE65100)
                PackageRequestStatus.BOOKED -> Color(0xFF1565C0)
                PackageRequestStatus.IN_TRANSIT -> Color(0xFFF57F17)
            }
        }
    }
    
    val primaryColor = parseColor(status.color)
    val backgroundColor = primaryColor.copy(alpha = 0.12f)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName, // ✅ NOW USING ENUM PROPERTY!
            style = MaterialTheme.typography.labelSmall,
            color = primaryColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// MARK: - Previews
@Preview("Status Chips")
@Composable
fun StatusChipsPreview() {
    PasabayanTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(status = PackageRequestStatus.OPEN)
            StatusChip(status = PackageRequestStatus.PENDING_REQUEST)
            StatusChip(status = PackageRequestStatus.MATCHED)
            StatusChip(status = PackageRequestStatus.DELIVERED)
            StatusChip(status = PackageRequestStatus.CANCELLED)
            StatusChip(status = PackageRequestStatus.PENDING)
            StatusChip(status = PackageRequestStatus.BOOKED)
            StatusChip(status = PackageRequestStatus.IN_TRANSIT)
        }
    }
} 