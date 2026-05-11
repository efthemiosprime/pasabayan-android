package com.efthemiosprime.pasabayan.features.locations.services

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import com.efthemiosprime.pasabayan.core.network.SupplementalApi
import com.efthemiosprime.pasabayan.core.network.location.CityOptionJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.features.locations.model.CandidateCity
import com.efthemiosprime.pasabayan.features.locations.model.HomeCityMatch
import com.efthemiosprime.pasabayan.features.locations.model.HomeCityMatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Outcomes the orchestrator can return. Matches the iOS flow:
 *  - [Skipped] — auth missing / permission denied / GPS not returned / already run this
 *    session.
 *  - [NoChange] — GPS resolved to a city that already matches the profile.
 *  - [Updated] — local profile + server were updated with the new home city.
 */
sealed interface HomeCityDetectionOutcome {
    data class Skipped(val reason: SkipReason) : HomeCityDetectionOutcome
    data class NoChange(val cityId: Int, val distanceKm: Double) : HomeCityDetectionOutcome
    data class Updated(val cityId: Int, val cityName: String, val distanceKm: Double) : HomeCityDetectionOutcome

    enum class SkipReason {
        NOT_AUTHENTICATED,
        NO_LOCATION,
        EMPTY_CATALOG,
        NO_MATCH_IN_RADIUS,
        ALREADY_DETECTED_THIS_SESSION,
    }
}

/**
 * Coordinates the GPS + city-catalog + profile fan-out described in spec 12 § "Home city
 * detection flow". Single-shot per process unless [detectIfNeeded] is called with
 * `forceRefresh = true`. Mirrors iOS `HomeCityDetectionService`.
 */
interface HomeCityDetectionService {
    suspend fun detectIfNeeded(
        countryCode: String = DEFAULT_COUNTRY,
        forceRefresh: Boolean = false,
    ): HomeCityDetectionOutcome

    companion object {
        const val DEFAULT_COUNTRY: String = "CA"
    }
}

@Singleton
class DefaultHomeCityDetectionService @Inject constructor(
    private val locationProvider: LocationProvider,
    private val supplementalApi: SupplementalApi,
    private val profileApi: ProfileApi,
    private val authTokenProvider: AuthTokenProvider,
    private val confirmedStore: ConfirmedHomeCityStore,
) : HomeCityDetectionService {

    @Volatile
    private var hasRunThisSession: Boolean = false

    override suspend fun detectIfNeeded(
        countryCode: String,
        forceRefresh: Boolean,
    ): HomeCityDetectionOutcome {
        if (!forceRefresh && hasRunThisSession) {
            return HomeCityDetectionOutcome.Skipped(
                HomeCityDetectionOutcome.SkipReason.ALREADY_DETECTED_THIS_SESSION,
            )
        }
        if (authTokenProvider.currentToken().isNullOrBlank()) {
            return HomeCityDetectionOutcome.Skipped(
                HomeCityDetectionOutcome.SkipReason.NOT_AUTHENTICATED,
            )
        }

        val outcome = coroutineScope {
            val locationDeferred = async { locationProvider.currentLocation() }
            val citiesDeferred = async {
                runCatching { supplementalApi.getCitiesForCountry(countryCode) }.getOrNull()
            }
            val profileDeferred = async { runCatching { profileApi.getProfile() }.getOrNull() }

            val gps = locationDeferred.await()
                ?: return@coroutineScope HomeCityDetectionOutcome.Skipped(
                    HomeCityDetectionOutcome.SkipReason.NO_LOCATION,
                )

            val cities = citiesDeferred.await()
                ?.takeIf { it.isSuccessful }
                ?.body()?.data.orEmpty()
                .mapNotNull { it.toCandidateCity() }
            if (cities.isEmpty()) {
                return@coroutineScope HomeCityDetectionOutcome.Skipped(
                    HomeCityDetectionOutcome.SkipReason.EMPTY_CATALOG,
                )
            }

            val profileBody = profileDeferred.await()?.takeIf { it.isSuccessful }?.body()
            val currentHomeCityId = profileBody?.data?.homeCityId
            val currentHomeCityName = currentHomeCityId
                ?.let { id -> cities.firstOrNull { it.id == id }?.name }

            val match: HomeCityMatch = HomeCityMatcher.match(
                gps = gps,
                candidates = cities,
                profileCityName = currentHomeCityName,
            ) ?: return@coroutineScope HomeCityDetectionOutcome.Skipped(
                HomeCityDetectionOutcome.SkipReason.NO_MATCH_IN_RADIUS,
            )

            if (currentHomeCityId == match.city.id && confirmedStore.get() == match.city.id) {
                return@coroutineScope HomeCityDetectionOutcome.NoChange(match.city.id, match.distanceKm)
            }

            val update = runCatching {
                profileApi.putProfile(UpdateProfileRequestJson(homeCityId = match.city.id))
            }.getOrNull()
            if (update != null && update.isSuccessful) {
                confirmedStore.set(match.city.id)
                HomeCityDetectionOutcome.Updated(match.city.id, match.city.name, match.distanceKm)
            } else {
                // Server update failed — don't mark confirmed; let next session retry.
                HomeCityDetectionOutcome.NoChange(match.city.id, match.distanceKm)
            }
        }

        hasRunThisSession = true
        return outcome
    }

    private fun CityOptionJson.toCandidateCity(): CandidateCity? {
        val lat = latString().toDoubleOrNull() ?: return null
        val lng = lngString().toDoubleOrNull() ?: return null
        return CandidateCity(id = id, name = name, latitude = lat, longitude = lng)
    }
}
