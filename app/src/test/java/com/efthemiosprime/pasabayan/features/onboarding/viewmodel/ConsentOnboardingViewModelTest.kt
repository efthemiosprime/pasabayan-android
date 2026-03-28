package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import com.efthemiosprime.pasabayan.features.onboarding.model.ConsentSelections
import com.efthemiosprime.pasabayan.features.onboarding.services.ConsentOnboardingRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentOnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeConsentOnboardingRepository
    private lateinit var fakePrefs: FakeOnboardingPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeConsentOnboardingRepository()
        fakePrefs = FakeOnboardingPreferences()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ConsentOnboardingViewModel =
        ConsentOnboardingViewModel(fakeRepo, fakePrefs)

    // ── Initial state ──

    @Test
    fun `initial state has all consents false and not saving`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertEquals(ConsentSelections(), state.selections)
        assertFalse(state.isSaving)
    }

    // ── Toggle selections ──

    @Test
    fun `setPushNotifications updates selection`() = runTest {
        val vm = createViewModel()
        vm.setPushNotifications(true)
        assertTrue(vm.uiState.value.selections.pushNotifications)
    }

    @Test
    fun `setLocationTracking updates selection`() = runTest {
        val vm = createViewModel()
        vm.setLocationTracking(true)
        assertTrue(vm.uiState.value.selections.locationTracking)
    }

    @Test
    fun `setAnalytics updates selection`() = runTest {
        val vm = createViewModel()
        vm.setAnalytics(true)
        assertTrue(vm.uiState.value.selections.analytics)
    }

    @Test
    fun `setMarketingCommunications updates selection`() = runTest {
        val vm = createViewModel()
        vm.setMarketingCommunications(true)
        assertTrue(vm.uiState.value.selections.marketingCommunications)
    }

    @Test
    fun `toggling selection off works`() = runTest {
        val vm = createViewModel()
        vm.setPushNotifications(true)
        vm.setPushNotifications(false)
        assertFalse(vm.uiState.value.selections.pushNotifications)
    }

    // ── saveAndContinue ──

    @Test
    fun `saveAndContinue success persists push opt-in and calls onFinished`() = runTest {
        fakeRepo.updateResult = Result.success(Unit)
        val vm = createViewModel()
        vm.setPushNotifications(true)
        vm.setAnalytics(true)

        var receivedPush: Boolean? = null
        vm.saveAndContinue { receivedPush = it }
        advanceUntilIdle()

        assertEquals(true, receivedPush)
        assertTrue(fakePrefs.pushConsentOptIn)
        assertFalse(vm.uiState.value.isSaving)
        assertEquals(
            ConsentSelections(pushNotifications = true, analytics = true),
            fakeRepo.lastSelections,
        )
    }

    @Test
    fun `saveAndContinue failure does not persist push opt-in but still calls onFinished`() = runTest {
        fakeRepo.updateResult = Result.failure(RuntimeException("server error"))
        val vm = createViewModel()
        vm.setPushNotifications(true)

        var receivedPush: Boolean? = null
        vm.saveAndContinue { receivedPush = it }
        advanceUntilIdle()

        assertEquals(true, receivedPush)
        assertFalse(fakePrefs.pushConsentOptIn) // not persisted on failure
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `saveAndContinue with all false reports pushNotifications false`() = runTest {
        fakeRepo.updateResult = Result.success(Unit)
        val vm = createViewModel()

        var receivedPush: Boolean? = null
        vm.saveAndContinue { receivedPush = it }
        advanceUntilIdle()

        assertEquals(false, receivedPush)
        assertFalse(fakePrefs.pushConsentOptIn)
    }
}

private class FakeConsentOnboardingRepository : ConsentOnboardingRepository {
    var updateResult: Result<Unit> = Result.success(Unit)
    var lastSelections: ConsentSelections? = null

    override suspend fun updateConsentPreferences(selections: ConsentSelections): Result<Unit> {
        lastSelections = selections
        return updateResult
    }
}
