package com.efthemiosprime.pasabayan.ui.screens.packagerequest.models

/**
 * Package Request Events - Sealed class for event-driven architecture
 * Following functional programming patterns with immutable events
 */
sealed class PackageRequestEvent {
    data class ShowError(val message: String) : PackageRequestEvent()
    data class ShowSuccess(val message: String) : PackageRequestEvent()
    object NavigateBack : PackageRequestEvent()
    object ClearForm : PackageRequestEvent()
} 