package com.efthemiosprime.pasabayan.features.packages.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PackageSubmitRequestMapperTest {

    @Test
    fun `maps package payload to create package request json`() {
        val payload = PackageSubmitPayload(
            pickupAddress = "123 Main",
            pickupCity = "Toronto",
            pickupCountryCode = "CA",
            deliveryAddress = "456 Oak",
            deliveryCity = "Montreal",
            deliveryCountryCode = "CA",
            packageWeightKg = 2.75,
            packageTypeCode = "electronics",
            fragile = true,
            urgencyLevelCode = "high",
            pickupDatePreferred = LocalDate.of(2026, 4, 1),
            pickupTimePreferred = LocalTime.of(9, 5),
            pickupDateFlexible = false,
            deliveryDateNeeded = LocalDate.of(2026, 4, 2),
            deliveryTimeNeeded = LocalTime.of(14, 30),
            packageValue = 250.0,
            packageDescription = " Laptop ",
            maxPriceBudget = 70.0,
            specialHandlingRequirements = " Handle with care ",
        )

        val request = PackageSubmitRequestMapper.toCreatePackageRequestJson(payload)
        assertEquals("CA", request.pickupCountry)
        assertEquals("electronics", request.packageType)
        assertEquals("2026-04-01", request.pickupDatePreferred)
        assertEquals("09:05", request.pickupTimePreferred)
        assertEquals("14:30", request.deliveryTimeNeeded)
        assertEquals("Laptop", request.packageDescription)
        assertEquals("Handle with care", request.specialHandlingRequirements)
    }

    @Test
    fun `maps service payload and drops blank optional fields`() {
        val payload = ServiceRequestSubmitPayload(
            serviceTypeCode = "grocery_shopping",
            shoppingItems = listOf(
                ServiceRequestShoppingItem(item = " Milk ", quantity = " 2L ", notes = " "),
            ),
            deliveryCity = " Toronto ",
            deliveryAddress = " 123 Main ",
            storeName = " ",
            storeAddress = null,
            estimatedCost = 45.0,
            maxPriceBudget = null,
            deliveryDateNeeded = LocalDate.of(2026, 4, 10),
            urgencyLevelCode = "normal",
            directionCode = "receive",
            recipientName = " ",
            recipientPhone = "  ",
        )

        val request = PackageSubmitRequestMapper.toCreateServiceRequestBodyJson(payload)
        assertEquals("grocery_shopping", request.serviceType)
        assertEquals("Milk", request.shoppingList.first().item)
        assertEquals("2L", request.shoppingList.first().quantity)
        assertNull(request.shoppingList.first().notes)
        assertEquals("Toronto", request.deliveryCity)
        assertEquals("123 Main", request.deliveryAddress)
        assertEquals("2026-04-10", request.deliveryDateNeeded)
        assertNull(request.storeName)
        assertNull(request.recipientName)
        assertNull(request.recipientPhone)
    }

    @Test
    fun `maps task-mode payload with task name and description`() {
        val payload = ServiceRequestSubmitPayload(
            serviceTypeCode = "general_errand",
            shoppingItems = emptyList(),
            deliveryCity = "Toronto",
            deliveryAddress = "789 Bay",
            storeName = null,
            storeAddress = null,
            estimatedCost = null,
            maxPriceBudget = 50.0,
            deliveryDateNeeded = LocalDate.of(2026, 4, 10),
            urgencyLevelCode = "normal",
            directionCode = "task",
            recipientName = null,
            recipientPhone = null,
            taskName = " Water plants ",
            taskDescription = " Twice a week ",
            storeLat = 43.65,
            storeLng = -79.38,
            deliveryLat = 43.66,
            deliveryLng = -79.39,
        )

        val request = PackageSubmitRequestMapper.toCreateServiceRequestBodyJson(payload)
        assertEquals("Water plants", request.taskName)
        assertEquals("Twice a week", request.taskDescription)
        assertEquals(43.65, request.storeLat!!, 0.001)
        assertEquals(-79.38, request.storeLng!!, 0.001)
        assertEquals(43.66, request.deliveryLat!!, 0.001)
        assertEquals(-79.39, request.deliveryLng!!, 0.001)
        assertEquals("task", request.direction)
    }
}
