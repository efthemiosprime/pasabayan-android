package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.profile.model.CurrencyPreference
import com.efthemiosprime.pasabayan.features.profile.services.CacheClearer
import com.efthemiosprime.pasabayan.features.profile.services.PreferredCurrencyStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val currencyStore: PreferredCurrencyStore = mockk(relaxed = true)
    private val cacheClearer: CacheClearer = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { currencyStore.get() } returns CurrencyPreference.CAD
        every {
            context.getString(R.string.profile_settings_cache_cleared_formatted, any<Long>())
        } answers {
            "Cache cleared (${secondArg<Array<Any?>>()[0]} KB)"
        }
        every { context.getString(R.string.profile_settings_cache_cleared_error) } returns
            "Clear cache failed"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reads currency from store`() {
        val vm = SettingsViewModel(currencyStore, cacheClearer, context)
        assertEquals(CurrencyPreference.CAD, vm.state.value.currency)
    }

    @Test
    fun `onCurrencyChange persists and updates state`() {
        val vm = SettingsViewModel(currencyStore, cacheClearer, context)
        vm.onCurrencyChange(CurrencyPreference.PHP)
        assertEquals(CurrencyPreference.PHP, vm.state.value.currency)
        verify(exactly = 1) { currencyStore.set(CurrencyPreference.PHP) }
    }

    @Test
    fun `clearCache success surfaces formatted message`() = runTest(dispatcher) {
        coEvery { cacheClearer.clear() } returns CacheClearer.ClearCacheResult(bytesFreed = 5120L)
        val vm = SettingsViewModel(currencyStore, cacheClearer, context)
        vm.clearCache()
        advanceUntilIdle()
        assertFalse(vm.state.value.isClearingCache)
        assertEquals("Cache cleared (5 KB)", vm.state.value.infoMessage)
    }

    @Test
    fun `clearCache failure surfaces error`() = runTest(dispatcher) {
        coEvery { cacheClearer.clear() } throws IllegalStateException("nope")
        val vm = SettingsViewModel(currencyStore, cacheClearer, context)
        vm.clearCache()
        advanceUntilIdle()
        assertFalse(vm.state.value.isClearingCache)
        assertNotNull(vm.state.value.errorMessage)
    }
}
