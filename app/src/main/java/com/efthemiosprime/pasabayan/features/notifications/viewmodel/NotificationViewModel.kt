package com.efthemiosprime.pasabayan.features.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRepository
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SHIPPER_ROLE = "shipper"
private const val CARRIER_ROLE = "carrier"

/**
 * View-model for the notifications screen (spec 08). Maintains the three unread counts
 * (total + per-role), the notification list with pagination, and the optimistic
 * mark-as-read behavior described in § "Badge count updates".
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val router: NotificationRouter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var currentRole: String? = null

    fun loadNotifications(role: String? = null, page: Int = 1, append: Boolean = false) {
        currentRole = role
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !append,
                    isLoadingMore = append,
                    errorMessage = null,
                )
            }
            repository.fetchNotifications(page = page, role = role).fold(
                onSuccess = { pageData ->
                    _uiState.update {
                        it.copy(
                            notifications = if (append) it.notifications + pageData.notifications else pageData.notifications,
                            pagination = pageData.pagination,
                            isLoading = false,
                            isLoadingMore = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = e.message ?: "Failed to load notifications",
                        )
                    }
                },
            )
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        val nextPage = (state.pagination?.currentPage ?: 1) + 1
        loadNotifications(role = currentRole, page = nextPage, append = true)
    }

    fun loadUnreadCounts() {
        viewModelScope.launch {
            val totalDef = async { repository.getUnreadCount().getOrNull() }
            val carrierDef = async { repository.getUnreadCount(role = CARRIER_ROLE).getOrNull() }
            val shipperDef = async { repository.getUnreadCount(role = SHIPPER_ROLE).getOrNull() }
            awaitAll(totalDef, carrierDef, shipperDef)
            _uiState.update {
                it.copy(
                    unreadCount = totalDef.getCompleted() ?: it.unreadCount,
                    carrierUnreadCount = carrierDef.getCompleted() ?: it.carrierUnreadCount,
                    shipperUnreadCount = shipperDef.getCompleted() ?: it.shipperUnreadCount,
                )
            }
        }
    }

    /**
     * Optimistic mark-as-read: update local state immediately, then call the API. On failure
     * we re-sync role counts from the server (matches iOS behavior — no manual revert).
     */
    fun markAsRead(id: Int) {
        val before = _uiState.value
        val notif = before.notifications.firstOrNull { it.id == id } ?: return
        if (notif.isRead) return
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { n ->
                    if (n.id == id) n.copy(isRead = true, readAt = "now") else n
                },
                unreadCount = (state.unreadCount - 1).coerceAtLeast(0),
                carrierUnreadCount = if (notif.recipientRole == CARRIER_ROLE) {
                    (state.carrierUnreadCount - 1).coerceAtLeast(0)
                } else state.carrierUnreadCount,
                shipperUnreadCount = if (notif.recipientRole == SHIPPER_ROLE) {
                    (state.shipperUnreadCount - 1).coerceAtLeast(0)
                } else state.shipperUnreadCount,
            )
        }
        viewModelScope.launch {
            val result = repository.markAsRead(id)
            if (result.isFailure) {
                loadUnreadCounts()
            }
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) },
                unreadCount = 0,
                carrierUnreadCount = 0,
                shipperUnreadCount = 0,
            )
        }
        viewModelScope.launch {
            val result = repository.markAllAsRead()
            if (result.isFailure) {
                loadUnreadCounts()
            }
        }
    }

    /**
     * In-app card tap. Emits a navigation event through the singleton [NotificationRouter] and
     * marks the notification as read.
     */
    fun onNotificationTapped(notification: PushNotification) {
        router.emitFromInApp(notification)
        if (!notification.isRead) markAsRead(notification.id)
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val result = repository.sendTestNotification()
            _uiState.update {
                it.copy(
                    testNotificationStatus = if (result.isSuccess) {
                        TestNotificationStatus.SENT
                    } else {
                        TestNotificationStatus.FAILED
                    },
                )
            }
        }
    }

    fun clearTestNotificationStatus() {
        _uiState.update { it.copy(testNotificationStatus = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
