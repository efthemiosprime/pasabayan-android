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
 * Matches iOS status chip styling and behavior
 * EXACTLY preserves original functionality
 */
@Composable
fun StatusChip(
    status: PackageRequestStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PackageRequestStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PackageRequestStatus.OPEN -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        PackageRequestStatus.MATCHED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        PackageRequestStatus.BOOKED -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        PackageRequestStatus.IN_TRANSIT -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        PackageRequestStatus.DELIVERED -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        PackageRequestStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
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
            StatusChip(status = PackageRequestStatus.PENDING)
            StatusChip(status = PackageRequestStatus.MATCHED)
            StatusChip(status = PackageRequestStatus.BOOKED)
            StatusChip(status = PackageRequestStatus.IN_TRANSIT)
            StatusChip(status = PackageRequestStatus.DELIVERED)
            StatusChip(status = PackageRequestStatus.CANCELLED)
        }
    }
} 