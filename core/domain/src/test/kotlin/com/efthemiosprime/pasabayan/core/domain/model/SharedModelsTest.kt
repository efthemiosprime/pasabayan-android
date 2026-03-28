package com.efthemiosprime.pasabayan.core.domain.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedModelsTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun loadFixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/domain/$name")!!
            .bufferedReader().readText()

    // -- UserSummary --

    @Test
    fun `UserSummary decodes full JSON with all fields`() {
        val fixture = loadFixture("user_summary_full.json")
        val user = json.decodeFromString<UserSummary>(fixture)

        assertEquals(42, user.id)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals("https://example.com/avatar.jpg", user.avatar)
        assertEquals("+1234567890", user.phone)
        assertEquals(true, user.phoneVerified)
        assertEquals(true, user.profileCompleted) // from int 1
        assertEquals(listOf("shipper", "carrier"), user.userTypes)
        assertEquals("4.5", user.rating)
        assertEquals(25, user.totalRatings)
        assertEquals("verified", user.verificationLevel)
        assertEquals(true, user.isActiveCarrier)
        assertEquals(true, user.isActiveShipper) // from "yes"
    }

    @Test
    fun `UserSummary decodes minimal JSON with only id and name`() {
        val fixture = loadFixture("user_summary_minimal.json")
        val user = json.decodeFromString<UserSummary>(fixture)

        assertEquals(1, user.id)
        assertEquals("Jane", user.name)
        assertNull(user.email)
        assertNull(user.avatar)
        assertNull(user.phone)
        assertNull(user.phoneVerified)
        assertNull(user.userTypes)
        assertNull(user.rating)
        assertNull(user.totalRatings)
        assertNull(user.verificationLevel)
    }

    @Test
    fun `UserSummary decodes dict-format userTypes`() {
        val fixture = loadFixture("user_summary_dict_user_types.json")
        val user = json.decodeFromString<UserSummary>(fixture)

        assertNotNull(user.userTypes)
        assertTrue(user.userTypes!!.contains("shipper"))
        assertTrue(user.userTypes!!.contains("carrier"))
    }

    @Test
    fun `UserSummary effectiveVerificationLevel returns correct level`() {
        val verified = UserSummary(id = 1, name = "A", verificationLevel = "verified")
        assertEquals(VerificationLevel.VERIFIED, verified.effectiveVerificationLevel)

        val premium = UserSummary(id = 2, name = "B", verificationLevel = "premium")
        assertEquals(VerificationLevel.PREMIUM, premium.effectiveVerificationLevel)

        val basic = UserSummary(id = 3, name = "C", verificationLevel = null)
        assertEquals(VerificationLevel.BASIC, basic.effectiveVerificationLevel)
    }

    @Test
    fun `UserSummary ratingValue parses string to double`() {
        val user = UserSummary(id = 1, name = "A", rating = "4.5")
        assertEquals(4.5, user.ratingValue!!, 0.001)
    }

    @Test
    fun `UserSummary ratingValue returns null for non-numeric`() {
        val user = UserSummary(id = 1, name = "A", rating = "N/A")
        assertNull(user.ratingValue)
    }

    @Test
    fun `UserSummary ratingValue returns null when rating is null`() {
        val user = UserSummary(id = 1, name = "A")
        assertNull(user.ratingValue)
    }

    @Test
    fun `UserSummary formattedRating formats correctly`() {
        val user = UserSummary(id = 1, name = "A", rating = "4.5")
        assertEquals("4.5", user.formattedRating)
    }

    @Test
    fun `UserSummary formattedRating shows No rating when null`() {
        val user = UserSummary(id = 1, name = "A")
        assertEquals("No rating", user.formattedRating)
    }

    @Test
    fun `UserSummary isVerified delegates to effectiveVerificationLevel`() {
        val verified = UserSummary(id = 1, name = "A", verificationLevel = "verified")
        assertTrue(verified.isVerified)

        val basic = UserSummary(id = 2, name = "B", verificationLevel = "basic")
        assertFalse(basic.isVerified)
    }

    @Test
    fun `UserSummary isPremium delegates to effectiveVerificationLevel`() {
        val premium = UserSummary(id = 1, name = "A", verificationLevel = "premium")
        assertTrue(premium.isPremium)

        val verified = UserSummary(id = 2, name = "B", verificationLevel = "verified")
        assertFalse(verified.isPremium)
    }

    // -- PackageDimensions --

    @Test
    fun `PackageDimensions decodes from JSON object`() {
        val fixture = loadFixture("package_dimensions_object.json")
        val dims = json.decodeFromString<PackageDimensions>(fixture)

        assertEquals(10.0, dims.length, 0.001)
        assertEquals(5.5, dims.width, 0.001)
        assertEquals(3.0, dims.height, 0.001)
    }

    @Test
    fun `PackageDimensions decodes from stringified JSON`() {
        val fixture = loadFixture("package_dimensions_string.json")
        val dims = json.decodeFromString<PackageDimensions>(fixture)

        assertEquals(10.0, dims.length, 0.001)
        assertEquals(5.5, dims.width, 0.001)
        assertEquals(3.0, dims.height, 0.001)
    }

    @Test
    fun `PackageDimensions decodes inline with string values`() {
        val input = """{"length":"20","width":"10","height":"5"}"""
        val dims = json.decodeFromString<PackageDimensions>(input)

        assertEquals(20.0, dims.length, 0.001)
        assertEquals(10.0, dims.width, 0.001)
        assertEquals(5.0, dims.height, 0.001)
    }

    @Test
    fun `PackageDimensions formatted property`() {
        val dims = PackageDimensions(10.0, 5.0, 3.0)
        assertEquals("10.0\u00D75.0\u00D73.0 cm", dims.formatted)
    }

    @Test
    fun `PackageDimensions serialization round-trip`() {
        val original = PackageDimensions(10.0, 5.5, 3.0)
        val encoded = json.encodeToString(PackageDimensions.serializer(), original)
        val decoded = json.decodeFromString<PackageDimensions>(encoded)
        assertEquals(original, decoded)
    }

    // -- CompatibilityDetails --

    @Test
    fun `CompatibilityDetails decodes from JSON`() {
        val input = """{
            "route_match": "exact",
            "capacity_sufficient": true,
            "date_compatible": true,
            "price_compatible": false,
            "weight_usage_percentage": 75.5,
            "space_usage_percentage": 50.0
        }"""
        val details = json.decodeFromString<CompatibilityDetails>(input)

        assertEquals("exact", details.routeMatch)
        assertTrue(details.capacitySufficient)
        assertTrue(details.dateCompatible)
        assertFalse(details.priceCompatible)
        assertEquals(75.5, details.weightUsagePercentage, 0.001)
        assertEquals(50.0, details.spaceUsagePercentage, 0.001)
    }

    @Test
    fun `CompatibilityDetails defaults when fields missing`() {
        val input = """{}"""
        val details = json.decodeFromString<CompatibilityDetails>(input)

        assertNull(details.routeMatch)
        assertFalse(details.capacitySufficient)
        assertFalse(details.dateCompatible)
        assertFalse(details.priceCompatible)
        assertEquals(0.0, details.weightUsagePercentage, 0.001)
        assertEquals(0.0, details.spaceUsagePercentage, 0.001)
    }

    // -- Coordinates --

    @Test
    fun `Coordinates serialization round-trip`() {
        val original = Coordinates(45.5017, -73.5673)
        val encoded = json.encodeToString(Coordinates.serializer(), original)
        val decoded = json.decodeFromString<Coordinates>(encoded)
        assertEquals(original, decoded)
    }
}
