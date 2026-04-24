package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate

enum class TripCreationMode {
    WIZARD,
    REVIEW,
}

data class TripCreationFlowState(
    val currentStep: Int = 0,
    val mode: TripCreationMode = TripCreationMode.WIZARD,
) {
    fun previousStep(): TripCreationFlowState {
        if (currentStep == 0) return this
        return copy(currentStep = currentStep - 1)
    }

    fun nextStep(lastWizardStep: Int): TripCreationFlowState {
        if (currentStep >= lastWizardStep) return this
        return copy(currentStep = currentStep + 1)
    }

    fun switchToReview(): TripCreationFlowState = copy(mode = TripCreationMode.REVIEW)

    fun switchToWizard(): TripCreationFlowState = copy(mode = TripCreationMode.WIZARD)
}

data class TripCreationRouteFields(
    val originCity: String = "",
    val originCountryCode: String = "",
    val pickupAddress: String = "",
    val destinationCity: String = "",
    val destinationCountryCode: String = "",
    val dropoffAddress: String = "",
) {
    fun applySavedRoute(route: SavedRouteTemplate): TripCreationRouteFields = copy(
        originCity = route.startLocation,
        originCountryCode = route.startCountryCode,
        pickupAddress = route.pickupAddress.orEmpty(),
        destinationCity = route.endLocation,
        destinationCountryCode = route.endCountryCode,
        dropoffAddress = route.dropoffAddress.orEmpty(),
    )
}
