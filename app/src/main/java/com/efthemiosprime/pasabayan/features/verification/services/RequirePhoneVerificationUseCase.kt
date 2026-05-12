package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.session.AuthRepository
import javax.inject.Inject

/**
 * Gate for actions that require a phone-verified user (create package, create trip,
 * book trip, request to carry). Reads the live session via [AuthRepository.currentUser]
 * so feature view-models can guard at function entry without holding the user themselves.
 */
class RequirePhoneVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Result<Unit> {
        val user = authRepository.currentUser().value
        return if (user?.phoneVerified == true) {
            Result.success(Unit)
        } else {
            Result.failure(PhoneVerificationRequired)
        }
    }

    object PhoneVerificationRequired : Exception("phone_verification_required")
}
