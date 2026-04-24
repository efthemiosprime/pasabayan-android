package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PackageTutorialStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var store: PackageTutorialStore
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
        every { editor.apply() } just runs
        store = PackageTutorialStore(prefs)
    }

    @Test
    fun `tutorial is false by default`() {
        assertFalse(store.hasSeenTutorial(userId = 5L))
    }

    @Test
    fun `mark tutorial seen sets user flag only`() {
        store.markTutorialSeen(userId = 5L)
        assertTrue(store.hasSeenTutorial(userId = 5L))
        assertFalse(store.hasSeenTutorial(userId = 6L))
    }
}
