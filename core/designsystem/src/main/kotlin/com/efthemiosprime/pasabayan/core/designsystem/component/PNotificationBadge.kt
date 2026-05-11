package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Compact unread-count badge. Hidden when [count] is `<= 0`. Mirrors iOS `NotificationBadge`.
 *
 * @param maxDisplay caps the rendered value (e.g. `99+` for counts above 99).
 */
@Composable
fun PNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxDisplay: Int = 99,
    backgroundColor: Color = PasabayanColors.Error,
    contentColor: Color = Color.White,
) {
    if (count <= 0) return
    val label = if (count > maxDisplay) "$maxDisplay+" else count.toString()
    Box(
        modifier = modifier
            .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.small.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            modifier = Modifier.defaultMinSize(minHeight = 12.dp),
        )
    }
}

/** Minimal dot variant for "unread" hint without a count. */
@Composable
fun PUnreadDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color, CircleShape),
    )
}

@Preview(name = "Badge counts", showBackground = true)
@Composable
private fun PNotificationBadgePreview() {
    PasabayanTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            PNotificationBadge(count = 1)
            PNotificationBadge(count = 5)
            PNotificationBadge(count = 42)
            PNotificationBadge(count = 250)
            PUnreadDot()
        }
    }
}
