package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeConnectRepositoryImpl @Inject constructor(
    private val connectApi: StripeConnectApi,
    private val json: Json,
) : StripeConnectRepository {

    override suspend fun startOnboarding(): Result<String> {
        return try {
            val res = connectApi.startOnboarding()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val url = body.data?.onboardingUrl
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun checkStatus(): Result<StripeConnectStatusJson> {
        return try {
            val res = connectApi.getStatus()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val status = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getDashboardUrl(): Result<String> {
        return try {
            val res = connectApi.getDashboard()
            if (!res.isSuccessful) return Result.failure(
                DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
            )
            val body = res.body() ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) return Result.failure(mapBusinessError(body.message))
            val url = body.data?.dashboardUrl
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapBusinessError(message: String?): DomainErrorMapperException =
        DomainErrorMapperException(DomainError.ServerError(message ?: "Stripe connect request failed"))
}
