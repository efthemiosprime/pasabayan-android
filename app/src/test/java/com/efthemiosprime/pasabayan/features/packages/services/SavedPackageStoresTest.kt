package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SavedPackageStoresTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val stringValues = mutableMapOf<String, String>()

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk()
        every { prefs.edit() } returns editor
        every { prefs.getString(any(), any()) } answers {
            stringValues[arg<String>(0)] ?: arg<String?>(1)
        }
        every { editor.putString(any(), any()) } answers {
            val key = arg<String>(0)
            val payload = arg<String?>(1)
            if (payload == null) {
                stringValues.remove(key)
            } else {
                stringValues[key] = payload
            }
            editor
        }
        every { editor.apply() } just runs
    }

    @Test
    fun `saved descriptions keeps latest 15 and dedups case insensitive`() {
        val store = SavedPackageDescriptionsStore(prefs, Json)
        repeat(16) { index ->
            store.save("Description $index")
        }
        store.save("description 15")
        val all = store.getAll()
        assertEquals(15, all.size)
        assertEquals("description 15", all.first())
        assertTrue(all.none { it == "Description 0" })
    }

    @Test
    fun `pickup and handoff templates are capped at five`() {
        val store = SavedPackageRouteTemplatesStore(prefs, Json)
        repeat(6) { index ->
            store.savePickupTemplate(
                PickupTemplate(
                    pickupCountryCode = "CA",
                    pickupCity = "Pickup$index",
                    createdAt = index.toLong(),
                ),
            )
            store.saveHandoffTemplate(
                HandoffTemplate(
                    deliveryCountryCode = "CA",
                    deliveryCity = "Handoff$index",
                    createdAt = index.toLong(),
                ),
            )
        }

        val pickups = store.getPickupTemplates()
        val handoffs = store.getHandoffTemplates()
        assertEquals(5, pickups.size)
        assertEquals(5, handoffs.size)
        assertTrue(pickups.none { it.pickupCity == "Pickup0" })
        assertTrue(handoffs.none { it.deliveryCity == "Handoff0" })
    }
}
