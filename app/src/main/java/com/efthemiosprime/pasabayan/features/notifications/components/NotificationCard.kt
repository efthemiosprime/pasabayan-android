package com.efthemiosprime.pasabayan.features.notifications.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PUnreadDot
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.model.TimeAgoFormatter

/**
 * Single notification card — colored type-icon + title + body + type pill + timeAgo + unread
 * indicator with a "Mark as Read" affordance. Mirrors iOS `NotificationCard`.
 */
@Composable
fun NotificationCard(
    notification: PushNotification,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier,
    nowMillis: Long = System.currentTimeMillis(),
) {
    val context = LocalContext.current
    val timeAgo = TimeAgoFormatter.format(context, notification.createdAt, nowMillis)
    PCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            TypeIcon(notification.type)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = PasabayanTextStyles.Body.medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (!notification.isRead) {
                        PUnreadDot(color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = notification.body,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    TypePill(notification.type)
                    Text(
                        text = timeAgo,
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!notification.isRead) {
                        Text(
                            text = stringResource(R.string.notifications_action_mark_as_read),
                            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(onClick = onMarkAsRead),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeIcon(type: NotificationType) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(type.color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = stringResource(type.displayNameRes),
            tint = type.color,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun TypePill(type: NotificationType) {
    Box(
        modifier = Modifier
            .background(type.color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(type.displayNameRes),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = type.color,
        )
    }
}

@Preview(name = "NotificationCard light", showBackground = true)
@Preview(name = "NotificationCard dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationCardPreview() {
    PasabayanTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            modifier = Modifier.padding(PasabayanSpacing.md),
        ) {
            NotificationCard(
                notification = PushNotification(
                    id = 1,
                    title = "New Request",
                    body = "Alice wants to ship a package from Toronto to Montreal.",
                    type = NotificationType.MATCH_REQUEST,
                    data = NotificationData(matchId = 1),
                    sentAt = "x",
                    createdAt = "x",
                    isRead = false,
                ),
                onClick = {},
                onMarkAsRead = {},
                nowMillis = 0L,
            )
            NotificationCard(
                notification = PushNotification(
                    id = 2,
                    title = "Payment Received",
                    body = "You received \$25.50 for delivery #42.",
                    type = NotificationType.PAYMENT_RECEIVED,
                    sentAt = "x",
                    createdAt = "x",
                    isRead = true,
                ),
                onClick = {},
                onMarkAsRead = {},
                nowMillis = 0L,
            )
        }
    }
}
