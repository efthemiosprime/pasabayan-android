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
    fun `applySavedRoute replaces origin and destination cities`() {
        val fields = TripCreationRouteFields(originCity = "Old", destinationCity = "Cities")
        val route = SavedRouteTemplate(
            startCountryCode = "CA",
            startLocation = "Toronto",
            endCountryCode = "CA",
            endLocation = "Montreal",
        )

        val updated = fields.applySavedRoute(route)

        assertEquals("Toronto", updated.originCity)
        assertEquals("Montreal", updated.destinationCity)
    }
}
