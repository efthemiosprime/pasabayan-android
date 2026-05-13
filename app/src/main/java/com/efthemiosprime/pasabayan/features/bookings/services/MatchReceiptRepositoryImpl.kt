package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.bookings.MatchReceiptApi
import com.efthemiosprime.pasabayan.features.bookings.model.MatchReceipt
import com.efthemiosprime.pasabayan.features.profile.services.ImageCompressor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchReceiptRepositoryImpl @Inject constructor(
    private val api: MatchReceiptApi,
    private val imageCompressor: ImageCompressor,
    private val json: Json,
) : MatchReceiptRepository {

    override suspend fun uploadReceipt(matchId: Int, photoBytes: ByteArray): Result<MatchReceipt> {
        return try {
            val compressed = imageCompressor.compressToJpeg(
                input = photoBytes,
                maxDimension = RECEIPT_MAX_DIMENSION,
                jpegQuality = RECEIPT_JPEG_QUALITY,
            )
            val part = MultipartBody.Part.createFormData(
                name = "receipt_photo",
                filename = "receipt.jpg",
                body = compressed.toRequestBody("image/jpeg".toMediaTypeOrNull()),
            )
            val res = api.uploadReceipt(matchId = matchId, receiptPhoto = part)
            if (!res.isSuccessful) return Result.failure(mapError(res.code(), res.errorBody()?.bytes()))
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(
                MatchReceipt(
                    receiptPhoto = data.receiptPhoto,
                    receiptUrl = data.receiptUrl,
                    uploadedAt = null,
                ),
            )
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchReceipt(matchId: Int): Result<MatchReceipt?> {
        return try {
            val res = api.getReceipt(matchId)
            // iOS treats 404 as "no receipt yet" — surface null, not a failure.
            if (res.code() == 404) return Result.success(null)
            if (!res.isSuccessful) return Result.failure(mapError(res.code(), res.errorBody()?.bytes()))
            val data = res.body()?.data ?: return Result.success(null)
            Result.success(
                MatchReceipt(
                    receiptPhoto = data.receiptPhoto,
                    receiptUrl = data.receiptUrl,
                    uploadedAt = data.uploadedAt,
                ),
            )
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapError(code: Int, body: ByteArray?): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(code, body, json))

    private companion object {
        const val RECEIPT_MAX_DIMENSION = 1024
        const val RECEIPT_JPEG_QUALITY = 80
    }
}
