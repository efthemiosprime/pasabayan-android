package com.efthemiosprime.pasabayan.features.payments.model

/**
 * iOS parity: `StripeConnectError` in `Pasabayan/Features/Payments/Services/StripeConnectService.swift`.
 * Mapped from server message strings inside [com.efthemiosprime.pasabayan.features.payments.services.StripeConnectRepositoryImpl].
 */
sealed class StripeConnectError(message: String) : Throwable(message) {
    /** Non-null variant of [Throwable.message]. */
    val displayMessage: String get() = message ?: ""

    object NetworkError : StripeConnectError("Network error")
    object NotCarrier : StripeConnectError("Stripe Connect is only available for carriers")
    object AlreadyOnboarded : StripeConnectError("Carrier has already completed Stripe Connect onboarding")
    object NotOnboarded : StripeConnectError("Please complete Stripe Connect onboarding first")
    object InvalidResponse : StripeConnectError("Invalid response from server")
    data class ApiError(val serverMessage: String) : StripeConnectError(serverMessage)

    companion object {
        /** Map a backend message body to a typed [StripeConnectError]. */
        fun fromServerMessage(message: String?): StripeConnectError {
            val raw = message?.lowercase()?.trim() ?: return ApiError("Stripe connect request failed")
            return when {
                raw.contains("must be a carrier") -> NotCarrier
                raw.contains("already completed") -> AlreadyOnboarded
                raw.contains("complete stripe onboarding first") -> NotOnboarded
                else -> ApiError(message)
            }
        }
    }
}
