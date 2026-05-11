package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the FCM lifecycle described in spec 08 § "Device token lifecycle":
 *  - `onTokenRefreshed(token)` — store; register immediately if authenticated.
 *  - `registerStoredTokenIfAuthenticated()` — call post-login.
 *  - `unregisterAndClear()` — call before signing out.
 */
interface NotificationLifecycleManager {
    /** Called from `FirebaseMessagingService.onNewToken`. Fires registration async if authed. */
    fun onTokenRefreshed(token: String)

    /** Called post-login. Returns the outcome, or `null` if no stored token / unauthenticated. */
    suspend fun registerStoredTokenIfAuthenticated(): DeviceTokenRegisterResult?

    /** Called pre-logout. DELETEs the token on the server, then clears local storage. */
    suspend fun unregisterAndClear(): Result<Unit>
}

@Singleton
class DefaultNotificationLifecycleManager @Inject constructor(
    private val repository: NotificationRepository,
    private val tokenStore: FCMTokenStore,
    private val authTokenProvider: AuthTokenProvider,
    private val appScope: NotificationAppScope,
) : NotificationLifecycleManager {

    override fun onTokenRefreshed(token: String) {
        tokenStore.setToken(token)
        if (!isAuthenticated()) return
        appScope.scope.launch {
            repository.registerDeviceToken(token)
        }
    }

    override suspend fun registerStoredTokenIfAuthenticated(): DeviceTokenRegisterResult? {
        if (!isAuthenticated()) return null
        val token = tokenStore.getToken()?.takeIf { it.isNotBlank() } ?: return null
        return repository.registerDeviceToken(token)
    }

    override suspend fun unregisterAndClear(): Result<Unit> {
        val token = tokenStore.getToken()?.takeIf { it.isNotBlank() }
            ?: return Result.success(Unit)
        val result = repository.unregisterDeviceToken(token)
        tokenStore.clear()
        return result
    }

    private fun isAuthenticated(): Boolean = !authTokenProvider.currentToken().isNullOrBlank()
}

/** Singleton coroutine scope for fire-and-forget FCM work; swappable in tests. */
interface NotificationAppScope {
    val scope: CoroutineScope
}

@Singleton
class DefaultNotificationAppScope @Inject constructor() : NotificationAppScope {
    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
