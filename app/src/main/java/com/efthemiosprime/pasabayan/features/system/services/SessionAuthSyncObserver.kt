package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.locations.services.HomeCityDetectionService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * App-lifetime observer that turns `AuthRepository.currentUser` emissions into the
 * post-login bootstrap calls that previously had no caller:
 *
 *  - Refreshes [SessionActivityLogUserProvider] so [ActivityLogger] submissions carry
 *    the current `user_id` / `user_type` (null on logout → cleared).
 *  - Triggers [HomeCityDetectionService.detectIfNeeded] for the first non-null user
 *    seen this process (the service has its own `hasRunThisSession` guard so
 *    re-emissions are no-ops).
 *
 * Started from [com.efthemiosprime.pasabayan.PasabayanApplication.onCreate] with an
 * app-scope coroutine so it runs across the full app lifetime — single source of
 * truth for "what happens when a user logs in / out."
 */
@Singleton
class SessionAuthSyncObserver @Inject constructor(
    private val authRepository: AuthRepository,
    private val activityLogUserProvider: SessionActivityLogUserProvider,
    private val homeCityDetectionService: HomeCityDetectionService,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            authRepository.currentUser()
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { user -> handle(user) }
        }
    }

    private fun CoroutineScope.handle(user: AuthUser?) {
        if (user == null) {
            activityLogUserProvider.clear()
            return
        }
        // userType currently resolves to "user" downstream; the active role isn't
        // in the auth payload and re-plumbing RoleViewModel here would invert the
        // dependency direction. Follow-up: feed role updates back via a dedicated
        // observer when role becomes a session-level concern.
        activityLogUserProvider.update(user, userType = null)
        launch { homeCityDetectionService.detectIfNeeded() }
    }
}
