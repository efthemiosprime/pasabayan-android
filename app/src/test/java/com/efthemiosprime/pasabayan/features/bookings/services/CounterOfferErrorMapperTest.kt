package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterOfferErrorMapperTest {

    private val messages = CounterOfferErrorMessages(
        limitReached = "LIMIT_REACHED",
        signInAgain = "SIGN_IN_AGAIN",
        networkError = "NETWORK_ERROR",
        notFound = "NOT_FOUND",
        rateLimited = "RATE_LIMITED",
    )

    // -- Conflict --

    @Test
    fun `conflict with counter-offer limit message returns limit reached copy`() {
        val error = DomainError.Conflict(
            message = "Counter-offer limit reached for this match.",
            expiresAt = null,
        )
        assertEquals("LIMIT_REACHED", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `conflict with non-limit message returns backend message verbatim`() {
        val error = DomainError.Conflict(message = "Duplicate match request detected", expiresAt = null)
        assertEquals("Duplicate match request detected", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `conflict limit detection is case-insensitive`() {
        val error = DomainError.Conflict(message = "COUNTER-OFFER LIMIT exceeded", expiresAt = null)
        assertEquals("LIMIT_REACHED", CounterOfferErrorMapper.userMessage(error, messages))
    }

    // -- Pass-through messages --

    @Test
    fun `serverError returns backend message`() {
        val error = DomainError.ServerError(message = "Internal server error")
        assertEquals("Internal server error", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `paymentRequired returns backend message`() {
        val error = DomainError.PaymentRequired(message = "Payment method required")
        assertEquals("Payment method required", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `carrierOnboardingRequired returns backend message`() {
        val error = DomainError.CarrierOnboardingRequired(message = "Complete carrier onboarding first")
        assertEquals("Complete carrier onboarding first", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `tripNotFound returns backend message`() {
        val error = DomainError.TripNotFound(message = "This trip has been cancelled", tripId = "42")
        assertEquals("This trip has been cancelled", CounterOfferErrorMapper.userMessage(error, messages))
    }

    // -- Auth family --

    @Test
    fun `unauthorized returns sign-in copy`() {
        assertEquals("SIGN_IN_AGAIN", CounterOfferErrorMapper.userMessage(DomainError.Unauthorized, messages))
    }

    @Test
    fun `authenticationError returns sign-in copy`() {
        assertEquals("SIGN_IN_AGAIN", CounterOfferErrorMapper.userMessage(DomainError.AuthenticationError, messages))
    }

    @Test
    fun `unauthenticated returns sign-in copy`() {
        assertEquals("SIGN_IN_AGAIN", CounterOfferErrorMapper.userMessage(DomainError.Unauthenticated, messages))
    }

    // -- Network / transport --

    @Test
    fun `networkError returns localized network copy`() {
        val error = DomainError.NetworkError(cause = null)
        assertEquals("NETWORK_ERROR", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `notFound returns localized not-found copy`() {
        assertEquals("NOT_FOUND", CounterOfferErrorMapper.userMessage(DomainError.NotFound, messages))
    }

    @Test
    fun `rateLimited returns localized rate-limited copy`() {
        val error = DomainError.RateLimited(retryAfterSeconds = 60)
        assertEquals("RATE_LIMITED", CounterOfferErrorMapper.userMessage(error, messages))
    }

    // -- Validation (now an explicit branch) --

    @Test
    fun `validationError formats field errors via DomainError`() {
        val error = DomainError.ValidationError(
            message = "Validation failed",
            fieldErrors = mapOf("proposed_price" to listOf("Must be a number")),
        )
        val result = CounterOfferErrorMapper.userMessage(error, messages)
        assertTrue(result.contains("Validation failed"))
        assertTrue(result.contains("Proposed Price"))
    }

    @Test
    fun `validationError with empty fieldErrors returns top-level message`() {
        val error = DomainError.ValidationError(
            message = "The given data was invalid.",
            fieldErrors = emptyMap(),
        )
        assertEquals("The given data was invalid.", CounterOfferErrorMapper.userMessage(error, messages))
    }

    // -- ConsentRequired --

    @Test
    fun `consentRequired with backend message returns it verbatim`() {
        val error = DomainError.ConsentRequired(
            purpose = "carrier_profile",
            message = "Carrier consent needed to proceed",
        )
        assertEquals("Carrier consent needed to proceed", CounterOfferErrorMapper.userMessage(error, messages))
    }

    @Test
    fun `consentRequired with blank message falls back to DomainError userMessage`() {
        val error = DomainError.ConsentRequired(purpose = "carrier_profile", message = "   ")
        // DomainError fallback returns "Additional consent is required to continue".
        val result = CounterOfferErrorMapper.userMessage(error, messages)
        assertEquals("Additional consent is required to continue", result)
    }

    // -- Unknown branch --

    @Test
    fun `unknown error falls back to DomainError userMessage`() {
        val result = CounterOfferErrorMapper.userMessage(DomainError.Unknown, messages)
        assertEquals("An unknown error occurred", result)
    }
}
