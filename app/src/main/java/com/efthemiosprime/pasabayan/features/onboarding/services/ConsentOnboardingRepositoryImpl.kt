package com.efthemiosprime.pasabayan.features.onboarding.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.SupplementalApi
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.features.onboarding.model.ConsentSelections
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class ConsentOnboardingRepositoryImpl @Inject constructor(
    private val supplementalApi: SupplementalApi,
    private val json: Json,
) : ConsentOnboardingRepository {

    override suspend fun updateConsentPreferences(selections: ConsentSelections): Result<Unit> {
        return try {
            val body = ConsentPreferencesUpdateJson(
                pushNotifications = selections.pushNotifications,
                locationTracking = selections.locationTracking,
                analytics = selections.analytics,
                marketingCommunications = selections.marketingCommunications,
            )
            val res = supplementalApi.updateConsentPreferences(body)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(
                        ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json),
                    ),
                )
            }
            val responseBody = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            if (!responseBody.success) {
                return Result.failure(
                    DomainErrorMapperException(DomainError.ServerError("Consent update failed")),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
