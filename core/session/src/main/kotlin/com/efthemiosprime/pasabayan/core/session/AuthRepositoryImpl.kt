package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.auth.AuthApi
import com.efthemiosprime.pasabayan.core.network.auth.ProviderLoginRequestJson
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val json: Json,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun loginWithProviderAccessToken(provider: String, accessToken: String): Result<AuthUser> {
        return try {
            val res = authApi.loginWithProvider(provider, ProviderLoginRequestJson(accessToken))
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success || body.data == null) {
                val msg = body.message.ifBlank { "Login failed" }
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val authData = requireNotNull(body.data)
            tokenStore.setToken(authData.token)
            Result.success(authData.user.toAuthUser())
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadCurrentUser(): Result<AuthUser> {
        return try {
            val res = authApi.getMe()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                return Result.failure(DomainErrorMapperException(DomainError.ServerError("Invalid session")))
            }
            Result.success(body.data.user.toAuthUser())
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val res = authApi.logout()
            tokenStore.clear()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            tokenStore.clear()
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
