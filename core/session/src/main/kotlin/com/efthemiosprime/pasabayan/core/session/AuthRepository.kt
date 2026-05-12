package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import kotlinx.coroutines.flow.StateFlow

/**
 * OAuth provider login (`/auth/{provider}/login`), session user (`/auth/me`), logout.
 * Failures use [Result] with [DomainErrorMapperException] wrapping [DomainError].
 */
interface AuthRepository {

    /** `provider` e.g. `google`, `facebook` — must match backend routes. */
    suspend fun loginWithProviderAccessToken(provider: String, accessToken: String): Result<AuthUser>

    suspend fun loadCurrentUser(): Result<AuthUser>

    suspend fun logout(): Result<Unit>

    /**
     * Live session user. `null` when signed out. Updated by [loginWithProviderAccessToken]
     * and [loadCurrentUser] success, cleared by [logout] and [clearCurrentUser].
     */
    fun currentUser(): StateFlow<AuthUser?>

    /** Drop the cached session user (used on out-of-band invalidation, e.g. 401 from network). */
    fun clearCurrentUser()
}

fun <T> Result<T>.domainError(): DomainError? =
    (exceptionOrNull() as? DomainErrorMapperException)?.domainError
