package com.efthemiosprime.pasabayan.ui.screens.carrier.models

/**
 * Trip Creation Events - Sealed class for event-driven architecture
 * Following functional programming patterns with immutable events
 */
sealed class TripCreationEvent {
    data class ShowError(val message: String) : TripCreationEvent()
    data class ShowSuccess(val message: String) : TripCreationEvent()
    object NavigateBack : TripCreationEvent()
    object ClearForm : TripCreationEvent()
} 