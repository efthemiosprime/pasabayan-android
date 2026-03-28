package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.features.onboarding.model.CityPickerOption
import com.efthemiosprime.pasabayan.features.onboarding.services.CityOnboardingRepository
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityOnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeCityOnboardingRepository
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCityOnboardingRepository()
        mockContext = mockk(relaxed = true) {
            every { applicationContext } returns this
            every { getString(any()) } returns "error"
            every { getString(any(), any()) } returns "error"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CityOnboardingViewModel =
        CityOnboardingViewModel(mockContext, fakeRepo)

    // ── Initial state ──

    @Test
    fun `initial state has empty cities and isLoadingCities true`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertTrue(state.cities.isEmpty())
        assertTrue(state.isLoadingCities)
        assertNull(state.selectedCity)
        assertNull(state.loadErrorMessage)
    }

    // ── loadCities ──

    @Test
    fun `loadCities success populates cities list`() = runTest {
        val cities = listOf(
            CityPickerOption(1, "Toronto"),
            CityPickerOption(2, "Vancouver"),
        )
        fakeRepo.fetchCitiesResult = Result.success(cities)

        val vm = createViewModel()
        vm.loadCities()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(cities, state.cities)
        assertFalse(state.isLoadingCities)
        assertNull(state.loadErrorMessage)
    }

    @Test
    fun `loadCities failure sets loadErrorMessage`() = runTest {
        fakeRepo.fetchCitiesResult = Result.failure(RuntimeException("network error"))

        val vm = createViewModel()
        vm.loadCities()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoadingCities)
        assertTrue(state.loadErrorMessage != null)
        assertTrue(state.cities.isEmpty())
    }

    // ── selectCity ──

    @Test
    fun `selectCity updates selectedCity and closes picker`() = runTest {
        val vm = createViewModel()
        vm.setShowPickerSheet(true)
        vm.setSearchQuery("Tor")

        val city = CityPickerOption(1, "Toronto")
        vm.selectCity(city)

        val state = vm.uiState.value
        assertEquals(city, state.selectedCity)
        assertFalse(state.showPickerSheet)
        assertEquals("", state.searchQuery)
        assertNull(state.saveErrorMessage)
    }

    // ── Picker sheet ──

    @Test
    fun `setShowPickerSheet true opens picker`() = runTest {
        val vm = createViewModel()
        vm.setShowPickerSheet(true)
        assertTrue(vm.uiState.value.showPickerSheet)
    }

    @Test
    fun `setShowPickerSheet false closes picker and clears search`() = runTest {
        val vm = createViewModel()
        vm.setShowPickerSheet(true)
        vm.setSearchQuery("Van")
        vm.setShowPickerSheet(false)

        val state = vm.uiState.value
        assertFalse(state.showPickerSheet)
        assertEquals("", state.searchQuery)
    }

    // ── Search ──

    @Test
    fun `setSearchQuery updates searchQuery`() = runTest {
        val vm = createViewModel()
        vm.setSearchQuery("Mon")
        assertEquals("Mon", vm.uiState.value.searchQuery)
    }

    // ── onUseMyLocationStub ──

    @Test
    fun `onUseMyLocationStub sets showLocationStubMessage`() = runTest {
        val vm = createViewModel()
        vm.onUseMyLocationStub()
        assertTrue(vm.uiState.value.showLocationStubMessage)
    }

    // ── saveAndContinue ──

    @Test
    fun `saveAndContinue with no city sets saveErrorMessage and does not call onFinished`() = runTest {
        val vm = createViewModel()
        var finished = false

        vm.saveAndContinue { finished = true }
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saveErrorMessage != null)
        assertFalse(finished)
    }

    @Test
    fun `saveAndContinue with selected city calls updateHomeCity and invokes onFinished`() = runTest {
        fakeRepo.updateHomeCityResult = Result.success(Unit)
        val vm = createViewModel()
        vm.selectCity(CityPickerOption(5, "Calgary"))

        var finished = false
        vm.saveAndContinue { finished = true }
        advanceUntilIdle()

        assertEquals(5, fakeRepo.lastUpdatedCityId)
        assertTrue(finished)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `saveAndContinue with API failure sets saveErrorMessage but still calls onFinished`() = runTest {
        fakeRepo.updateHomeCityResult = Result.failure(RuntimeException("save failed"))
        val vm = createViewModel()
        vm.selectCity(CityPickerOption(3, "Ottawa"))

        var finished = false
        vm.saveAndContinue { finished = true }
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saveErrorMessage != null)
        assertTrue(finished)
    }
}

private class FakeCityOnboardingRepository : CityOnboardingRepository {
    var fetchCitiesResult: Result<List<CityPickerOption>> = Result.success(emptyList())
    var updateHomeCityResult: Result<Unit> = Result.success(Unit)
    var lastUpdatedCityId: Int? = null

    override suspend fun fetchCitiesCanada(): Result<List<CityPickerOption>> = fetchCitiesResult

    override suspend fun updateHomeCity(cityId: Int): Result<Unit> {
        lastUpdatedCityId = cityId
        return updateHomeCityResult
    }
}
