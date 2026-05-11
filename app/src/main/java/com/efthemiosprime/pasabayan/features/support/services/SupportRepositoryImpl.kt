package com.efthemiosprime.pasabayan.features.support.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.support.SupportApi
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportMapper
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import com.efthemiosprime.pasabayan.features.support.model.SupportTicket
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepositoryImpl @Inject constructor(
    private val supportApi: SupportApi,
    private val attachmentReader: AttachmentReader,
    private val json: Json,
) : SupportRepository {

    override suspend fun submit(
        category: SupportCategory,
        subject: String,
        email: String,
        priority: SupportPriority,
        description: String,
        attachments: List<Uri>,
    ): Result<SupportTicket> = runCatchingNetwork {
        require(subject.isNotBlank()) { "Subject must not be blank" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("category", category.rawValue)
            .addFormDataPart("subject", subject)
            .addFormDataPart("email", email)
            .addFormDataPart("priority", priority.rawValue)
            .addFormDataPart("description", description)

        attachments.forEach { uri ->
            val part = attachmentReader.readAsPart(ATTACHMENTS_FIELD, uri)
                ?: throw DomainErrorMapperException(
                    DomainError.ServerError("Could not read attachment $uri"),
                )
            builder.addPart(part)
        }

        val response = supportApi.submitTicket(builder.build())
        if (!response.isSuccessful) {
            throw DomainErrorMapperException(
                ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json),
            )
        }
        val body = response.body()
            ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        if (!body.success) {
            throw DomainErrorMapperException(
                DomainError.ServerError(body.message ?: "Support ticket submission failed"),
            )
        }
        val ticketDto = body.data
            ?: throw DomainErrorMapperException(DomainError.InvalidResponse)
        SupportMapper.toDomain(ticketDto)
    }

    private inline fun <T> runCatchingNetwork(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: IllegalArgumentException) {
        Result.failure(DomainErrorMapperException(DomainError.ServerError(e.message)))
    } catch (e: DomainErrorMapperException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
    }

    @Suppress("unused")
    private fun emptyText() = "".toRequestBody("text/plain".toMediaTypeOrNull())

    private companion object {
        /** Laravel-style array field. iOS sends the same key per part. */
        const val ATTACHMENTS_FIELD = "attachments[]"
    }
}
