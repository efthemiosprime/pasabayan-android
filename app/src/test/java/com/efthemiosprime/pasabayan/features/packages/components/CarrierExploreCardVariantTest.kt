package com.efthemiosprime.pasabayan.features.packages.components

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import org.junit.Assert.assertEquals
import org.junit.Test

class CarrierExploreCardVariantTest {

    @Test
    fun `parcel when service type is null`() {
        assertEquals(
            CarrierExploreCardVariant.PARCEL,
            stub(serviceType = null).exploreVariant(),
        )
    }

    @Test
    fun `parcel when service type is delivery`() {
        assertEquals(
            CarrierExploreCardVariant.PARCEL,
            stub(serviceType = "delivery", direction = "send").exploreVariant(),
        )
    }

    @Test
    fun `receive when service is grocery and direction null (legacy)`() {
        assertEquals(
            CarrierExploreCardVariant.RECEIVE,
            stub(serviceType = "grocery_shopping", direction = null).exploreVariant(),
        )
    }

    @Test
    fun `send when direction is send`() {
        assertEquals(
            CarrierExploreCardVariant.SEND,
            stub(serviceType = "general_errand", direction = "send").exploreVariant(),
        )
    }

    @Test
    fun `task when direction is task`() {
        assertEquals(
            CarrierExploreCardVariant.TASK,
            stub(serviceType = "general_errand", direction = "task").exploreVariant(),
        )
    }

    @Test
    fun `mixed-case direction normalizes`() {
        assertEquals(
            CarrierExploreCardVariant.SEND,
            stub(serviceType = "general_errand", direction = "Send").exploreVariant(),
        )
        assertEquals(
            CarrierExploreCardVariant.TASK,
            stub(serviceType = "general_errand", direction = "TASK").exploreVariant(),
        )
    }

    private fun stub(serviceType: String?, direction: String? = null) = AvailablePackage(
        id = 1,
        packageRequestId = 1,
        pickupCity = "Montreal",
        pickupCountry = null,
        deliveryCity = "Toronto",
        deliveryCountry = null,
        packageWeightKg = null,
        packageDimensions = null,
        urgencyLevel = UrgencyLevel.NORMAL,
        maxPriceBudget = null,
        pickupDatePreferred = "2026-05-15",
        pickupDateFlexible = false,
        deliveryDateNeeded = "2026-05-20",
        fragile = false,
        packageType = PackageType.GENERAL,
        packageDescription = null,
        createdAt = "2026-05-10",
        daysSincePosted = null,
        distanceKm = null,
        shipper = null,
        serviceType = serviceType,
        direction = direction,
    )
}
