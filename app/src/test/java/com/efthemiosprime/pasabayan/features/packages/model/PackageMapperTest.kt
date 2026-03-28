package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.packages.AvailablePackageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageImageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageRequestJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageMapperTest {

    @Test
    fun `PackageRequestJson toDomain maps all core fields`() {
        val json = PackageRequestJson(
            id = 10,
            shipperId = 5,
            pickupCity = "Montreal",
            deliveryCity = "Ottawa",
            packageWeightKg = 12.5,
            packageType = PackageType.ELECTRONICS,
            fragile = true,
            packageValue = 500.0,
            urgencyLevel = UrgencyLevel.HIGH,
            maxPriceBudget = 150.0,
            requestStatus = PackageRequestStatus.OPEN,
            compatibleTripsCount = 3,
        )
        val pkg = json.toDomain()

        assertEquals(10, pkg.id)
        assertEquals(5, pkg.shipperId)
        assertEquals("Montreal", pkg.pickupCity)
        assertEquals("Ottawa", pkg.deliveryCity)
        assertEquals(12.5, pkg.packageWeightKg!!, 0.001)
        assertEquals(PackageType.ELECTRONICS, pkg.packageType)
        assertTrue(pkg.isFragile)
        assertEquals(500.0, pkg.packageValue!!, 0.001)
        assertEquals(UrgencyLevel.HIGH, pkg.urgencyLevel)
        assertEquals(PackageRequestStatus.OPEN, pkg.requestStatus)
        assertEquals(3, pkg.compatibleTripsCount)
    }

    @Test
    fun `PackageRequestJson toDomain maps dimensions`() {
        val json = PackageRequestJson(
            id = 1,
            packageDimensions = PackageDimensions(30.0, 20.0, 15.0),
        )
        val pkg = json.toDomain()
        assertNotNull(pkg.packageDimensions)
        assertEquals(30.0, pkg.packageDimensions!!.length, 0.001)
    }

    @Test
    fun `PackageRequestJson toDomain maps shipper`() {
        val json = PackageRequestJson(
            id = 1,
            shipper = UserSummary(id = 5, name = "Alice"),
        )
        val pkg = json.toDomain()
        assertNotNull(pkg.shipper)
        assertEquals("Alice", pkg.shipper!!.name)
    }

    @Test
    fun `PackageRequestJson toDomain maps images`() {
        val json = PackageRequestJson(
            id = 1,
            images = listOf(
                PackageImageJson(id = 1, packageRequestId = 1, url = "https://example.com/img.jpg"),
            ),
        )
        val pkg = json.toDomain()
        assertEquals(1, pkg.images!!.size)
        assertEquals("https://example.com/img.jpg", pkg.images!![0].url)
    }

    @Test
    fun `PackageRequestJson toDomain handles all nulls`() {
        val json = PackageRequestJson(id = 99)
        val pkg = json.toDomain()
        assertEquals(99, pkg.id)
        assertNull(pkg.pickupCity)
        assertNull(pkg.packageType)
        assertNull(pkg.shipper)
        assertNull(pkg.images)
    }

    @Test
    fun `AvailablePackageJson toDomain maps fields`() {
        val json = AvailablePackageJson(
            id = 20,
            packageRequestId = 10,
            pickupCity = "Toronto",
            deliveryCity = "Montreal",
            packageWeightKg = 8.0,
            urgencyLevel = UrgencyLevel.NORMAL,
            fragile = false,
            packageType = PackageType.GENERAL,
            daysSincePosted = 2.5,
            distanceKm = 350.0,
            shipper = UserSummary(id = 5, name = "Bob"),
        )
        val pkg = json.toDomain()

        assertEquals(20, pkg.id)
        assertEquals(10, pkg.packageRequestId)
        assertEquals("Toronto", pkg.pickupCity)
        assertEquals(8.0, pkg.packageWeightKg!!, 0.001)
        assertEquals(2.5, pkg.daysSincePosted!!, 0.001)
        assertEquals(350.0, pkg.distanceKm!!, 0.001)
        assertEquals("Bob", pkg.shipper!!.name)
    }
}
