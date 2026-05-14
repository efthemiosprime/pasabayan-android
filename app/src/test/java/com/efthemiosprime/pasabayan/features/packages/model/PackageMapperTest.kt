package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.ErrandDirection
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.packages.AvailablePackageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageImageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageRequestJson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageMapperTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

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
    fun `PackageRequestJson toDomain flattens shopping list array`() {
        val json = PackageRequestJson(
            id = 50,
            serviceType = "grocery_shopping",
            shoppingList = this.json.parseToJsonElement(
                """
                [
                  {"item":"Cheese Burger","quantity":"1","notes":"Well-done"},
                  {"item":"Iced Coffee","quantity":"1","notes":"Vanilla"}
                ]
                """.trimIndent(),
            ),
        )

        val pkg = json.toDomain()

        assertEquals("grocery_shopping", pkg.serviceType)
        assertNotNull(pkg.shoppingList)
        assertTrue(pkg.shoppingList!!.contains("Cheese Burger x1 - Well-done"))
        assertTrue(pkg.shoppingList!!.contains("Iced Coffee x1 - Vanilla"))
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
    fun `PackageRequestJson toDomain maps coordinates`() {
        val payload = PackageRequestJson(
            id = 30,
            pickupLat = 43.6532,
            pickupLng = -79.3832,
            deliveryLat = 45.5017,
            deliveryLng = -73.5673,
        )
        val pkg = payload.toDomain()

        assertEquals(43.6532, pkg.pickupLat!!, 0.0001)
        assertEquals(-79.3832, pkg.pickupLng!!, 0.0001)
        assertEquals(45.5017, pkg.deliveryLat!!, 0.0001)
        assertEquals(-73.5673, pkg.deliveryLng!!, 0.0001)
    }

    @Test
    fun `PackageRequestJson toDomain maps task name and description`() {
        val payload = PackageRequestJson(
            id = 31,
            serviceType = "general_errand",
            taskName = "Water the plants",
            taskDescription = "Tuesdays and Fridays for the next two weeks",
        )
        val pkg = payload.toDomain()

        assertEquals("Water the plants", pkg.taskName)
        assertEquals("Tuesdays and Fridays for the next two weeks", pkg.taskDescription)
    }

    @Test
    fun `PackageRequestJson toDomain maps direction and recipient fields`() {
        val payload = PackageRequestJson(
            id = 32,
            serviceType = "general_errand",
            direction = "send",
            recipientName = "Jane Doe",
            recipientPhone = "+15145551234",
        )
        val pkg = payload.toDomain()

        assertEquals("send", pkg.direction)
        assertEquals(ErrandDirection.SEND, pkg.errandDirection)
        assertTrue(pkg.isSendErrand)
        assertEquals("Jane Doe", pkg.recipientName)
        assertEquals("+15145551234", pkg.recipientPhone)
    }

    @Test
    fun `PackageRequestJson toDomain treats null direction as RECEIVE`() {
        // Legacy delivery rows omit direction.
        val pkg = PackageRequestJson(id = 1).toDomain()
        assertNull(pkg.direction)
        assertEquals(ErrandDirection.RECEIVE, pkg.errandDirection)
        assertFalse(pkg.isTaskErrand)
        assertFalse(pkg.isSendErrand)
    }

    @Test
    fun `AvailablePackageJson toDomain maps errand fields and round-trips through toPackageRequest`() {
        val avail = AvailablePackageJson(
            id = 21,
            packageRequestId = 11,
            pickupCity = "Montreal",
            deliveryCity = "Toronto",
            urgencyLevel = UrgencyLevel.NORMAL,
            packageType = PackageType.GENERAL,
            serviceType = "general_errand",
            direction = "task",
            storeName = "Metro Plus",
            recipientName = null,
            recipientPhone = null,
            taskName = "Walk my dog",
            taskDescription = "Daily walk, 30 min.",
            shipper = UserSummary(id = 5, name = "Alex"),
        ).toDomain()

        assertEquals("task", avail.direction)
        assertEquals(ErrandDirection.TASK, avail.errandDirection)
        assertTrue(avail.isTaskErrand)
        assertEquals("Metro Plus", avail.storeName)
        assertEquals("Walk my dog", avail.taskName)
        assertEquals("Daily walk, 30 min.", avail.taskDescription)

        val pkg = avail.toPackageRequest()
        assertEquals("task", pkg.direction)
        assertEquals(ErrandDirection.TASK, pkg.errandDirection)
        assertEquals("Walk my dog", pkg.taskName)
        assertEquals("Daily walk, 30 min.", pkg.taskDescription)
        assertEquals("Metro Plus", pkg.storeName)
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
