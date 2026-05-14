package com.efthemiosprime.pasabayan.features.profile.model

import com.efthemiosprime.pasabayan.core.network.profile.ActionRequiredJson
import com.efthemiosprime.pasabayan.core.network.profile.BadgeSummaryJson
import com.efthemiosprime.pasabayan.core.network.profile.VerificationJson
import java.time.Instant
import java.time.format.DateTimeParseException

fun BadgeSummaryJson.toDomain(): BadgeSummary = BadgeSummary(
    unreadNotifications = unreadNotifications,
    actionRequired = actionRequired.toDomain(),
    pendingReviewsCount = pendingReviewsCount,
    verification = verification.toDomain(),
    total = total,
    computedAt = computedAt?.toInstantOrNull(),
    degraded = degraded,
)

private fun ActionRequiredJson.toDomain(): ActionRequired = ActionRequired(
    total = total,
    activeDeliveriesNeedingUpdate = activeDeliveriesNeedingUpdate,
    pendingBookingRequests = pendingBookingRequests,
    packagesReadyForPickup = packagesReadyForPickup,
    tripsWithoutActivity = tripsWithoutActivity,
)

private fun VerificationJson.toDomain(): Verification = Verification(
    phoneVerificationNeeded = phoneVerificationNeeded,
    payoutSetupNeeded = payoutSetupNeeded,
)

private fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (_: DateTimeParseException) {
    null
}
