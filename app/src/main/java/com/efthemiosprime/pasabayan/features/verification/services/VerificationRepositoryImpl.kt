package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.ResendOtpRequestJson
import com.efthemiosprime.pasabayan.core.network.verification.SendOtpRequestJson
import com.efthemiosprime.pasabayan.core.network.verification.VerificationApi
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpRequestJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.Response

@Singleton
class VerificationRepositoryImpl @Inject constructor(
    private val api: VerificationApi,
    private val json: Json,
) : VerificationRepository {

    override suspend fun sendOtp(phone: String): Result<OtpDataJson?> = wrap {
        val res = api.sendOtp(SendOtpRequestJson(phone = phone))
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        if (!body.success) return@wrap Result.failure(serverError(body.message))
        Result.success(body.data)
    }

    override suspend fun verifyOtp(phone: String, otpCode: String): Result<VerifyOtpDataJson?> = wrap {
        val res = api.verifyOtp(VerifyOtpRequestJson(phone = phone, otpCode = otpCode))
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        if (!body.success) return@wrap Result.failure(serverError(body.message))
        Result.success(body.data)
    }

    override suspend fun resendOtp(phone: String): Result<OtpDataJson?> = wrap {
        val res = api.resendOtp(ResendOtpRequestJson(phone = phone))
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        if (!body.success) return@wrap Result.failure(serverError(body.message))
        Result.success(body.data)
    }

    override suspend fun fetchPhoneStatus(): Result<PhoneStatusDataJson> = wrap {
        val res = api.phoneStatus()
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data ?: PhoneStatusDataJson())
    }

    private inline fun <T> wrap(block: () -> Result<T>): Result<T> =
        try {
            block()
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))

    private fun invalid(): DomainErrorMapperException =
        DomainErrorMapperException(DomainError.InvalidResponse)

    private fun serverError(message: String?): DomainErrorMapperException =
        DomainErrorMapperException(
            DomainError.ServerError(message?.takeIf { it.isNotBlank() } ?: "Verification failed"),
        )
}
