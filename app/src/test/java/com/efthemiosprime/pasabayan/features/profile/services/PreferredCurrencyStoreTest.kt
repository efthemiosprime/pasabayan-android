package com.efthemiosprime.pasabayan.features.profile.services

import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.features.profile.model.CurrencyPreference
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreferredCurrencyStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var store: PreferredCurrencyStore
    private val values = mutableMapOf<String, Any?>()

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk()
        every { prefs.edit() } returns editor
        every { prefs.getString(any(), any()) } answers {
            values[arg<String>(0)] as? String ?: arg<String?>(1)
        }
        every { editor.putString(any(), any()) } answers {
            values[arg<String>(0)] = arg<String?>(1)
            editor
        }
        every { editor.apply() } just runs
        store = PreferredCurrencyStore(prefs)
    }

    @Test
    fun `defaults to CAD when nothing stored`() {
        assertEquals(CurrencyPreference.CAD, store.get())
    }

    @Test
    fun `set persists the code and reads back`() {
        store.set(CurrencyPreference.PHP)
        assertEquals(CurrencyPreference.PHP, store.get())
    }

    @Test
    fun `unknown stored code falls back to CAD`() {
        values["preferred_currency"] = "BTC"
        assertEquals(CurrencyPreference.CAD, store.get())
    }
}
