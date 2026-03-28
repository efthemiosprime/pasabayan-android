package com.efthemiosprime.pasabayan.core.network.packages

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/packages/$name")!!
            .bufferedReader().readText()

    // -- PackageRequestJson --

    @Test
    fun `PackageRequestJson decodes full package with flexible types`() {
        val raw = fixture("package_request_full.json")
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        assertEquals(10, pkg.id)
        assertEquals(5, pkg.shipperId)
        assertEquals("Montreal", pkg.pickupCity)
        assertEquals("Ottawa", pkg.deliveryCity)
        // Flexible: weight from string "12.5"
        assertEquals(12.5, pkg.packageWeightKg!!, 0.001)
        assertEquals(PackageType.ELECTRONICS, pkg.packageType)
        // Flexible: fragile from int 1
        assertEquals(true, pkg.fragile)
        // Flexible: packageValue from string "500.00"
        assertEquals(500.0, pkg.packageValue!!, 0.001)
        assertEquals(UrgencyLevel.HIGH, pkg.urgencyLevel)
        assertEquals(150.0, pkg.maxPriceBudget!!, 0.001)
        assertEquals(PackageRequestStatus.OPEN, pkg.requestStatus)
        assertEquals(3, pkg.compatibleTripsCount)
        assertNotNull(pkg.shipper)
        assertEquals("Alice Shipper", pkg.shipper!!.name)
        assertNotNull(pkg.images)
        assertEquals(1, pkg.images!!.size)
    }

    @Test
    fun `PackageRequestJson decodes dimensions as object`() {
        val raw = fixture("package_request_full.json")
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        assertNotNull(pkg.packageDimensions)
        assertEquals(30.0, pkg.packageDimensions!!.length, 0.001)
        assertEquals(20.0, pkg.packageDimensions!!.width, 0.001)
        assertEquals(15.0, pkg.packageDimensions!!.height, 0.001)
    }

    @Test
    fun `PackageRequestJson decodes dimensions as stringified JSON`() {
        val raw = fixture("package_dimensions_string.json")
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        assertNotNull(pkg.packageDimensions)
        assertEquals(10.0, pkg.packageDimensions!!.length, 0.001)
        assertEquals(8.0, pkg.packageDimensions!!.width, 0.001)
        assertEquals(5.0, pkg.packageDimensions!!.height, 0.001)
    }

    @Test
    fun `PackageRequestJson decodes fragile from string no`() {
        val raw = fixture("package_dimensions_string.json")
        val pkg = json.decodeFromString<PackageRequestJson>(raw)
        assertEquals(false, pkg.fragile)
    }

    @Test
    fun `PackageRequestJson decodes with minimal fields`() {
        val raw = """{"id": 99, "pickup_city": "Winnipeg", "delivery_city": "Regina"}"""
        val pkg = json.decodeFromString<PackageRequestJson>(raw)
        assertEquals(99, pkg.id)
        assertEquals("Winnipeg", pkg.pickupCity)
        assertNull(pkg.packageWeightKg)
        assertNull(pkg.packageType)
        assertNull(pkg.shipper)
    }

    // -- AvailablePackageJson --

    @Test
    fun `AvailablePackageJson decodes with flexible fields`() {
        val raw = fixture("available_package.json")
        val pkg = json.decodeFromString<AvailablePackageJson>(raw)

        assertEquals(20, pkg.id)
        assertEquals(10, pkg.packageRequestId)
        assertEquals("Toronto", pkg.pickupCity)
        assertEquals("Montreal", pkg.deliveryCity)
        // Flexible: weight from string "8.0"
        assertEquals(8.0, pkg.packageWeightKg!!, 0.001)
        assertEquals(UrgencyLevel.NORMAL, pkg.urgencyLevel)
        // Flexible: maxPriceBudget from string "100.00"
        assertEquals(100.0, pkg.maxPriceBudget!!, 0.001)
        assertFalse(pkg.fragile)
        assertEquals(PackageType.GENERAL, pkg.packageType)
        // Flexible: daysSincePosted from string "2.5"
        assertEquals(2.5, pkg.daysSincePosted!!, 0.001)
        // Flexible: distanceKm from string "350.0"
        assertEquals(350.0, pkg.distanceKm!!, 0.001)
        assertNotNull(pkg.shipper)
        assertEquals("Bob", pkg.shipper!!.name)
    }

    // -- PackageImageJson --

    @Test
    fun `PackageImageJson decodes`() {
        val raw = """{
            "id": 1,
            "package_request_id": 10,
            "image_path": "/images/pkg.jpg",
            "display_order": 0,
            "url": "https://example.com/pkg.jpg"
        }"""
        val img = json.decodeFromString<PackageImageJson>(raw)
        assertEquals(1, img.id)
        assertEquals(10, img.packageRequestId)
        assertEquals("https://example.com/pkg.jpg", img.url)
        assertEquals(0, img.displayOrder)
    }

    // -- CreatePackageRequestJson encoding --

    @Test
    fun `CreatePackageRequestJson encodes all fields`() {
        val req = CreatePackageRequestJson(
            pickupAddress = "123 Main",
            pickupCity = "Toronto",
            pickupCountry = "Canada",
            deliveryAddress = "456 Oak",
            deliveryCity = "Montreal",
            deliveryCountry = "Canada",
            packageWeightKg = 10.0,
            packageType = "electronics",
            fragile = true,
            urgencyLevel = "high",
            pickupDatePreferred = "2026-04-05",
            pickupDateFlexible = false,
            deliveryDateNeeded = "2026-04-07",
        )
        val encoded = json.encodeToString(CreatePackageRequestJson.serializer(), req)
        assertTrue(encoded.contains("\"pickup_city\":\"Toronto\""))
        assertTrue(encoded.contains("\"package_weight_kg\":10.0"))
        assertTrue(encoded.contains("\"fragile\":true"))
    }

    // -- CreateServiceRequestBodyJson --

    @Test
    fun `CreateServiceRequestBodyJson encodes`() {
        val req = CreateServiceRequestBodyJson(
            serviceType = "grocery_shopping",
            shoppingList = listOf(
                ShoppingItemJson(item = "Milk", quantity = "2L"),
            ),
            deliveryCity = "Toronto",
            deliveryAddress = "123 Main St",
        )
        val encoded = json.encodeToString(CreateServiceRequestBodyJson.serializer(), req)
        assertTrue(encoded.contains("\"service_type\":\"grocery_shopping\""))
        assertTrue(encoded.contains("\"item\":\"Milk\""))
    }
}
