package com.efthemiosprime.pasabayan.features.trips.services

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TripTutorialStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var store: TripTutorialStore
    private val values = mutableMapOf<String, Any>()

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk()

        every { prefs.edit() } returns editor
        every { prefs.getBoolean(any(), any()) } answers {
            values[arg<String>(0)] as? Boolean ?: arg<Boolean>(1)
        }
        every { editor.putBoolean(any(), any()) } answers {
            values[arg<String>(0)] = arg<Boolean>(1)
            editor
        }
        every { editor.remove(any()) } answers {
            values.remove(arg<String>(0))
            editor
        }
        every { editor.apply() } just runs

        store = TripTutorialStore(prefs)
    }

    @Test
    fun `tutorial flag is false by default`() {
        assertFalse(store.hasSeenTutorial(userId = 42L))
    }

    @Test
    fun `mark tutorial seen affects only specific user`() {
        store.markTutorialSeen(userId = 101L)

        assertTrue(store.hasSeenTutorial(userId = 101L))
        assertFalse(store.hasSeenTutorial(userId = 202L))
    }

    @Test
    fun `clear tutorial seen resets user flag`() {
        store.markTutorialSeen(userId = 77L)
        assertTrue(store.hasSeenTutorial(userId = 77L))

        store.clearTutorialSeen(userId = 77L)

        assertFalse(store.hasSeenTutorial(userId = 77L))
    }
}
