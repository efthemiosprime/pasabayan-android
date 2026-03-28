package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingRole
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingStep
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
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePrefs: FakeOnboardingPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePrefs = FakeOnboardingPreferences()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OnboardingViewModel = OnboardingViewModel(fakePrefs)

    // ── Initial state ──

    @Test
    fun `initial step is RoleSelection`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(OnboardingStep.RoleSelection, vm.step.value)
    }

    @Test
    fun `init loads hasViewedCarrierJourney from preferences`() = runTest {
        fakePrefs.carrierJourneyViewed = true
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.hasViewedCarrierJourney.value)
    }

    @Test
    fun `init loads hasViewedSenderJourney from preferences`() = runTest {
        fakePrefs.senderJourneyViewed = true
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.hasViewedSenderJourney.value)
    }

    // ── Role selection ──

    @Test
    fun `selectRole transitions to Journey step 0 for carrier`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)

        val step = vm.step.value
        assertTrue(step is OnboardingStep.Journey)
        assertEquals(OnboardingRole.Carrier, (step as OnboardingStep.Journey).role)
        assertEquals(0, step.stepIndex)
    }

    @Test
    fun `selectRole transitions to Journey step 0 for shipper`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Shipper)

        val step = vm.step.value
        assertTrue(step is OnboardingStep.Journey)
        assertEquals(OnboardingRole.Shipper, (step as OnboardingStep.Journey).role)
        assertEquals(0, step.stepIndex)
    }

    // ── nextStep ──

    @Test
    fun `nextStep increments journey stepIndex`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        vm.nextStep()

        val step = vm.step.value as OnboardingStep.Journey
        assertEquals(1, step.stepIndex)
    }

    @Test
    fun `nextStep past last step transitions to Completion`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        // 4 carrier steps (indices 0-3): advance 4 times to go past last
        repeat(4) { vm.nextStep() }
        advanceUntilIdle()

        val step = vm.step.value
        assertTrue(step is OnboardingStep.Completion)
        assertEquals(OnboardingRole.Carrier, (step as OnboardingStep.Completion).role)
    }

    @Test
    fun `nextStep to Completion persists hasViewedCarrierJourney`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        repeat(4) { vm.nextStep() }
        advanceUntilIdle()

        assertTrue(fakePrefs.carrierJourneyViewed)
        assertTrue(vm.hasViewedCarrierJourney.value)
    }

    @Test
    fun `nextStep to Completion persists hasViewedSenderJourney`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Shipper)
        repeat(4) { vm.nextStep() }
        advanceUntilIdle()

        assertTrue(fakePrefs.senderJourneyViewed)
        assertTrue(vm.hasViewedSenderJourney.value)
    }

    @Test
    fun `nextStep from non-Journey is no-op`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.nextStep() // on RoleSelection

        assertEquals(OnboardingStep.RoleSelection, vm.step.value)
    }

    // ── previousStep ──

    @Test
    fun `previousStep decrements journey stepIndex`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        vm.nextStep()
        vm.nextStep() // at index 2
        vm.previousStep()

        assertEquals(1, (vm.step.value as OnboardingStep.Journey).stepIndex)
    }

    @Test
    fun `previousStep at step 0 returns to RoleSelection`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        vm.previousStep()

        assertEquals(OnboardingStep.RoleSelection, vm.step.value)
    }

    @Test
    fun `previousStep from non-Journey is no-op`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.previousStep()

        assertEquals(OnboardingStep.RoleSelection, vm.step.value)
    }

    // ── skipToCompletion ──

    @Test
    fun `skipToCompletion jumps from Journey to Completion`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Shipper)
        vm.skipToCompletion()
        advanceUntilIdle()

        val step = vm.step.value
        assertTrue(step is OnboardingStep.Completion)
        assertEquals(OnboardingRole.Shipper, (step as OnboardingStep.Completion).role)
    }

    @Test
    fun `skipToCompletion from non-Journey is no-op`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.skipToCompletion()

        assertEquals(OnboardingStep.RoleSelection, vm.step.value)
    }

    // ── exploreOtherRole ──

    @Test
    fun `exploreOtherRole starts Journey at step 0 for opposite role`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectRole(OnboardingRole.Carrier)
        repeat(4) { vm.nextStep() }
        advanceUntilIdle()

        vm.exploreOtherRole(OnboardingRole.Carrier)

        val step = vm.step.value as OnboardingStep.Journey
        assertEquals(OnboardingRole.Shipper, step.role)
        assertEquals(0, step.stepIndex)
    }

    // ── OnboardingRole.opposite ──

    @Test
    fun `carrier opposite is shipper`() {
        assertEquals(OnboardingRole.Shipper, OnboardingRole.Carrier.opposite)
    }

    @Test
    fun `shipper opposite is carrier`() {
        assertEquals(OnboardingRole.Carrier, OnboardingRole.Shipper.opposite)
    }

    // ── totalJourneySteps ──

    @Test
    fun `totalJourneySteps is 4`() = runTest {
        val vm = createViewModel()
        assertEquals(4, vm.totalJourneySteps)
    }
}
