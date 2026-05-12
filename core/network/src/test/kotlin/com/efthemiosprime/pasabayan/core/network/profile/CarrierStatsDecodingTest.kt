package com.efthemiosprime.pasabayan.core.network.profile

import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Resilient decoding for `/api/carrier/stats`, `/api/carrier/status`, and `/api/profile/stats`.
 * Backend inconsistently returns numeric fields as either JSON numbers (`0.95`) or strings
 * (`"0.95"`); these tests pin the `FlexibleDoubleSerializer` behavior so a server change
 * doesn't silently break the carrier dashboard. Mirrors iOS `CarrierStatsDecodingTests`.
 */
class CarrierStatsDecodingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ---- CarrierStats envelope + sub-models ----

    @Test
    fun `CarrierStatsResponseJson decodes full payload with numeric doubles`() {
        val raw = """
        {
          "success": true,
          "message": "ok",
          "data": {
            "deliveries": {
              "total_trips": 10,
              "active_trips": 2,
              "completed_trips": 7,
              "total_matches": 25,
              "completed_matches": 20,
              "success_rate": 0.95
            },
            "ratings": {
              "average_rating": "4.8",
              "total_ratings": 32,
              "response_time_hours": 1.5,
              "reliability_score": 90
            },
            "earnings": {
              "total_earnings": 1250.50,
              "monthly_earnings": 320.0,
              "average_per_delivery": 62.5,
              "pending_payments": 80.0,
              "top_routes": [
                { "route": "Montreal -> Toronto", "trips": 5, "potential_earnings": 200.0 }
              ]
            },
            "profile_complete": true
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        val data = res.data!!
        assertEquals(10, data.deliveries!!.totalTrips)
        assertEquals(0.95, data.deliveries.successRate!!, 0.001)
        assertEquals("4.8", data.ratings!!.averageRating)
        assertEquals(1.5, data.ratings.responseTimeHours!!, 0.001)
        assertEquals(1250.50, data.earnings!!.totalEarnings!!, 0.001)
        assertEquals(80.0, data.earnings.pendingPayments!!, 0.001)
        assertEquals(1, data.earnings.topRoutes!!.size)
        assertEquals(200.0, data.earnings.topRoutes[0].potentialEarnings!!, 0.001)
        assertTrue(data.profileComplete!!)
    }

    @Test
    fun `CarrierStats decodes when numeric fields arrive as strings`() {
        // Backend has been observed returning every "double" as a string.
        val raw = """
        {
          "data": {
            "deliveries": { "success_rate": "0.95" },
            "ratings": { "response_time_hours": "1.5" },
            "earnings": {
              "total_earnings": "1250.50",
              "monthly_earnings": "320.0",
              "average_per_delivery": "62.5",
              "pending_payments": "80.0",
              "top_routes": [ { "potential_earnings": "200.0" } ]
            }
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        val data = res.data!!
        assertEquals(0.95, data.deliveries!!.successRate!!, 0.001)
        assertEquals(1.5, data.ratings!!.responseTimeHours!!, 0.001)
        assertEquals(1250.50, data.earnings!!.totalEarnings!!, 0.001)
        assertEquals(320.0, data.earnings.monthlyEarnings!!, 0.001)
        assertEquals(62.5, data.earnings.averagePerDelivery!!, 0.001)
        assertEquals(80.0, data.earnings.pendingPayments!!, 0.001)
        assertEquals(200.0, data.earnings.topRoutes!![0].potentialEarnings!!, 0.001)
    }

    @Test
    fun `CarrierStats sub-models are all optional`() {
        // A freshly-onboarded carrier may have no deliveries / ratings / earnings.
        val raw = """{ "success": true, "data": {} }""".trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        val data = res.data!!
        assertNull(data.deliveries)
        assertNull(data.ratings)
        assertNull(data.earnings)
        assertNull(data.profileComplete)
    }

    @Test
    fun `CarrierStats decodes when data envelope is absent`() {
        val raw = """{ "success": false, "message": "not a carrier" }""".trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        assertFalse(res.success)
        assertEquals("not a carrier", res.message)
        assertNull(res.data)
    }

    @Test
    fun `CarrierEarnings decodes when top_routes is missing`() {
        val raw = """
        {
          "data": { "earnings": { "total_earnings": 100.0 } }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        assertNull(res.data!!.earnings!!.topRoutes)
    }

    @Test
    fun `CarrierEarnings decodes when top_routes is an empty array`() {
        val raw = """
        {
          "data": { "earnings": { "top_routes": [] } }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        assertEquals(0, res.data!!.earnings!!.topRoutes!!.size)
    }

    @Test
    fun `CarrierStats tolerates unknown keys`() {
        // ignoreUnknownKeys = true; future backend fields shouldn't break decoding.
        val raw = """
        {
          "data": {
            "deliveries": { "total_trips": 1, "new_future_field": "x" },
            "unrelated": 42
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatsResponseJson>(raw)
        assertEquals(1, res.data!!.deliveries!!.totalTrips)
    }

    // ---- UserStats envelope ----

    @Test
    fun `UserStatsResponseJson decodes numeric averageRating`() {
        val raw = """
        {
          "success": true,
          "data": {
            "packages_count": 5,
            "delivered_count": 3,
            "average_rating": 4.6,
            "total_ratings": 12,
            "ratings_count": 12
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<UserStatsResponseJson>(raw)
        assertEquals(4.6, res.data!!.averageRating!!, 0.001)
    }

    @Test
    fun `UserStatsResponseJson decodes stringified averageRating`() {
        val raw = """
        {
          "data": { "average_rating": "4.6", "packages_count": 5 }
        }
        """.trimIndent()
        val res = json.decodeFromString<UserStatsResponseJson>(raw)
        assertEquals(4.6, res.data!!.averageRating!!, 0.001)
        assertEquals(5, res.data.packagesCount)
    }

    // ---- CarrierStatus envelope + UserTypesSerializer ----

    @Test
    fun `CarrierStatusDataJson decodes userTypes as JSON array`() {
        val raw = """
        {
          "data": {
            "is_active_carrier": true,
            "carrier_status": "active",
            "has_carrier_profile": true,
            "profile_recommended": false,
            "user_types": ["shipper", "carrier"]
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatusResponseJson>(raw)
        val data = res.data!!
        assertEquals(true, data.isActiveCarrier)
        assertEquals(listOf("shipper", "carrier"), data.userTypes)
    }

    @Test
    fun `CarrierStatusDataJson decodes userTypes as dictionary of strings`() {
        // Laravel's `array_values` style: {"0":"shipper","1":"carrier"}
        val raw = """
        {
          "data": {
            "user_types": { "0": "shipper", "1": "carrier" }
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatusResponseJson>(raw)
        // Order isn't strictly guaranteed from a JsonObject; assert membership instead.
        assertNotNull(res.data!!.userTypes)
        val set = res.data.userTypes!!.toSet()
        assertEquals(setOf("shipper", "carrier"), set)
    }

    @Test
    fun `CarrierStatusDataJson decodes userTypes as key-flag dictionary`() {
        // `{"shipper":true,"carrier":false}` style — falls back to keys.
        val raw = """
        {
          "data": {
            "user_types": { "shipper": true, "carrier": false }
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<CarrierStatusResponseJson>(raw)
        assertEquals(setOf("shipper", "carrier"), res.data!!.userTypes!!.toSet())
    }

    @Test
    fun `UserSummary userTypes round-trips through array form`() {
        // Sanity check the shared serializer behaves the same on UserSummary as it
        // does on CarrierStatusDataJson — both use the same UserTypesSerializer.
        val raw = """
        { "id": 1, "name": "Alice", "user_types": ["shipper"] }
        """.trimIndent()
        val user = json.decodeFromString<UserSummary>(raw)
        assertEquals(listOf("shipper"), user.userTypes)
    }
}
