package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.data.model.DeliveryStatus

@Composable
fun StatusChip(
    status: DeliveryStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        DeliveryStatus.ACCEPTED -> Color(0xFFE3F2FD)
        DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT -> Color(0xFFFFF3E0)
        DeliveryStatus.DELIVERED -> Color(0xFFE8F5E8)
        DeliveryStatus.CANCELLED -> Color(0xFFFFEBEE)
    }

    val textColor = when (status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        DeliveryStatus.ACCEPTED -> Color(0xFF1976D2)
        DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT -> Color(0xFFE65100)
        DeliveryStatus.DELIVERED -> Color(0xFF2E7D32)
        DeliveryStatus.CANCELLED -> Color(0xFFC62828)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusChipPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(DeliveryStatus.PENDING)
            StatusChip(DeliveryStatus.ACCEPTED)
            StatusChip(DeliveryStatus.PICKED_UP)
            StatusChip(DeliveryStatus.DELIVERED)
            StatusChip(DeliveryStatus.CANCELLED)
        }
    }
} 