package com.efthemiosprime.pasabayan.features.profile.services

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide signal that something user-visible just changed and the badge
 * summary should be re-fetched. Mirrors iOS
 * `Notification.Name.profileAttentionShouldRefresh` (see
 * `BadgeSummaryStore.swift` observer setup).
 *
 * Producers: post-Stripe-Connect onboarding, post-rating-submission,
 * post-phone-verification, and the `badge_refresh` silent FCM handler.
 * Subscriber: [BadgeSummaryLifecycleObserver] re-fetches with
 * [BadgeSummaryRepository.lastRequestedRole].
 *
 * Replay 0 + small buffer — a missed emission while no observer is attached is
 * recovered by the lifecycle ON_START refresh that always runs on foreground.
 */
@Singleton
class BadgeRefreshBus @Inject constructor() {

    private val _events = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 4,
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emit(): Boolean = _events.tryEmit(Unit)
}
