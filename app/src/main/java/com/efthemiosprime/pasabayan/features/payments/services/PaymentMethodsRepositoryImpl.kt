package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodsApi
import com.efthemiosprime.pasabayan.core.network.payments.SetDefaultPaymentMethodRequestJson
import com.efthemiosprime.pasabayan.core.network.payments.SetupIntentDataJson
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.model.toDomain
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodsRepositoryImpl @Inject constructor(
    private val methodsApi: PaymentMethodsApi,
    private val json: Json,
) : PaymentMethodsRepository {

    override suspend fun loadPaymentMethods(): Result<List<PaymentMethodDisplay>> {
        return try {
            val res = methodsApi.getPaymentMethods()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(null))
            val methods = body.data.map { it.toDomain() }
            Result.success(methods)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadDefaultPaymentMethod(): Result<String?> {
        return try {
            val res = methodsApi.getDefaultPaymentMethod()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(null))
            Result.success(body.data?.paymentMethodId)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createSetupIntent(): Result<SetupIntentDataJson> {
        return try {
            val res = methodsApi.createSetupIntent()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(null))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun removePaymentMethod(methodId: String): Result<Unit> {
        return try {
            val res = methodsApi.deletePaymentMethod(methodId)
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun setDefaultPaymentMethod(methodId: String): Result<Unit> {
        return try {
            val res = methodsApi.setDefaultPaymentMethod(SetDefaultPaymentMethodRequestJson(methodId))
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapBusinessError(message: String?): DomainErrorMapperException =
        DomainErrorMapperException(DomainError.ServerError(message ?: "Payment method request failed"))
}
