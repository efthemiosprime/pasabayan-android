package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.auth.AuthApi
import com.efthemiosprime.pasabayan.core.network.auth.ProviderLoginRequestJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val json: Json,
    private val tokenStore: TokenStore,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)

    override fun currentUser(): StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override fun clearCurrentUser() {
        _currentUser.value = null
    }

    override suspend fun loginWithProviderAccessToken(provider: String, accessToken: String): Result<AuthUser> {
        return try {
            val body = when (provider) {
                "google" -> ProviderLoginRequestJson(idToken = accessToken)
                else -> ProviderLoginRequestJson(accessToken = accessToken)
            }
            val res = authApi.loginWithProvider(provider, body)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val responseBody = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!responseBody.success || responseBody.data == null) {
                val msg = responseBody.message.ifBlank { "Login failed" }
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val authData = requireNotNull(responseBody.data)
            tokenStore.setToken(authData.token)
            val user = authData.user.toAuthUser()
            _currentUser.value = user
            Result.success(user)
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
            val user = body.data.user.toAuthUser()
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val res = authApi.logout()
            tokenStore.clear()
            _currentUser.value = null
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            tokenStore.clear()
            _currentUser.value = null
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
