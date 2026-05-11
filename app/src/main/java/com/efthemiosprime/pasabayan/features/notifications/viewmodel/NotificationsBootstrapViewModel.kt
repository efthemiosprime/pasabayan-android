package com.efthemiosprime.pasabayan.features.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationLifecycleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin VM that re-registers the stored FCM device token after login. The actual orchestration
 * lives in [NotificationLifecycleManager]; this exists only so [MainTabScreen] can launch the
 * coroutine through a `hiltViewModel()` scope.
 */
@HiltViewModel
class NotificationsBootstrapViewModel @Inject constructor(
    private val lifecycleManager: NotificationLifecycleManager,
) : ViewModel() {

    fun registerIfNeeded() {
        viewModelScope.launch {
            lifecycleManager.registerStoredTokenIfAuthenticated()
        }
    }
}
