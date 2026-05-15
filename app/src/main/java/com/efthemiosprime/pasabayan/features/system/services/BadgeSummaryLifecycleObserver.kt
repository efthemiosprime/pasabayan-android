package com.efthemiosprime.pasabayan.features.system.services

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.services.BadgeRefreshBus
import com.efthemiosprime.pasabayan.features.profile.services.BadgeSummaryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * App-scope observer that drives [BadgeSummaryRepository.refresh] from the three
 * background signals that don't originate in a screen:
 *
 *  1. Foreground transition — `ProcessLifecycleOwner` ON_START re-fetches with
 *     the most-recent role. iOS parity: `UIApplication.willEnterForegroundNotification`.
 *  2. Auth user change — login / logout / phone-verified flip. iOS parity:
 *     `AuthService.$currentUser`.
 *  3. In-app bus — [BadgeRefreshBus] post-action signal. iOS parity:
 *     `Notification.Name.profileAttentionShouldRefresh`.
 *
 * Started from `PasabayanApplication.onCreate` next to [SessionAuthSyncObserver].
 */
@Singleton
class BadgeSummaryLifecycleObserver @Inject constructor(
    private val repository: BadgeSummaryRepository,
    private val refreshBus: BadgeRefreshBus,
    private val authRepository: AuthRepository,
    private val processLifecycle: Lifecycle,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            processLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.refresh(repository.lastRequestedRole)
            }
        }
        scope.launch {
            authRepository.currentUser()
                .distinctUntilChanged(::authSignalUnchanged)
                .collect { user ->
                    if (user == null) {
                        repository.clear()
                    } else {
                        repository.refresh(repository.lastRequestedRole)
                    }
                }
        }
        scope.launch {
            refreshBus.events.collect {
                repository.refresh(repository.lastRequestedRole)
            }
        }
    }

    /**
     * Refresh on identity changes and on the phone-verified flag flip — iOS
     * relies on the same composite key so the badge re-pulls after OTP success.
     */
    private fun authSignalUnchanged(old: AuthUser?, new: AuthUser?): Boolean =
        old?.id == new?.id && old?.phoneVerified == new?.phoneVerified
}
