package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType
import com.efthemiosprime.pasabayan.features.profile.model.ActionRequired
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import com.efthemiosprime.pasabayan.features.profile.model.Verification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * iOS parity for [buildActionableItemSpecs] + [buildAccountSetupSpecs]: card visibility is
 * driven strictly from `BadgeSummary.actionRequired` and `BadgeSummary.verification` — local
 * trip / match / package state is no longer consulted here.
 */
class ActionableItemAggregatorTest {

    // -- Action Required: Carrier --

    @Test
    fun `carrier sees BOOKING_REQUEST when pending_booking_requests greater than zero`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(pendingBookingRequests = 2, total = 2)),
            role = UserRole.CARRIER,
        )

        val booking = specs.first { it.id == ActionableItemIds.BOOKING_REQUESTS }
        assertEquals(ActionableItemType.BOOKING_REQUEST, booking.type)
        assertEquals(2, booking.count)
    }

    @Test
    fun `carrier sees STATUS_UPDATE when active_deliveries_needing_update greater than zero`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(activeDeliveriesNeedingUpdate = 3, total = 3)),
            role = UserRole.CARRIER,
        )

        val status = specs.first { it.id == ActionableItemIds.STATUS_UPDATES }
        assertEquals(ActionableItemType.STATUS_UPDATE, status.type)
        assertEquals(3, status.count)
    }

    @Test
    fun `carrier sees INACTIVE_TRIPS when trips_without_activity greater than zero`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(tripsWithoutActivity = 1, total = 1)),
            role = UserRole.CARRIER,
        )

        val inactive = specs.first { it.id == ActionableItemIds.INACTIVE_TRIPS }
        assertEquals(ActionableItemType.INACTIVE_TRIPS, inactive.type)
        assertEquals(1, inactive.count)
    }

    @Test
    fun `carrier does not see shipper-only cards`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(packagesReadyForPickup = 5, total = 5)),
            role = UserRole.CARRIER,
        )

        assertTrue(specs.none { it.id == ActionableItemIds.PICKUP_READY })
    }

    // -- Action Required: Shipper --

    @Test
    fun `shipper sees CARRIER_RESPONSE when pending_booking_requests greater than zero`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(pendingBookingRequests = 4, total = 4)),
            role = UserRole.SHIPPER,
        )

        val carrier = specs.first { it.id == ActionableItemIds.CARRIER_RESPONSES }
        assertEquals(ActionableItemType.CARRIER_RESPONSE, carrier.type)
        assertEquals(4, carrier.count)
    }

    @Test
    fun `shipper sees PICKUP_READY when packages_ready_for_pickup greater than zero`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(packagesReadyForPickup = 2, total = 2)),
            role = UserRole.SHIPPER,
        )

        val pickup = specs.first { it.id == ActionableItemIds.PICKUP_READY }
        assertEquals(ActionableItemType.PICKUP_READY, pickup.type)
        assertEquals(2, pickup.count)
    }

    @Test
    fun `shipper does not see carrier-only cards`() {
        val specs = buildActionableItemSpecs(
            summary = summary(
                action = ActionRequired(
                    activeDeliveriesNeedingUpdate = 1,
                    tripsWithoutActivity = 1,
                    total = 2,
                ),
            ),
            role = UserRole.SHIPPER,
        )

        assertTrue(specs.none { it.id == ActionableItemIds.STATUS_UPDATES })
        assertTrue(specs.none { it.id == ActionableItemIds.INACTIVE_TRIPS })
    }

    // -- Zero / null edge cases --

    @Test
    fun `null summary yields empty action list`() {
        assertTrue(buildActionableItemSpecs(summary = null, role = UserRole.CARRIER).isEmpty())
        assertTrue(buildActionableItemSpecs(summary = null, role = UserRole.SHIPPER).isEmpty())
    }

    @Test
    fun `empty action_required yields empty action list`() {
        assertTrue(
            buildActionableItemSpecs(summary = summary(), role = UserRole.CARRIER).isEmpty(),
        )
    }

    @Test
    fun `zero counts produce no cards`() {
        val specs = buildActionableItemSpecs(
            summary = summary(action = ActionRequired(total = 0)),
            role = UserRole.CARRIER,
        )
        assertTrue(specs.isEmpty())
    }

    // -- Account Setup --

    @Test
    fun `verify_phone card appears when phoneVerificationNeeded is true`() {
        val specs = buildAccountSetupSpecs(
            summary = summary(verification = Verification(phoneVerificationNeeded = true)),
        )

        val phone = specs.first { it.id == ActionableItemIds.VERIFY_PHONE }
        assertEquals(ActionableItemType.VERIFY_PHONE, phone.type)
    }

    @Test
    fun `setup_payout card appears when payoutSetupNeeded is true`() {
        val specs = buildAccountSetupSpecs(
            summary = summary(verification = Verification(payoutSetupNeeded = true)),
        )

        val payout = specs.first { it.id == ActionableItemIds.SETUP_PAYOUT }
        assertEquals(ActionableItemType.SETUP_PAYOUT, payout.type)
    }

    @Test
    fun `both flags true produce phone first then payout (iOS order)`() {
        val specs = buildAccountSetupSpecs(
            summary = summary(
                verification = Verification(
                    phoneVerificationNeeded = true,
                    payoutSetupNeeded = true,
                ),
            ),
        )

        assertEquals(2, specs.size)
        assertEquals(ActionableItemIds.VERIFY_PHONE, specs[0].id)
        assertEquals(ActionableItemIds.SETUP_PAYOUT, specs[1].id)
    }

    @Test
    fun `both flags false yields empty account setup list`() {
        val specs = buildAccountSetupSpecs(summary = summary())
        assertTrue(specs.isEmpty())
    }

    @Test
    fun `null summary yields empty account setup list`() {
        assertTrue(buildAccountSetupSpecs(summary = null).isEmpty())
    }

    // -- Regression: account-setup-only fixture (the iOS empty-drawer bug) --

    @Test
    fun `verification-only payload produces no action items but two account-setup items`() {
        val regression = summary(
            verification = Verification(
                phoneVerificationNeeded = true,
                payoutSetupNeeded = true,
            ),
            total = 2,
        )

        val actions = buildActionableItemSpecs(summary = regression, role = UserRole.CARRIER)
        val account = buildAccountSetupSpecs(summary = regression)

        assertTrue(actions.isEmpty())
        assertEquals(2, account.size)
    }

    private fun summary(
        action: ActionRequired = ActionRequired.empty,
        verification: Verification = Verification.empty,
        total: Int = action.total + verification.flagCount(),
    ): BadgeSummary = BadgeSummary(
        actionRequired = action,
        verification = verification,
        total = total,
    )

    private fun Verification.flagCount(): Int =
        (if (phoneVerificationNeeded) 1 else 0) + (if (payoutSetupNeeded) 1 else 0)
}
