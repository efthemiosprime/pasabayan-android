package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatusDataJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileDataJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import retrofit2.Response

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val json: Json,
) : ProfileRepository {

    private val profileCacheLock = Mutex()
    private var cachedProfile: ProfileDataJson? = null

    override suspend fun fetchProfile(forceRefresh: Boolean): Result<ProfileDataJson> {
        try {
            if (forceRefresh) {
                profileCacheLock.withLock { cachedProfile = null }
            } else {
                val snap = profileCacheLock.withLock { cachedProfile }
                if (snap != null) {
                    return Result.success(snap)
                }
            }
            val res = profileApi.getProfile()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Profile request failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            val data = body.data
            profileCacheLock.withLock { cachedProfile = data }
            return Result.success(data)
        } catch (e: Exception) {
            return Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchCarrierProfile(): Result<CarrierProfileJson?> {
        return try {
            val res = profileApi.getCarrierProfile()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchCarrierStats(): Result<CarrierStatsJson?> {
        return try {
            val res = profileApi.getCarrierStats()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (body.success == false) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Stats failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun fetchUserStats(): Result<UserStatsDataJson?> {
        return try {
            val res = profileApi.getUserStats()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (body.success == false) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "User stats failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            Result.success(body.data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun toggleCarrierStatus(): Result<CarrierStatusDataJson> {
        return try {
            val res = profileApi.postCarrierToggleStatus()
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val data = body.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createCarrierProfile(
        body: CreateCarrierProfileRequestJson,
    ): Result<CarrierProfileJson?> {
        return try {
            val res = profileApi.postCarrierProfile(body)
            when {
                res.isSuccessful -> {
                    val data = res.body()?.data
                    Result.success(data)
                }
                res.code() == 409 -> fetchCarrierProfile()
                else -> Result.failure(mapError(res))
            }
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))
}
