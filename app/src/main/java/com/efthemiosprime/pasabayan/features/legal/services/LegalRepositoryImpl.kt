package com.efthemiosprime.pasabayan.features.legal.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.legal.AgreementRequestJson
import com.efthemiosprime.pasabayan.core.network.legal.LegalApi
import com.efthemiosprime.pasabayan.core.network.legal.WithdrawalRequestJson
import com.efthemiosprime.pasabayan.features.legal.model.LegalMapper
import com.efthemiosprime.pasabayan.features.legal.model.LegalStatus
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalRepositoryImpl @Inject constructor(
    private val legalApi: LegalApi,
    private val json: Json,
) : LegalRepository {

    override suspend fun fetchStatus(): Result<LegalStatus> = safeCall {
        val res = legalApi.getLegalStatus()
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw invalidResponse()
        if (!body.success) throw DomainErrorMapperException(DomainError.ServerError(body.message))
        LegalMapper.toDomain(body.data)
    }

    override suspend fun agree(documentIds: List<Int>, deviceId: String?): Result<AgreementOutcome> = safeCall {
        require(documentIds.isNotEmpty()) { "agree() requires at least one document id" }
        val res = legalApi.agree(AgreementRequestJson(documentIds = documentIds, deviceId = deviceId))
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw invalidResponse()
        if (!body.success) throw DomainErrorMapperException(DomainError.ServerError(body.message))
        val data = body.data
        AgreementOutcome(
            allRequiredAgreed = data?.allRequiredAgreed ?: false,
            pendingRequiredCount = data?.pendingRequiredCount ?: 0,
        )
    }

    override suspend fun withdraw(documentType: String): Result<WithdrawalOutcome> = safeCall {
        require(documentType.isNotBlank()) { "withdraw() requires a non-blank documentType" }
        val res = legalApi.withdraw(WithdrawalRequestJson(documentType = documentType))
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw invalidResponse()
        if (!body.success) throw DomainErrorMapperException(DomainError.ServerError(body.message))
        WithdrawalOutcome(
            documentType = body.data?.documentType ?: documentType,
            warning = body.data?.warning,
        )
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: DomainErrorMapperException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
    }

    private fun mapHttpError(code: Int, bytes: ByteArray?): Throwable =
        DomainErrorMapperException(ApiErrorMapper.map(code, bytes, json))

    private fun invalidResponse(): Throwable =
        DomainErrorMapperException(DomainError.InvalidResponse)
}
