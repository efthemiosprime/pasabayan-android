package com.efthemiosprime.pasabayan.core.domain.error

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorAlertPolicyTest {

    @Test
    fun foreground_alwaysPresents() {
        val errors = listOf(
            DomainError.Unauthorized,
            DomainError.NetworkError(RuntimeException()),
            DomainError.NotFound,
            DomainError.ValidationError("x", emptyMap()),
        )
        errors.forEach { e ->
            assertTrue(
                ErrorAlertPolicy.shouldPresentToUser(e, ErrorAlertContext.ForegroundAction),
            )
        }
    }

    @Test
    fun background_presentsAuthPaymentConsent() {
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(DomainError.Unauthorized, ErrorAlertContext.BackgroundRefresh),
        )
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(DomainError.AuthenticationError, ErrorAlertContext.BackgroundRefresh),
        )
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(DomainError.Unauthenticated, ErrorAlertContext.BackgroundRefresh),
        )
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(DomainError.PaymentRequired("pay"), ErrorAlertContext.BackgroundRefresh),
        )
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(
                DomainError.CarrierOnboardingRequired("onboard"),
                ErrorAlertContext.BackgroundRefresh,
            ),
        )
        assertTrue(
            ErrorAlertPolicy.shouldPresentToUser(
                DomainError.ConsentRequired("terms", "accept"),
                ErrorAlertContext.BackgroundRefresh,
            ),
        )
    }

    @Test
    fun background_suppressesNetworkValidationNotFound() {
        assertFalse(
            ErrorAlertPolicy.shouldPresentToUser(
                DomainError.NetworkError(null),
                ErrorAlertContext.BackgroundRefresh,
            ),
        )
        assertFalse(
            ErrorAlertPolicy.shouldPresentToUser(
                DomainError.ValidationError("bad", mapOf("a" to listOf("b"))),
                ErrorAlertContext.BackgroundRefresh,
            ),
        )
        assertFalse(
            ErrorAlertPolicy.shouldPresentToUser(DomainError.NotFound, ErrorAlertContext.BackgroundRefresh),
        )
        assertFalse(
            ErrorAlertPolicy.shouldPresentToUser(
                DomainError.RateLimited(null),
                ErrorAlertContext.BackgroundRefresh,
            ),
        )
    }
}
