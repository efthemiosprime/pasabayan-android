package com.efthemiosprime.pasabayan.features.trips.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarrierPreferencesFormStoreTest {
    private lateinit var store: CarrierPreferencesFormStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("carrier_preferences_form_store_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = CarrierPreferencesFormStore(prefs)
    }

    @Test
    fun acknowledgement_is_scoped_per_user() {
        assertFalse(store.isAcknowledged(101))

        store.markAcknowledged(101)

        assertTrue(store.isAcknowledged(101))
        assertFalse(store.isAcknowledged(202))
    }

    @Test
    fun clear_all_removes_all_keys() {
        store.markAcknowledged(101)
        store.markAcknowledged(202)

        store.clearAllAcknowledgements()

        assertFalse(store.isAcknowledged(101))
        assertFalse(store.isAcknowledged(202))
    }
}
