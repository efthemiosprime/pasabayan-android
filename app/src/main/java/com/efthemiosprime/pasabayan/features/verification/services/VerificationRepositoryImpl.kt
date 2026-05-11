package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationSubmissionDataJson
import com.efthemiosprime.pasabayan.core.network.verification.ResendOtpRequestJson
import com.efthemiosprime.pasabayan.core.network.verification.SendOtpRequestJson
import com.efthemiosprime.pasabayan.core.network.verification.VerificationApi
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpRequestJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    override suspend fun submitPremiumVerification(
        idType: String,
        idDocumentFront: ByteArray,
        idDocumentBack: ByteArray?,
        selfieWithId: ByteArray,
        idNumber: String?,
        birthDate: String?,
        mimeType: String,
    ): Result<PremiumVerificationSubmissionDataJson?> = wrap {
        val frontPart = MultipartBody.Part.createFormData(
            name = "id_document",
            filename = "id_front.jpg",
            body = idDocumentFront.toRequestBody(mimeType.toMediaTypeOrNull()),
        )
        val backPart = idDocumentBack?.let {
            MultipartBody.Part.createFormData(
                name = "id_document_back",
                filename = "id_back.jpg",
                body = it.toRequestBody(mimeType.toMediaTypeOrNull()),
            )
        }
        val selfiePart = MultipartBody.Part.createFormData(
            name = "selfie_with_id",
            filename = "selfie.jpg",
            body = selfieWithId.toRequestBody(mimeType.toMediaTypeOrNull()),
        )
        val res = api.requestPremiumVerification(
            idDocument = frontPart,
            idDocumentBack = backPart,
            selfieWithId = selfiePart,
            idType = idType.toTextPart(),
            idNumber = idNumber?.takeIf { it.isNotBlank() }?.toTextPart(),
            birthDate = birthDate?.takeIf { it.isNotBlank() }?.toTextPart(),
        )
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        if (!body.success) return@wrap Result.failure(serverError(body.message))
        Result.success(body.data)
    }

    override suspend fun fetchPremiumStatus(): Result<PremiumVerificationStatusDataJson> = wrap {
        val res = api.premiumStatus()
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data ?: PremiumVerificationStatusDataJson())
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

    private fun String.toTextPart(): RequestBody =
        toRequestBody("text/plain".toMediaTypeOrNull())
}
