package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import org.junit.Assert.assertEquals
import org.junit.Test

class TripCreationFlowStateTest {

    @Test
    fun `nextStep increments current step until last step`() {
        val flow = TripCreationFlowState(currentStep = 3)
        val next = flow.nextStep(lastWizardStep = 4)
        val capped = next.nextStep(lastWizardStep = 4)

        assertEquals(4, next.currentStep)
        assertEquals(4, capped.currentStep)
    }

    @Test
    fun `previousStep does not go below zero`() {
        val flow = TripCreationFlowState(currentStep = 0)
        val previous = flow.previousStep()

        assertEquals(0, previous.currentStep)
    }

    @Test
    fun `switchToReview and switchToWizard updates mode`() {
        val flow = TripCreationFlowState()
        val review = flow.switchToReview()
        val wizard = review.switchToWizard()

        assertEquals(TripCreationMode.REVIEW, review.mode)
        assertEquals(TripCreationMode.WIZARD, wizard.mode)
    }

    @Test
    fun `applySavedRoute replaces route, country, and address fields`() {
        val fields = TripCreationRouteFields(
            originCity = "Old",
            originCountryCode = "CA",
            pickupAddress = "Old pickup",
            destinationCity = "Cities",
            destinationCountryCode = "CA",
            dropoffAddress = "Old dropoff",
        )
        val route = SavedRouteTemplate(
            startCountryCode = "CA",
            startLocation = "Toronto",
            pickupAddress = "Union Station",
            endCountryCode = "CA",
            endLocation = "Montreal",
            dropoffAddress = "Old Port",
        )

        val updated = fields.applySavedRoute(route)

        assertEquals("Toronto", updated.originCity)
        assertEquals("Montreal", updated.destinationCity)
        assertEquals("CA", updated.originCountryCode)
        assertEquals("CA", updated.destinationCountryCode)
        assertEquals("Union Station", updated.pickupAddress)
        assertEquals("Old Port", updated.dropoffAddress)
    }
}
