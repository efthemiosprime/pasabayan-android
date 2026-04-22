package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsLocalStateCoordinator @Inject constructor(
    private val usualTransportStore: UsualTransportStore,
    private val savedRouteTemplatesStore: SavedRouteTemplatesStore,
    private val carrierDisclaimerStore: CarrierDisclaimerStore,
) : TripsLocalStateUpdater {
    override fun onTripCreationSucceeded(userId: Long, request: CreateTripFromPackageRequest) {
        val method = transportationMethodFromRaw(request.transportationMethod)
        if (method != null && method != TransportationMethod.NONE) {
            usualTransportStore.set(userId = userId.toInt(), method = method)
        }
        savedRouteTemplatesStore.save(
            SavedRouteTemplate(
                startCountryCode = request.originCountry,
                startLocation = request.originCity,
                pickupAddress = request.pickupAddress,
                endCountryCode = request.destinationCountry,
                endLocation = request.destinationCity,
                dropoffAddress = request.dropoffAddress,
            ),
        )
    }

    override fun onCarrierDisclaimerAcknowledgeAttempt(userId: Long, syncSucceeded: Boolean) {
        val id = userId.toInt()
        carrierDisclaimerStore.setAcknowledged(id)
        carrierDisclaimerStore.setPendingSync(id, pending = !syncSucceeded)
    }

    override suspend fun retryPendingCarrierDisclaimerSync(
        userId: Long,
        syncAction: suspend () -> Result<Unit>,
    ): Boolean {
        val id = userId.toInt()
        if (!carrierDisclaimerStore.isPendingSync(id)) return false
        val synced = syncAction().isSuccess
        if (synced) {
            carrierDisclaimerStore.setPendingSync(id, pending = false)
        }
        return synced
    }

    private fun transportationMethodFromRaw(raw: String?): TransportationMethod? {
        if (raw.isNullOrBlank()) return null
        return TransportationMethod.entries.firstOrNull { method ->
            method.name.equals(raw, ignoreCase = true)
        }
    }
}
