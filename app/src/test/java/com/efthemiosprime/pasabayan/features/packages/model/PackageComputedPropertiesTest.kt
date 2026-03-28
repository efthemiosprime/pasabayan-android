package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageSize
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageComputedPropertiesTest {

    private fun basePkg(
        packageDescription: String? = "Test package",
        packageType: PackageType? = PackageType.GENERAL,
        pickupAddress: String? = "123 Main St",
        pickupCity: String? = "Toronto",
        deliveryAddress: String? = "456 Oak Ave",
        deliveryCity: String? = "Montreal",
        fragile: Boolean? = false,
        requestStatus: PackageRequestStatus? = PackageRequestStatus.OPEN,
        packageWeightKg: Double? = 10.0,
        serviceType: String? = null,
    ) = PackageRequest(
        id = 1, shipperId = 5,
        pickupAddress = pickupAddress, pickupCity = pickupCity, pickupCountry = "Canada",
        deliveryAddress = deliveryAddress, deliveryCity = deliveryCity, deliveryCountry = "Canada",
        packageWeightKg = packageWeightKg, packageDimensions = null,
        packageType = packageType, fragile = fragile,
        packageValue = null, packageDescription = packageDescription,
        urgencyLevel = UrgencyLevel.NORMAL, maxPriceBudget = null,
        pickupDatePreferred = "2026-04-05", pickupTimePreferred = "10:00",
        pickupDateFlexible = false,
        deliveryDateNeeded = "2026-04-07", deliveryTimeNeeded = null,
        specialHandlingRequirements = null,
        requestStatus = requestStatus,
        createdAt = null, updatedAt = null,
        compatibleTripsCount = null, shipper = null,
        images = null, imagesProcessing = null,
        serviceType = serviceType, shoppingList = null,
        storeName = null, storeAddress = null,
        receiptRequired = null,
    )

    @Test
    fun `title uses packageDescription when available`() {
        assertEquals("Test package", basePkg(packageDescription = "Test package").title)
    }

    @Test
    fun `title falls back to packageType displayName`() {
        val pkg = basePkg(packageDescription = null, packageType = PackageType.ELECTRONICS)
        assertTrue(pkg.title.contains("Package"))
    }

    @Test
    fun `pickupLocation combines address and city`() {
        assertEquals("123 Main St, Toronto", basePkg().pickupLocation)
    }

    @Test
    fun `pickupLocation shows only city when no address`() {
        assertEquals("Toronto", basePkg(pickupAddress = null).pickupLocation)
    }

    @Test
    fun `pickupLocation empty when both null`() {
        assertEquals("", basePkg(pickupAddress = null, pickupCity = null).pickupLocation)
    }

    @Test
    fun `deliveryLocation combines address and city`() {
        assertEquals("456 Oak Ave, Montreal", basePkg().deliveryLocation)
    }

    @Test
    fun `isFragile defaults to false when null`() {
        assertFalse(basePkg(fragile = null).isFragile)
    }

    @Test
    fun `isFragile returns true when true`() {
        assertTrue(basePkg(fragile = true).isFragile)
    }

    @Test
    fun `status defaults to OPEN when null`() {
        assertEquals(PackageRequestStatus.OPEN, basePkg(requestStatus = null).status)
    }

    @Test
    fun `packageSize computed from weight - small`() {
        assertEquals(PackageSize.SMALL, basePkg(packageWeightKg = 3.0).packageSize)
    }

    @Test
    fun `packageSize computed from weight - medium`() {
        assertEquals(PackageSize.MEDIUM, basePkg(packageWeightKg = 10.0).packageSize)
    }

    @Test
    fun `packageSize computed from weight - large`() {
        assertEquals(PackageSize.LARGE, basePkg(packageWeightKg = 25.0).packageSize)
    }

    @Test
    fun `packageSize computed from weight - extra large`() {
        assertEquals(PackageSize.EXTRA_LARGE, basePkg(packageWeightKg = 50.0).packageSize)
    }

    @Test
    fun `isServiceRequest true for grocery_shopping`() {
        assertTrue(basePkg(serviceType = "grocery_shopping").isServiceRequest)
    }

    @Test
    fun `isServiceRequest false for delivery`() {
        assertFalse(basePkg(serviceType = "delivery").isServiceRequest)
    }

    @Test
    fun `isServiceRequest false when null`() {
        assertFalse(basePkg(serviceType = null).isServiceRequest)
    }
}
