package com.efthemiosprime.pasabayan.features.legal.services

import com.efthemiosprime.pasabayan.features.legal.model.LegalStatus

interface LegalRepository {
    suspend fun fetchStatus(): Result<LegalStatus>

    /**
     * Agree to one or more pending documents. Returns the updated [LegalStatus]-style
     * `allAgreed` summary so the caller can immediately reflect post-agree state.
     */
    suspend fun agree(documentIds: List<Int>, deviceId: String? = null): Result<AgreementOutcome>

    suspend fun withdraw(documentType: String): Result<WithdrawalOutcome>
}

/** Result of `POST /legal/agree`. */
data class AgreementOutcome(
    val allRequiredAgreed: Boolean,
    val pendingRequiredCount: Int,
)

/** Result of `POST /legal/withdraw`. */
data class WithdrawalOutcome(
    val documentType: String?,
    val warning: String?,
)
