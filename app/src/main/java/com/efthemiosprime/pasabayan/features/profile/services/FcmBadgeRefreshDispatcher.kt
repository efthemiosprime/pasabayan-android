package com.efthemiosprime.pasabayan.features.profile.services

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inspects an FCM data payload for `type=badge_refresh` and, if present, pokes
 * [BadgeRefreshBus] to trigger a re-fetch. Extracted from the FCM service so
 * the dispatch logic is unit-testable without instantiating a
 * `FirebaseMessagingService`.
 *
 * iOS parity: `AppDelegate.swift` (~L378–391) silent-push handler that calls
 * `BadgeSummaryStore.shared.refresh()` for the same payload shape.
 */
@Singleton
class FcmBadgeRefreshDispatcher @Inject constructor(
    private val refreshBus: BadgeRefreshBus,
) {

    /**
     * Returns true if [data] was a badge_refresh signal (and was consumed —
     * caller must skip any user-visible UI for this message). Logs the
     * optional `reason` field for support correlation.
     */
    fun handleIfBadgeRefresh(data: Map<String, String>): Boolean {
        if (data[TYPE_KEY] != BADGE_REFRESH_TYPE) return false
        val reason = data[REASON_KEY] ?: "unknown"
        Log.d(TAG, "FCM badge_refresh received reason=$reason")
        refreshBus.emit()
        return true
    }

    private companion object {
        const val TAG = "BadgeSummary"
        const val TYPE_KEY = "type"
        const val REASON_KEY = "reason"
        const val BADGE_REFRESH_TYPE = "badge_refresh"
    }
}
