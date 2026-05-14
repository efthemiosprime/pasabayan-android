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

    @Test
    fun `PackageRequestJson decodes shopping list array payload`() {
        val raw = """
            {
              "id": 210,
              "service_type": "grocery_shopping",
              "shopping_list": [
                {"item":"Cheese Burger","quantity":"1","notes":"Well-done"},
                {"item":"Iced Coffee","quantity":"1","notes":"Vanilla syrup"}
              ],
              "pickup_city": "Montreal",
              "delivery_city": "Montreal"
            }
        """.trimIndent()
        val pkg = json.decodeFromString<PackageRequestJson>(raw)
        assertEquals(210, pkg.id)
        assertEquals("grocery_shopping", pkg.serviceType)
        assertNotNull(pkg.shoppingList)
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

    // -- Coordinates + task fields (read-side parity gaps 2 + 3) --

    @Test
    fun `PackageRequestJson decodes pickup and delivery coordinates`() {
        val raw = """
            {
              "id": 100,
              "pickup_lat": 43.6532,
              "pickup_lng": "-79.3832",
              "delivery_lat": 45.5017,
              "delivery_lng": -73.5673
            }
        """.trimIndent()
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        // FlexibleDoubleSerializer must accept both numeric and string lat/lng.
        assertEquals(43.6532, pkg.pickupLat!!, 0.0001)
        assertEquals(-79.3832, pkg.pickupLng!!, 0.0001)
        assertEquals(45.5017, pkg.deliveryLat!!, 0.0001)
        assertEquals(-73.5673, pkg.deliveryLng!!, 0.0001)
    }

    @Test
    fun `PackageRequestJson decodes task name and description for general_errand`() {
        val raw = """
            {
              "id": 101,
              "service_type": "general_errand",
              "task_name": "Water the plants",
              "task_description": "Tuesdays and Fridays for the next two weeks"
            }
        """.trimIndent()
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        assertEquals("Water the plants", pkg.taskName)
        assertEquals("Tuesdays and Fridays for the next two weeks", pkg.taskDescription)
    }

    @Test
    fun `PackageRequestJson decodes direction and recipient fields for send errand`() {
        val raw = """
            {
              "id": 102,
              "service_type": "general_errand",
              "direction": "send",
              "recipient_name": "Jane Doe",
              "recipient_phone": "+15145551234"
            }
        """.trimIndent()
        val pkg = json.decodeFromString<PackageRequestJson>(raw)

        assertEquals("send", pkg.direction)
        assertEquals("Jane Doe", pkg.recipientName)
        assertEquals("+15145551234", pkg.recipientPhone)
    }

    @Test
    fun `AvailablePackageJson decodes errand fields`() {
        val raw = """
            {
              "id": 30,
              "pickup_city": "Montreal",
              "delivery_city": "Toronto",
              "service_type": "general_errand",
              "direction": "task",
              "store_name": "Metro Plus",
              "recipient_name": "Jane Doe",
              "recipient_phone": "+15145551234",
              "task_name": "Walk my dog",
              "task_description": "Daily walk, 30 min."
            }
        """.trimIndent()
        val pkg = json.decodeFromString<AvailablePackageJson>(raw)

        assertEquals("task", pkg.direction)
        assertEquals("Metro Plus", pkg.storeName)
        assertEquals("Jane Doe", pkg.recipientName)
        assertEquals("+15145551234", pkg.recipientPhone)
        assertEquals("Walk my dog", pkg.taskName)
        assertEquals("Daily walk, 30 min.", pkg.taskDescription)
    }

    @Test
    fun `AvailablePackageJson tolerates unknown links and quality_score`() {
        // Compatible-packages endpoint adds `quality_score` (Int) and the paginator
        // envelope carries `links[]`. Both must be tolerated by the decoder.
        val raw = """
            {
              "id": 31,
              "pickup_city": "Montreal",
              "delivery_city": "Toronto",
              "quality_score": 99,
              "links": [{"url": "...", "label": "1", "active": true}]
            }
        """.trimIndent()
        val pkg = json.decodeFromString<AvailablePackageJson>(raw)
        assertEquals(31, pkg.id)
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
