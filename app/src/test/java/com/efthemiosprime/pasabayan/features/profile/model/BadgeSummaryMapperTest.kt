package com.efthemiosprime.pasabayan.features.profile.model

import com.efthemiosprime.pasabayan.core.network.profile.ActionRequiredJson
import com.efthemiosprime.pasabayan.core.network.profile.BadgeSummaryJson
import com.efthemiosprime.pasabayan.core.network.profile.VerificationJson
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeSummaryMapperTest {

    @Test
    fun `maps a fully-populated wire payload`() {
        val wire = BadgeSummaryJson(
            unreadNotifications = 4,
            actionRequired = ActionRequiredJson(
                total = 6,
                activeDeliveriesNeedingUpdate = 1,
                pendingBookingRequests = 2,
                packagesReadyForPickup = 1,
                tripsWithoutActivity = 2,
            ),
            pendingReviewsCount = 3,
            verification = VerificationJson(
                phoneVerificationNeeded = true,
                payoutSetupNeeded = true,
            ),
            total = 15,
            computedAt = "2026-05-14T17:59:41Z",
            degraded = false,
        )

        val domain = wire.toDomain()

        assertEquals(4, domain.unreadNotifications)
        assertEquals(6, domain.actionRequired.total)
        assertEquals(1, domain.actionRequired.activeDeliveriesNeedingUpdate)
        assertEquals(2, domain.actionRequired.pendingBookingRequests)
        assertEquals(1, domain.actionRequired.packagesReadyForPickup)
        assertEquals(2, domain.actionRequired.tripsWithoutActivity)
        assertEquals(3, domain.pendingReviewsCount)
        assertTrue(domain.verification.phoneVerificationNeeded)
        assertTrue(domain.verification.payoutSetupNeeded)
        assertEquals(15, domain.total)
        assertEquals(Instant.parse("2026-05-14T17:59:41Z"), domain.computedAt)
        assertFalse(domain.degraded)
    }

    @Test
    fun `maps a default wire payload to the empty domain`() {
        val domain = BadgeSummaryJson().toDomain()

        assertEquals(BadgeSummary.empty, domain)
    }

    @Test
    fun `passes degraded through unchanged`() {
        val wire = BadgeSummaryJson(total = 1, degraded = true)
        assertTrue(wire.toDomain().degraded)
    }

    @Test
    fun `parses computed_at with fractional seconds`() {
        val wire = BadgeSummaryJson(computedAt = "2026-05-14T17:59:41.123Z")
        assertEquals(Instant.parse("2026-05-14T17:59:41.123Z"), wire.toDomain().computedAt)
    }

    @Test
    fun `null computed_at yields null Instant`() {
        val wire = BadgeSummaryJson(computedAt = null)
        assertNull(wire.toDomain().computedAt)
    }

    @Test
    fun `malformed computed_at is treated as null instead of throwing`() {
        val wire = BadgeSummaryJson(computedAt = "not-a-timestamp")
        assertNull(wire.toDomain().computedAt)
    }

    @Test
    fun `account-setup-only fixture maps verification flags without re-summing total`() {
        val wire = BadgeSummaryJson(
            verification = VerificationJson(
                phoneVerificationNeeded = true,
                payoutSetupNeeded = true,
            ),
            total = 2,
        )

        val domain = wire.toDomain()

        assertEquals(0, domain.unreadNotifications)
        assertEquals(0, domain.actionRequired.total)
        assertEquals(0, domain.pendingReviewsCount)
        assertTrue(domain.verification.phoneVerificationNeeded)
        assertTrue(domain.verification.payoutSetupNeeded)
        assertEquals(2, domain.total)
    }
}
