package com.efthemiosprime.pasabayan.features.onboarding.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.SupplementalApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.features.onboarding.model.CityPickerOption
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class CityOnboardingRepositoryImpl @Inject constructor(
    private val supplementalApi: SupplementalApi,
    private val profileApi: ProfileApi,
    private val json: Json,
) : CityOnboardingRepository {

    override suspend fun fetchCitiesCanada(): Result<List<CityPickerOption>> {
        return try {
            val res = supplementalApi.getCitiesForCountry("CA")
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(
                        ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json),
                    ),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                return Result.failure(DomainErrorMapperException(DomainError.ServerError("Cities request failed")))
            }
            val sorted = body.data
                .map { CityPickerOption(id = it.id, display = it.display) }
                .sortedBy { it.display.lowercase() }
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updateHomeCity(cityId: Int): Result<Unit> {
        return try {
            val res = profileApi.putProfile(UpdateProfileRequestJson(homeCityId = cityId))
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(
                        ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json),
                    ),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!body.success) {
                val msg = body.message?.takeIf { it.isNotBlank() } ?: "Profile update failed"
                return Result.failure(DomainErrorMapperException(DomainError.ServerError(msg)))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
