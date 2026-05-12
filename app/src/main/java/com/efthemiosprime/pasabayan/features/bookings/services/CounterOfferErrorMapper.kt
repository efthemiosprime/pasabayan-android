package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.domain.error.userMessage

/**
 * Maps a [DomainError] from the counter-offer endpoints to copy suitable
 * for the body of an error alert. Mirrors iOS `CounterOfferErrorMapper`
 * from Features/Bookings/CounterOfferErrorMapper.swift.
 *
 * The returned string is intended for an alert whose title already conveys
 * the failure context — it avoids redundant prefixes like
 * "Failed to send counter-offer: …".
 */
object CounterOfferErrorMapper {

    fun userMessage(error: DomainError, messages: CounterOfferErrorMessages): String = when (error) {
        is DomainError.Conflict -> {
            val backendMessage = error.message
            if (backendMessage != null && isLimitReachedMessage(backendMessage)) {
                messages.limitReached
            } else {
                backendMessage ?: error.userMessage()
            }
        }

        is DomainError.ServerError -> error.message ?: error.userMessage()
        is DomainError.PaymentRequired -> error.message ?: error.userMessage()
        is DomainError.CarrierOnboardingRequired -> error.message ?: error.userMessage()
        is DomainError.TripNotFound -> error.message ?: error.userMessage()

        // 400/422 with field-level errors. The DomainError formatter combines the top-level
        // message with a humanized "Field: error1, error2" block per iOS `ValidationErrorResponse.formattedMessage`.
        is DomainError.ValidationError -> error.userMessage()

        // 409 from the consent middleware (rare on counter-offer but possible if a
        // role-switch consent expires mid-session).
        is DomainError.ConsentRequired -> error.message?.takeIf { it.isNotBlank() } ?: error.userMessage()

        is DomainError.Unauthorized,
        is DomainError.AuthenticationError,
        is DomainError.Unauthenticated -> messages.signInAgain

        is DomainError.NetworkError -> messages.networkError
        is DomainError.NotFound -> messages.notFound
        is DomainError.RateLimited -> messages.rateLimited

        // iOS also has a dedicated `phoneVerificationRequired` case. Android enforces
        // phone verification client-side via `RequirePhoneVerificationUseCase`, so a
        // server-returned phone-verification error currently arrives here as a generic
        // `ServerError` or `PaymentRequired` and is surfaced via its message.
        else -> error.userMessage()
    }

    /**
     * Narrow on purpose — broader patterns ("limit", "maximum") risked
     * over-mapping unrelated 409 conflicts to the limit-reached copy.
     */
    private fun isLimitReachedMessage(message: String): Boolean =
        message.contains("counter-offer limit", ignoreCase = true)
}
