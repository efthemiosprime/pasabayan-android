package com.efthemiosprime.pasabayan.features.trips.services

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

class SavedRouteTemplatesStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var store: SavedRouteTemplatesStore
    private val values = mutableMapOf<String, String>()
    private val key = "saved_route_templates_v1"

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk()

        every { prefs.edit() } returns editor
        every { prefs.getString(any(), any()) } answers {
            values[arg<String>(0)] ?: arg<String?>(1)
        }
        every { editor.putString(any(), any()) } answers {
            val storageKey = arg<String>(0)
            val payload = arg<String?>(1)
            if (payload == null) {
                values.remove(storageKey)
            } else {
                values[storageKey] = payload
            }
            editor
        }
        every { editor.remove(any()) } answers {
            values.remove(arg<String>(0))
            editor
        }
        every { editor.apply() } just runs

        store = SavedRouteTemplatesStore(prefs, Json)
    }

    @Test
    fun `save keeps newest five routes only`() {
        repeat(6) { index ->
            store.save(
                SavedRouteTemplate(
                    startCountryCode = "CA",
                    startLocation = "Start$index",
                    endCountryCode = "CA",
                    endLocation = "End$index",
                    createdAt = index.toLong(),
                ),
            )
        }

        val all = store.getAll()
        assertEquals(5, all.size)
        assertTrue(all.none { it.startLocation == "Start0" })
    }

    @Test
    fun `getAll returns templates sorted by createdAt descending`() {
        values[key] = Json.encodeToString(
            listOf(
                SavedRouteTemplate("CA", "Older", null, "CA", "A", null, createdAt = 100L),
                SavedRouteTemplate("CA", "Newest", null, "CA", "B", null, createdAt = 300L),
                SavedRouteTemplate("CA", "Middle", null, "CA", "C", null, createdAt = 200L),
            ),
        )

        val all = store.getAll()
        assertEquals(listOf("Newest", "Middle", "Older"), all.map { it.startLocation })
    }
}
