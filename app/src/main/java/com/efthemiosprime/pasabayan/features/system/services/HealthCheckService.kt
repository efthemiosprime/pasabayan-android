package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.network.system.SystemApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps `GET /health`. Returns `true` when the backend responded with HTTP 2xx and
 * `success = true` (or `status` reported `"ok"`).
 */
interface HealthCheckService {
    suspend fun isReachable(): Result<Boolean>
}

@Singleton
class DefaultHealthCheckService @Inject constructor(
    private val systemApi: SystemApi,
) : HealthCheckService {

    override suspend fun isReachable(): Result<Boolean> = try {
        val response = systemApi.healthCheck()
        if (!response.isSuccessful) {
            Result.success(false)
        } else {
            val body = response.body()
            val ok = body?.success == true || body?.status.equals("ok", ignoreCase = true)
            Result.success(ok)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
