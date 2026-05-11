package com.efthemiosprime.pasabayan.features.notifications.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.notifications.components.ActionableItemCard
import com.efthemiosprime.pasabayan.features.notifications.components.NotificationCard
import com.efthemiosprime.pasabayan.features.notifications.components.NotificationEmptyState
import com.efthemiosprime.pasabayan.features.notifications.components.NotificationPendingReviewCard
import com.efthemiosprime.pasabayan.features.notifications.components.NotificationSectionHeader
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.viewmodel.NotificationUiState
import com.efthemiosprime.pasabayan.features.notifications.viewmodel.NotificationViewModel

/**
 * Locally-computed actionable item shown in Section 2 of the notifications screen. Supplied
 * by the host (cross-feature data — bookings/packages/trips/chat unread/verification level).
 */
data class ActionableItem(
    val id: String,
    val type: ActionableItemType,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

/**
 * Pending review entry shown in Section 3.
 */
data class PendingReviewItem(
    val id: Int,
    val subtitle: String,
    val onClick: () -> Unit,
)

/**
 * Spec 08 § "ComprehensiveNotificationsView" — three-section sheet. Sections 2 and 3 are
 * supplied by the host because they aggregate cross-feature state.
 */
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ComprehensiveNotificationsScreen(
    onClose: () -> Unit,
    role: String? = null,
    actionableItems: List<ActionableItem> = emptyList(),
    pendingReviews: List<PendingReviewItem> = emptyList(),
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(role) {
        viewModel.loadNotifications(role = role)
        viewModel.loadUnreadCounts()
    }
    ComprehensiveNotificationsContent(
        state = state,
        actionableItems = actionableItems,
        pendingReviews = pendingReviews,
        onClose = onClose,
        onTap = { viewModel.onNotificationTapped(it) },
        onMarkAsRead = { viewModel.markAsRead(it.id) },
        onMarkAllAsRead = { viewModel.markAllAsRead() },
        onLoadMore = { viewModel.loadNextPage() },
        onSendTest = { viewModel.sendTestNotification() },
        modifier = modifier,
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
internal fun ComprehensiveNotificationsContent(
    state: NotificationUiState,
    actionableItems: List<ActionableItem>,
    pendingReviews: List<PendingReviewItem>,
    onClose: () -> Unit,
    onTap: (PushNotification) -> Unit,
    onMarkAsRead: (PushNotification) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onLoadMore: () -> Unit,
    onSendTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.notifications_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.notifications_action_clear_all),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val notificationsEmpty = state.notifications.isEmpty()
        val actionsEmpty = actionableItems.isEmpty()
        val reviewsEmpty = pendingReviews.isEmpty()
        val globalEmpty = notificationsEmpty && actionsEmpty && reviewsEmpty && !state.isLoading

        if (globalEmpty) {
            NotificationEmptyState(
                title = stringResource(R.string.notifications_empty_all_title),
                description = stringResource(R.string.notifications_empty_all_description),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            return@PScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = PasabayanSpacing.screenPadding),
            contentPadding = PaddingValues(vertical = PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            // -- Section 1: Notifications --
            item {
                NotificationSectionHeader(
                    title = stringResource(R.string.notifications_section_notifications),
                    count = state.unreadCount,
                )
            }
            if (state.unreadCount > 0) {
                item {
                    PButton(
                        text = stringResource(R.string.notifications_action_mark_all_read),
                        onClick = onMarkAllAsRead,
                        style = PButtonStyle.Tertiary,
                    )
                }
            }
            if (notificationsEmpty) {
                item {
                    NotificationEmptyState(
                        title = stringResource(R.string.notifications_empty_unread_title),
                        description = stringResource(R.string.notifications_empty_unread_description),
                    )
                }
            } else {
                items(state.notifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = { onTap(notif) },
                        onMarkAsRead = { onMarkAsRead(notif) },
                    )
                }
                if (state.hasMore) {
                    item {
                        PButton(
                            text = stringResource(R.string.notifications_load_more),
                            onClick = onLoadMore,
                            style = PButtonStyle.Secondary,
                            isLoading = state.isLoadingMore,
                        )
                    }
                }
            }

            // -- Section 2: Action Required --
            item {
                NotificationSectionHeader(
                    title = stringResource(R.string.notifications_section_action_required),
                    count = actionableItems.size,
                )
            }
            if (actionsEmpty) {
                item {
                    NotificationEmptyState(
                        title = stringResource(R.string.notifications_action_empty_title),
                        description = stringResource(R.string.notifications_action_empty_description),
                    )
                }
            } else {
                items(actionableItems, key = { it.id }) { item ->
                    ActionableItemCard(
                        type = item.type,
                        title = item.title,
                        description = item.description,
                        onClick = item.onClick,
                    )
                }
            }

            // -- Section 3: Pending Reviews --
            item {
                NotificationSectionHeader(
                    title = stringResource(R.string.notifications_section_pending_reviews),
                    count = pendingReviews.size,
                )
            }
            if (reviewsEmpty) {
                item {
                    NotificationEmptyState(
                        title = stringResource(R.string.notifications_reviews_empty_title),
                        description = stringResource(R.string.notifications_reviews_empty_description),
                    )
                }
            } else {
                items(pendingReviews, key = { it.id }) { review ->
                    NotificationPendingReviewCard(
                        subtitle = review.subtitle,
                        onClick = review.onClick,
                    )
                }
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Notifications screen — populated light", showBackground = true)
@Preview(
    name = "Notifications screen — populated dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ComprehensiveNotificationsPopulatedPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = NotificationUiState(
                notifications = listOf(
                    PushNotification(
                        id = 1, title = "New Request", body = "Alice wants to ship a package.",
                        type = NotificationType.MATCH_REQUEST, sentAt = "x", createdAt = "x",
                    ),
                    PushNotification(
                        id = 2, title = "Payment Received", body = "You received \$25.50",
                        type = NotificationType.PAYMENT_RECEIVED, sentAt = "x", createdAt = "x",
                        isRead = true,
                    ),
                ),
                unreadCount = 1,
            ),
            actionableItems = listOf(
                ActionableItem(
                    id = "boking-1",
                    type = ActionableItemType.BOOKING_REQUEST,
                    title = "Pending booking request",
                    description = "Alice wants to ship a package",
                    onClick = {},
                ),
            ),
            pendingReviews = listOf(
                PendingReviewItem(id = 1, subtitle = "Delivery #42 — 2 days ago", onClick = {}),
            ),
            onClose = {},
            onTap = {},
            onMarkAsRead = {},
            onMarkAllAsRead = {},
            onLoadMore = {},
            onSendTest = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Notifications screen — empty light", showBackground = true)
@Preview(
    name = "Notifications screen — empty dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ComprehensiveNotificationsEmptyPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = NotificationUiState(),
            actionableItems = emptyList(),
            pendingReviews = emptyList(),
            onClose = {},
            onTap = {},
            onMarkAsRead = {},
            onMarkAllAsRead = {},
            onLoadMore = {},
            onSendTest = {},
        )
    }
}
