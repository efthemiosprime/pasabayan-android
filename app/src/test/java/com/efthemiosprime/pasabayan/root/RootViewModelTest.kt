package com.efthemiosprime.pasabayan.root

import com.efthemiosprime.pasabayan.onboarding.OnboardingPreferences
import com.efthemiosprime.pasabayan.onboarding.OnboardingRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeOnboardingPreferences(
        var completed: Boolean,
        var preferredRole: String? = null,
        var viewedCarrier: Boolean = false,
        var viewedSender: Boolean = false,
    ) : OnboardingPreferences {
        override suspend fun hasCompletedOnboarding(): Boolean = completed

        override suspend fun setHasCompletedOnboarding(completed: Boolean) {
            this.completed = completed
        }

        override suspend fun hasViewedCarrierJourney(): Boolean = viewedCarrier

        override suspend fun setHasViewedCarrierJourney(viewed: Boolean) {
            viewedCarrier = viewed
        }

        override suspend fun hasViewedSenderJourney(): Boolean = viewedSender

        override suspend fun setHasViewedSenderJourney(viewed: Boolean) {
            viewedSender = viewed
        }

        override suspend fun getPreferredRoleWire(): String? = preferredRole

        override suspend fun setPreferredRoleWire(roleWire: String) {
            preferredRole = roleWire
        }

        var citySetupCompleted: Boolean = false

        override suspend fun hasCompletedCitySetup(): Boolean = citySetupCompleted

        override suspend fun setHasCompletedCitySetup(completed: Boolean) {
            citySetupCompleted = completed
        }
    }

    @Test
    fun whenNotCompleted_showsOnboarding() = runTest {
        val prefs = FakeOnboardingPreferences(completed = false)
        val vm = RootViewModel(prefs)
        assertEquals(OnboardingBootstrapUi.ShowOnboarding, vm.onboardingBootstrap.value)
    }

    @Test
    fun whenCompleted_showsMain() = runTest {
        val prefs = FakeOnboardingPreferences(completed = true)
        val vm = RootViewModel(prefs)
        assertEquals(OnboardingBootstrapUi.ShowMain, vm.onboardingBootstrap.value)
    }

    @Test
    fun completeOnboarding_movesToMainAndPersists() = runTest {
        val prefs = FakeOnboardingPreferences(completed = false)
        val vm = RootViewModel(prefs)
        assertEquals(OnboardingBootstrapUi.ShowOnboarding, vm.onboardingBootstrap.value)
        vm.completeOnboardingWithPreferredRole(OnboardingRole.Carrier)
        assertEquals(OnboardingBootstrapUi.ShowMain, vm.onboardingBootstrap.value)
        assertEquals(true, prefs.completed)
        assertEquals("carrier", prefs.preferredRole)
    }
}
