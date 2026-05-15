package com.efthemiosprime.pasabayan.features.profile.services

import android.util.Log
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.session.TokenStore
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import com.efthemiosprime.pasabayan.features.profile.model.toDomain
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json

@Singleton
class BadgeSummaryRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val tokenStore: TokenStore,
    private val json: Json,
    private val clock: Clock = Clock.systemUTC(),
) : BadgeSummaryRepository {

    private val _summary = MutableStateFlow<BadgeSummary?>(null)
    private val _hasValidData = MutableStateFlow(false)
    private val _lastFetchedAt = MutableStateFlow<Instant?>(null)

    @Volatile
    private var _lastRequestedRole: UserRole? = null

    /**
     * Per-role mutex; `tryLock` succeeds for the first refresh per key and fails
     * for any concurrent caller, producing the iOS "second call is a no-op"
     * behavior without blocking the caller's coroutine.
     */
    private val mutexes = ConcurrentHashMap<String, Mutex>()

    override val summary: StateFlow<BadgeSummary?> = _summary.asStateFlow()
    override val hasValidData: StateFlow<Boolean> = _hasValidData.asStateFlow()
    override val lastFetchedAt: StateFlow<Instant?> = _lastFetchedAt.asStateFlow()
    override val lastRequestedRole: UserRole? get() = _lastRequestedRole

    @Volatile
    override var snapshotMode: Boolean = false

    override suspend fun refresh(role: UserRole?) {
        val key = role.toKey()
        _lastRequestedRole = role

        if (snapshotMode) {
            Log.d(TAG, "$LOG_PREFIX: refresh skipped — snapshotMode (role=$key)")
            return
        }
        if (tokenStore.getToken() == null) {
            Log.d(TAG, "$LOG_PREFIX: refresh skipped — unauthenticated (role=$key)")
            clear()
            return
        }

        val mutex = mutexes.getOrPut(key) { Mutex() }
        if (!mutex.tryLock()) {
            Log.d(TAG, "$LOG_PREFIX: refresh coalesced — already in flight (role=$key)")
            return
        }
        try {
            performRefresh(role, key)
        } finally {
            mutex.unlock()
        }
    }

    override fun clear() {
        _summary.value = null
        _hasValidData.value = false
        _lastFetchedAt.value = null
    }

    override fun seedForSnapshotTesting(summary: BadgeSummary?) {
        _summary.value = summary
        _hasValidData.value = summary != null
        _lastFetchedAt.value = if (summary != null) clock.instant() else null
    }

    private suspend fun performRefresh(role: UserRole?, key: String) {
        Log.d(TAG, "$LOG_PREFIX: GET /me/badge-summary role=$key")
        try {
            val response = profileApi.getBadgeSummary(role?.toQueryValue())
            if (!response.isSuccessful) {
                val err = ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json)
                handleFailure(DomainErrorMapperException(err), key)
                return
            }
            val body = response.body()
            if (body == null) {
                handleFailure(DomainErrorMapperException(DomainError.InvalidResponse), key)
                return
            }
            val domain = body.toDomain()
            _summary.value = domain
            _hasValidData.value = true
            _lastFetchedAt.value = clock.instant()
            Log.d(
                TAG,
                "$LOG_PREFIX: applied total=${domain.total} unread=${domain.unreadNotifications} " +
                    "action=${domain.actionRequired.total} reviews=${domain.pendingReviewsCount} " +
                    "phone=${domain.verification.phoneVerificationNeeded} " +
                    "payout=${domain.verification.payoutSetupNeeded} degraded=${domain.degraded}",
            )
        } catch (t: Throwable) {
            handleFailure(t, key)
        }
    }

    /**
     * Keep [_summary] so consumers don't flash zero. Flip [_hasValidData] off so
     * badge surfaces gating on it can hide. Next successful refresh restores
     * authoritative state.
     */
    private fun handleFailure(error: Throwable, key: String) {
        _hasValidData.value = false
        Log.w(TAG, "$LOG_PREFIX: refresh failed (role=$key) - ${error.message ?: error::class.simpleName}")
    }

    private fun UserRole?.toKey(): String = this?.name?.lowercase() ?: ALL_KEY

    private fun UserRole.toQueryValue(): String = name.lowercase()

    private companion object {
        const val TAG = "BadgeSummary"
        const val LOG_PREFIX = "BadgeSummaryRepository"
        const val ALL_KEY = "all"
    }
}
