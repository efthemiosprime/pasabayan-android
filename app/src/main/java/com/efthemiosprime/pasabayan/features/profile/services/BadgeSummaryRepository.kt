package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source-of-truth attention container. Mirrors iOS
 * `Pasabayan/Features/Notifications/Services/BadgeSummaryStore.swift`.
 *
 * Consumers read [summary] + [hasValidData] to render badges; the same store
 * feeds the tab-bar Profile badge, dashboard bell, drawer sections, and per-row
 * Profile badges. Failures keep the last-good [summary] so badges don't flash
 * zero — [hasValidData] flips off so gating consumers can choose to hide.
 *
 * Concurrent [refresh] calls for the same role are coalesced — a second call
 * while one is in flight is a no-op. Different roles fetch independently.
 *
 * [snapshotMode] short-circuits [refresh] for UI tests; tests seed state via
 * [seedForSnapshotTesting].
 */
interface BadgeSummaryRepository {
    val summary: StateFlow<BadgeSummary?>
    val hasValidData: StateFlow<Boolean>
    val lastFetchedAt: StateFlow<Instant?>

    /** Most-recent role passed to [refresh]. Used by lifecycle/auth/bus observers. */
    val lastRequestedRole: UserRole?

    var snapshotMode: Boolean

    /**
     * Fetch `/me/badge-summary` for [role] (null = dual-role union). Coalesces
     * per role. Failures keep prior [summary] and flip [hasValidData] to false.
     * Skipped (and triggers [clear]) when no auth token is available.
     */
    suspend fun refresh(role: UserRole?)

    /** Drop cached state. Called on logout / auth user becoming null. */
    fun clear()

    /** UI-test seed. Always applied, even in [snapshotMode]. */
    fun seedForSnapshotTesting(summary: BadgeSummary?)
}
