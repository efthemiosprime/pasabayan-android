package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * iOS parity for [buildActionableItemSpecs]: verifies the cross-feature aggregation rules
 * defined in `ComprehensiveNotificationsView.actionableItems`.
 */
class ActionableItemAggregatorTest {

    // -- Carrier --

    @Test
    fun `carrier sees BOOKING_REQUEST for matches in shipper_requested status`() {
        val specs = run(
            role = UserRole.CARRIER,
            matches = listOf(
                match(MatchStatus.SHIPPER_REQUESTED),
                match(MatchStatus.SHIPPER_REQUESTED),
                match(MatchStatus.CONFIRMED),
            ),
        )
        val bookingReq = specs.first { it.id == ActionableItemIds.BOOKING_REQUESTS }
        assertEquals(ActionableItemType.BOOKING_REQUEST, bookingReq.type)
        assertEquals(2, bookingReq.count)
    }

    @Test
    fun `carrier sees STATUS_UPDATE for matches in confirmed or picked_up status`() {
        val specs = run(
            role = UserRole.CARRIER,
            matches = listOf(
                match(MatchStatus.CONFIRMED),
                match(MatchStatus.PICKED_UP),
                match(MatchStatus.DELIVERED),
                match(MatchStatus.SHIPPER_REQUESTED),
            ),
        )
        val statusUpdate = specs.first { it.id == ActionableItemIds.STATUS_UPDATES }
        assertEquals(ActionableItemType.STATUS_UPDATE, statusUpdate.type)
        assertEquals(2, statusUpdate.count)
    }

    @Test
    fun `carrier sees INACTIVE_TRIPS for trips in planning status`() {
        val specs = run(
            role = UserRole.CARRIER,
            carrierTrips = listOf(
                trip(TripStatus.PLANNING),
                trip(TripStatus.PLANNING),
                trip(TripStatus.ACTIVE),
                trip(TripStatus.COMPLETED),
            ),
        )
        val inactive = specs.first { it.id == ActionableItemIds.INACTIVE_TRIPS }
        assertEquals(ActionableItemType.INACTIVE_TRIPS, inactive.type)
        assertEquals(2, inactive.count)
    }

    @Test
    fun `carrier does not see shipper-only items`() {
        val specs = run(
            role = UserRole.CARRIER,
            matches = listOf(match(MatchStatus.SHIPPER_REQUESTED)),
            shipperPackages = listOf(pkg(PackageRequestStatus.MATCHED)),
        )
        assertTrue(specs.none { it.id == ActionableItemIds.CARRIER_RESPONSES })
        assertTrue(specs.none { it.id == ActionableItemIds.PICKUP_READY })
    }

    // -- Shipper --

    @Test
    fun `shipper sees CARRIER_RESPONSE for matches in shipper_requested status`() {
        val specs = run(
            role = UserRole.SHIPPER,
            matches = listOf(
                match(MatchStatus.SHIPPER_REQUESTED),
                match(MatchStatus.SHIPPER_REQUESTED),
                match(MatchStatus.SHIPPER_REQUESTED),
            ),
        )
        val carrierResp = specs.first { it.id == ActionableItemIds.CARRIER_RESPONSES }
        assertEquals(ActionableItemType.CARRIER_RESPONSE, carrierResp.type)
        assertEquals(3, carrierResp.count)
    }

    @Test
    fun `shipper sees PICKUP_READY for packages in matched status`() {
        val specs = run(
            role = UserRole.SHIPPER,
            shipperPackages = listOf(
                pkg(PackageRequestStatus.MATCHED),
                pkg(PackageRequestStatus.OPEN),
                pkg(PackageRequestStatus.MATCHED),
            ),
        )
        val pickup = specs.first { it.id == ActionableItemIds.PICKUP_READY }
        assertEquals(ActionableItemType.PICKUP_READY, pickup.type)
        assertEquals(2, pickup.count)
    }

    @Test
    fun `shipper does not see carrier-only items`() {
        val specs = run(
            role = UserRole.SHIPPER,
            carrierTrips = listOf(trip(TripStatus.PLANNING)),
            matches = listOf(match(MatchStatus.CONFIRMED)),
        )
        assertTrue(specs.none { it.id == ActionableItemIds.BOOKING_REQUESTS })
        assertTrue(specs.none { it.id == ActionableItemIds.STATUS_UPDATES })
        assertTrue(specs.none { it.id == ActionableItemIds.INACTIVE_TRIPS })
    }

    // -- Cross-role --

    @Test
    fun `unread messages spec appears when count is positive`() {
        val specs = run(
            role = UserRole.CARRIER,
            unreadMessageCount = 3,
        )
        val unread = specs.first { it.id == ActionableItemIds.UNREAD_MESSAGES }
        assertEquals(3, unread.count)
        assertEquals(ActionableItemType.UNREAD_MESSAGES, unread.type)
    }

    @Test
    fun `unread messages spec omitted when count is zero`() {
        val specs = run(
            role = UserRole.CARRIER,
            unreadMessageCount = 0,
        )
        assertTrue(specs.none { it.id == ActionableItemIds.UNREAD_MESSAGES })
    }

    @Test
    fun `basic verification surfaces verify-number prompt`() {
        val specs = run(
            role = UserRole.CARRIER,
            verificationLevel = VerificationLevel.BASIC,
        )
        val verify = specs.first { it.id == ActionableItemIds.VERIFY_NUMBER }
        assertEquals(ActionableItemType.UPGRADE, verify.type)
        assertTrue(specs.none { it.id == ActionableItemIds.UPGRADE_PREMIUM })
    }

    @Test
    fun `verified surfaces premium upsell`() {
        val specs = run(
            role = UserRole.SHIPPER,
            verificationLevel = VerificationLevel.VERIFIED,
        )
        val upgrade = specs.first { it.id == ActionableItemIds.UPGRADE_PREMIUM }
        assertEquals(ActionableItemType.UPGRADE, upgrade.type)
        assertTrue(specs.none { it.id == ActionableItemIds.VERIFY_NUMBER })
    }

    @Test
    fun `premium suppresses both upgrade prompts`() {
        val specs = run(
            role = UserRole.CARRIER,
            verificationLevel = VerificationLevel.PREMIUM,
        )
        assertTrue(specs.none { it.id == ActionableItemIds.VERIFY_NUMBER })
        assertTrue(specs.none { it.id == ActionableItemIds.UPGRADE_PREMIUM })
    }

    @Test
    fun `empty state yields no specs except verification prompt`() {
        val specs = run(role = UserRole.CARRIER, verificationLevel = VerificationLevel.PREMIUM)
        assertEquals(emptyList<ActionableItemSpec>(), specs)
    }

    // -- Helpers --

    private fun run(
        role: UserRole,
        carrierTrips: List<Trip> = emptyList(),
        matches: List<DeliveryMatch> = emptyList(),
        shipperPackages: List<PackageRequest> = emptyList(),
        unreadMessageCount: Int = 0,
        verificationLevel: VerificationLevel = VerificationLevel.PREMIUM,
    ): List<ActionableItemSpec> = buildActionableItemSpecs(
        role = role,
        carrierTrips = carrierTrips,
        matches = matches,
        shipperPackages = shipperPackages,
        unreadMessageCount = unreadMessageCount,
        verificationLevel = verificationLevel,
    )

    private fun match(status: MatchStatus): DeliveryMatch = DeliveryMatch(
        id = 1, tripId = null, packageRequestId = null,
        matchStatus = status,
        agreedPrice = 0.0,
        initiatedBy = InitiatedBy.CARRIER,
        isCounterOffer = false, originalPrice = null,
        canCounterOffer = false, remainingCounterOffers = 0, counterOfferRound = null,
        carrierMessage = null, shipperMessage = null,
        carrier = null, shipper = null, chatConversationId = null,
        confirmedAt = null, pickedUpAt = null, deliveredAt = null,
        createdAt = null, updatedAt = null,
        platformFeePercent = null, transactionStatus = null,
        receiptPhoto = null, autoCancelAfterDays = null,
        pickupConfirmationCode = null, codeExpiresAt = null,
        deliveryVerificationCode = null, deliveryCodeExpiresAt = null,
    )

    private fun trip(status: TripStatus): Trip = Trip(
        id = 1, carrierId = 42,
        originCity = "Toronto", originCountry = "Canada",
        originLat = null, originLng = null,
        destinationCity = "Vancouver", destinationCountry = "Canada",
        destinationLat = null, destinationLng = null,
        departureDate = null, arrivalDate = null,
        availableWeightKg = 25.0, availableSpaceLiters = null,
        pricePerKg = null, tripStatus = status,
        transportationMethod = TransportationMethod.CAR,
        specialNotes = null, carrier = null,
        createdAt = null, updatedAt = null,
        pricingType = null, pricingMethod = null,
        flatTripPrice = null, basePrice = null, calculatedPrice = null,
        pickupAddress = null, pickupLandmark = null,
        dropoffAddress = null, dropoffLandmark = null,
        tripEarningsTotal = null, tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null, pendingRequestCount = null,
        pendingRequests = null, distanceKm = null,
    )

    private fun pkg(status: PackageRequestStatus): PackageRequest = PackageRequest(
        id = 1, shipperId = null,
        pickupAddress = null, pickupCity = null, pickupCountry = null,
        deliveryAddress = null, deliveryCity = null, deliveryCountry = null,
        packageWeightKg = null, packageDimensions = null,
        packageType = null, fragile = null, packageValue = null,
        packageDescription = "Test", urgencyLevel = null, maxPriceBudget = null,
        pickupDatePreferred = null, pickupTimePreferred = null, pickupDateFlexible = null,
        deliveryDateNeeded = null, deliveryTimeNeeded = null,
        specialHandlingRequirements = null,
        requestStatus = status,
        createdAt = null, updatedAt = null,
        compatibleTripsCount = null,
        shipper = null, images = null, imagesProcessing = null,
        serviceType = null, shoppingList = null, storeName = null,
        storeAddress = null, receiptRequired = null,
    )
}
