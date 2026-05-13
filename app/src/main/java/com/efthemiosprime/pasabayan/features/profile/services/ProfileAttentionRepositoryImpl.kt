package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.AttentionSignalsJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class ProfileAttentionRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val json: Json,
) : ProfileAttentionRepository {

    override suspend fun fetchAttention(): Result<AttentionSignalsJson> {
        return try {
            val response = profileApi.getAttention()
            if (!response.isSuccessful) {
                val err = ApiErrorMapper.map(response.code(), response.errorBody()?.bytes(), json)
                return Result.failure(DomainErrorMapperException(err))
            }
            val body = response.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(body)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
