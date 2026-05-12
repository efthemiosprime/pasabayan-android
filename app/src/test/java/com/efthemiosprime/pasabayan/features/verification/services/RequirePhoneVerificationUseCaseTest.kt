package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RequirePhoneVerificationUseCaseTest {

    private val authRepository: AuthRepository = mockk()

    private fun useCase() = RequirePhoneVerificationUseCase(authRepository)

    private fun givenCurrentUser(user: AuthUser?) {
        every { authRepository.currentUser() } returns MutableStateFlow(user)
    }

    @Test
    fun signedOut_returnsPhoneVerificationRequired() {
        givenCurrentUser(null)

        val result = useCase().invoke()

        assertTrue(result.isFailure)
        assertSame(
            RequirePhoneVerificationUseCase.PhoneVerificationRequired,
            result.exceptionOrNull(),
        )
    }

    @Test
    fun phoneNotVerified_returnsPhoneVerificationRequired() {
        givenCurrentUser(testUser(phoneVerified = false))

        val result = useCase().invoke()

        assertTrue(result.isFailure)
        assertSame(
            RequirePhoneVerificationUseCase.PhoneVerificationRequired,
            result.exceptionOrNull(),
        )
    }

    @Test
    fun phoneVerified_returnsSuccess() {
        givenCurrentUser(testUser(phoneVerified = true))

        val result = useCase().invoke()

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    private fun testUser(phoneVerified: Boolean): AuthUser = AuthUser(
        id = 1L,
        name = "Test",
        email = "test@example.com",
        avatar = null,
        phone = if (phoneVerified) "+15551112222" else null,
        phoneVerified = phoneVerified,
        profileCompleted = true,
        provider = "google",
        userTypes = listOf("shipper"),
        isActiveCarrier = false,
        isActiveShipper = true,
    )
}
