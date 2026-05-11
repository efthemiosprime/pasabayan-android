package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import com.efthemiosprime.pasabayan.features.payments.model.StripeConnectError
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeConnectRepositoryImpl @Inject constructor(
    private val connectApi: StripeConnectApi,
    private val json: Json,
) : StripeConnectRepository {

    override suspend fun startOnboarding(): Result<String> = runCatchingNetwork {
        val res = connectApi.startOnboarding()
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw StripeConnectError.InvalidResponse
        if (!body.success) throw StripeConnectError.fromServerMessage(body.message)
        val url = body.data?.onboardingUrl?.takeIf { it.isNotBlank() }
            ?: throw StripeConnectError.InvalidResponse
        url
    }

    override suspend fun checkStatus(): Result<StripeConnectStatusJson> = runCatchingNetwork {
        val res = connectApi.getStatus()
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw StripeConnectError.InvalidResponse
        if (!body.success) throw StripeConnectError.fromServerMessage(body.message)
        body.data ?: throw StripeConnectError.InvalidResponse
    }

    override suspend fun getDashboardUrl(): Result<String> = runCatchingNetwork {
        val res = connectApi.getDashboard()
        if (!res.isSuccessful) throw mapHttpError(res.code(), res.errorBody()?.bytes())
        val body = res.body() ?: throw StripeConnectError.InvalidResponse
        if (!body.success) throw StripeConnectError.fromServerMessage(body.message)
        val url = body.data?.dashboardUrl?.takeIf { it.isNotBlank() }
            ?: throw StripeConnectError.InvalidResponse
        url
    }

    private inline fun <T> runCatchingNetwork(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: StripeConnectError) {
        Result.failure(e)
    } catch (e: DomainErrorMapperException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(StripeConnectError.NetworkError)
    }

    /**
     * For HTTP error responses, try to extract a meaningful message and route through
     * [StripeConnectError.fromServerMessage]. Falls back to the generic [DomainErrorMapperException]
     * so 401 etc. still trigger global session invalidation.
     */
    private fun mapHttpError(code: Int, errorBytes: ByteArray?): Throwable {
        val mapped = try {
            ApiErrorMapper.map(code, errorBytes, json)
        } catch (e: Exception) {
            return DomainErrorMapperException(DomainError.NetworkError(e))
        }
        val rawMessage = (mapped as? DomainError.ServerError)?.message
            ?: (mapped as? DomainError.ValidationError)?.message
        val typed = StripeConnectError.fromServerMessage(rawMessage)
        return if (typed is StripeConnectError.ApiError) {
            DomainErrorMapperException(mapped)
        } else {
            typed
        }
    }
}
