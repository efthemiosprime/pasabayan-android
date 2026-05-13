package com.efthemiosprime.pasabayan.features.shipper.services

import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.features.shipper.model.RecentSearchEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecentSearchStoreTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var stored: String? = null
    private lateinit var json: Json
    private lateinit var store: RecentSearchStore

    @Before
    fun setUp() {
        json = Json { ignoreUnknownKeys = true }
        stored = null
        prefs = mockk()
        editor = mockk(relaxed = true)
        every { prefs.edit() } returns editor
        every { prefs.getString(RecentSearchStore.KEY, null) } answers { stored }
        val captured = slot<String>()
        every { editor.putString(RecentSearchStore.KEY, capture(captured)) } answers {
            stored = captured.captured
            editor
        }
        every { editor.remove(RecentSearchStore.KEY) } answers {
            stored = null
            editor
        }
        store = RecentSearchStore(prefs, json)
    }

    @Test
    fun `fetch returns empty list when key absent`() {
        assertTrue(store.fetch().isEmpty())
    }

    @Test
    fun `fetch tolerates corrupt JSON`() {
        stored = "not-valid-json"
        assertTrue(store.fetch().isEmpty())
    }

    @Test
    fun `save writes new entry at head`() {
        val updated = store.save("Toronto", "ca")
        assertEquals(1, updated.size)
        assertEquals("Toronto", updated[0].city)
        // country is normalized to upper-case before persisting + display
        assertEquals("CA", updated[0].country)
        verify { editor.apply() }
    }

    @Test
    fun `save dedupes case-insensitively and re-promotes to head`() {
        store.save("Montreal", "CA")
        store.save("Toronto", "CA")
        // Re-save Montreal with different casing — must collapse to a single entry, head.
        val updated = store.save("montreal", "ca")
        assertEquals(2, updated.size)
        assertEquals("montreal", updated[0].city) // newest casing wins at head
        assertEquals("Toronto", updated[1].city)
    }

    @Test
    fun `save caps the list at MAX_ENTRIES (8)`() {
        repeat(12) { store.save("City$it", "CA") }
        assertEquals(RecentSearchStore.MAX_ENTRIES, store.fetch().size)
        // Newest at head, oldest 4 dropped.
        assertEquals("City11", store.fetch().first().city)
        assertEquals("City4", store.fetch().last().city)
    }

    @Test
    fun `save no-ops on blank inputs`() {
        store.save("Toronto", "CA")
        val before = store.fetch()
        val afterBlankCity = store.save("   ", "CA")
        val afterBlankCountry = store.save("Vancouver", "  ")
        assertEquals(before, afterBlankCity)
        assertEquals(before, afterBlankCountry)
    }

    @Test
    fun `displayName formats as 'City, COUNTRY'`() {
        val entry = RecentSearchEntry(city = "Calgary", country = "ca")
        assertEquals("Calgary, CA", entry.displayName)
    }

    @Test
    fun `clear empties the list`() {
        store.save("Toronto", "CA")
        assertEquals(1, store.fetch().size)
        store.clear()
        assertTrue(store.fetch().isEmpty())
    }
}
