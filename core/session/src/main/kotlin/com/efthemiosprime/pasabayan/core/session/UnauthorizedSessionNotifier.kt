package com.efthemiosprime.pasabayan.core.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits when [TokenClearingHandler] clears the session after HTTP **401**, so UI (e.g. [AuthViewModel])
 * can match iOS `AuthService.removeToken()` + return to signed-out state.
 */
@Singleton
class UnauthorizedSessionNotifier @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyUnauthorized() {
        _events.tryEmit(Unit)
    }
}
