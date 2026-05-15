package com.efthemiosprime.pasabayan.features.notifications.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
 * A row in the **Action Required** or **Account Setup** section. Supplied by the host so it can
 * map server counts (`BadgeSummary.actionRequired` / `.verification`) into localized strings and
 * wire tap routing into the surrounding feature stack.
 */
data class ActionableItem(
    val id: String,
    val type: ActionableItemType,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

/**
 * Pending review entry shown in the **Pending Reviews** section.
 */
data class PendingReviewItem(
    val id: Int,
    val subtitle: String,
    val onClick: () -> Unit,
)

/**
 * Four-section attention sheet driven by `BadgeSummary`:
 * 1. Notifications (server push history; always shown, empty state if zero).
 * 2. Action Required (`summary.actionRequired.*` mapped to cards; always shown).
 * 3. Account Setup (`summary.verification.*`; shown only when at least one card exists — this
 *    is the section iOS recently added to fix the "bell shows 2, drawer empty" bug).
 * 4. Pending Reviews (`pendingReviews` from the host; always shown).
 *
 * The dedicated "No notifications" empty state only renders when **every** section is empty —
 * matching iOS `ComprehensiveNotificationsView`.
 */
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ComprehensiveNotificationsScreen(
    onClose: () -> Unit,
    role: String? = null,
    actionableItems: List<ActionableItem> = emptyList(),
    accountSetupItems: List<ActionableItem> = emptyList(),
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
        accountSetupItems = accountSetupItems,
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
    accountSetupItems: List<ActionableItem>,
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
        val accountSetupEmpty = accountSetupItems.isEmpty()
        val reviewsEmpty = pendingReviews.isEmpty()
        val globalEmpty =
            notificationsEmpty && actionsEmpty && accountSetupEmpty && reviewsEmpty && !state.isLoading

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

            // -- Section 3: Account Setup — only when non-empty (iOS parity). --
            if (!accountSetupEmpty) {
                item {
                    NotificationSectionHeader(
                        title = stringResource(R.string.notifications_section_account_setup),
                        count = accountSetupItems.size,
                    )
                }
                items(accountSetupItems, key = { it.id }) { item ->
                    ActionableItemCard(
                        type = item.type,
                        title = item.title,
                        description = item.description,
                        onClick = item.onClick,
                    )
                }
            }

            // -- Section 4: Pending Reviews --
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

private val verifyPhoneCard = ActionableItem(
    id = "verification_phone_needed",
    type = ActionableItemType.VERIFY_PHONE,
    title = "Verify Your Phone",
    description = "Confirm your phone number to unlock booking and messaging features.",
    onClick = {},
)

private val setupPayoutCard = ActionableItem(
    id = "verification_payout_needed",
    type = ActionableItemType.SETUP_PAYOUT,
    title = "Set Up Payouts",
    description = "Add your bank account so you can receive carrier earnings.",
    onClick = {},
)

private val bookingRequestCard = ActionableItem(
    id = "pending_requests_consolidated",
    type = ActionableItemType.BOOKING_REQUEST,
    title = "2 New Booking Requests",
    description = "You have 2 new booking requests from shippers",
    onClick = {},
)

private val pendingReviewCard = PendingReviewItem(
    id = 1,
    subtitle = "Delivery #42 — 2 days ago",
    onClick = {},
)

private fun previewState(notifications: List<PushNotification> = emptyList()) = NotificationUiState(
    notifications = notifications,
    unreadCount = notifications.count { !it.isRead },
)

private val sampleNotifications = listOf(
    PushNotification(
        id = 1, title = "New Request", body = "Alice wants to ship a package.",
        type = NotificationType.MATCH_REQUEST, sentAt = "x", createdAt = "x",
    ),
)

/** Regression preview for the iOS bug this work fixes: bell=2, drawer used to be empty. */
@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Drawer — account-setup-only (regression) light", showBackground = true)
@Preview(
    name = "Drawer — account-setup-only (regression) dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DrawerAccountSetupOnlyPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = previewState(),
            actionableItems = emptyList(),
            accountSetupItems = listOf(verifyPhoneCard, setupPayoutCard),
            pendingReviews = emptyList(),
            onClose = {}, onTap = {}, onMarkAsRead = {}, onMarkAllAsRead = {},
            onLoadMore = {}, onSendTest = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Drawer — action-required-only light", showBackground = true)
@Preview(
    name = "Drawer — action-required-only dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DrawerActionRequiredOnlyPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = previewState(),
            actionableItems = listOf(bookingRequestCard),
            accountSetupItems = emptyList(),
            pendingReviews = emptyList(),
            onClose = {}, onTap = {}, onMarkAsRead = {}, onMarkAllAsRead = {},
            onLoadMore = {}, onSendTest = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Drawer — pending-reviews-only light", showBackground = true)
@Preview(
    name = "Drawer — pending-reviews-only dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DrawerPendingReviewsOnlyPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = previewState(),
            actionableItems = emptyList(),
            accountSetupItems = emptyList(),
            pendingReviews = listOf(pendingReviewCard),
            onClose = {}, onTap = {}, onMarkAsRead = {}, onMarkAllAsRead = {},
            onLoadMore = {}, onSendTest = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Drawer — everything populated light", showBackground = true)
@Preview(
    name = "Drawer — everything populated dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DrawerEverythingPopulatedPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = previewState(notifications = sampleNotifications),
            actionableItems = listOf(bookingRequestCard),
            accountSetupItems = listOf(verifyPhoneCard, setupPayoutCard),
            pendingReviews = listOf(pendingReviewCard),
            onClose = {}, onTap = {}, onMarkAsRead = {}, onMarkAllAsRead = {},
            onLoadMore = {}, onSendTest = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Drawer — all empty light", showBackground = true)
@Preview(
    name = "Drawer — all empty dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DrawerAllEmptyPreview() {
    PasabayanTheme {
        ComprehensiveNotificationsContent(
            state = NotificationUiState(),
            actionableItems = emptyList(),
            accountSetupItems = emptyList(),
            pendingReviews = emptyList(),
            onClose = {}, onTap = {}, onMarkAsRead = {}, onMarkAllAsRead = {},
            onLoadMore = {}, onSendTest = {},
        )
    }
}
