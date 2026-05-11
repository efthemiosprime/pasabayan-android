package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.features.packages.services.ShipperDisclaimerStore
import com.efthemiosprime.pasabayan.features.trips.services.CarrierDisclaimerStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the local disclaimer stores (`CarrierDisclaimerStore`, `ShipperDisclaimerStore`) to the
 * `/api/profile/disclaimer-acknowledgments` endpoints (`09-profile-carrier-consent` § Disclaimer
 * acknowledgments). The local store remains the UI's source of truth so disclaimer sheets keep
 * working offline; this service:
 *
 * 1. Bootstraps the local flags from the server on app launch (cross-device parity).
 * 2. Fires the POST when the user acknowledges in-app, and stamps a `pendingSync` flag if the
 *    network call fails so the next launch can retry.
 * 3. Retries the pending syncs.
 *
 * Disclaimer raw types come from iOS — `"carrier_trip"` and `"shipper"`.
 */
@Singleton
class DisclaimerSyncService @Inject constructor(
    private val repository: ProfileRepository,
    private val carrierStore: CarrierDisclaimerStore,
    private val shipperStore: ShipperDisclaimerStore,
) {

    /**
     * Pulls server-side acknowledgments. When the server says a disclaimer is acknowledged but the
     * local store hasn't recorded it yet, we flip the local flag so a freshly installed app on
     * another device honours the prior acknowledgment without re-prompting.
     */
    suspend fun bootstrapAcknowledgments(userId: Long): Result<Unit> {
        val result = repository.fetchDisclaimerAcknowledgments()
        result.onSuccess { data ->
            val id = userId.toInt()
            if (!data.carrierTrip.isNullOrBlank() && !carrierStore.hasAcknowledged(id)) {
                carrierStore.setAcknowledged(id)
                carrierStore.setPendingSync(id, pending = false)
            }
            if (!data.shipper.isNullOrBlank() && !shipperStore.hasAcknowledged(id)) {
                shipperStore.setAcknowledged(id)
                shipperStore.setPendingSync(id, pending = false)
            }
        }
        return result.map { }
    }

    /**
     * Marks the local carrier disclaimer acknowledged and fires the network POST. Returns whether
     * the network sync succeeded (so callers can surface a non-blocking "we'll retry later" hint).
     * `pendingSync` is flipped accordingly.
     */
    suspend fun acknowledgeCarrier(userId: Long): Boolean {
        val id = userId.toInt()
        carrierStore.setAcknowledged(id)
        val synced = repository.acknowledgeDisclaimer(CARRIER_TRIP).isSuccess
        carrierStore.setPendingSync(id, pending = !synced)
        return synced
    }

    suspend fun acknowledgeShipper(userId: Long): Boolean {
        val id = userId.toInt()
        shipperStore.setAcknowledged(id)
        val synced = repository.acknowledgeDisclaimer(SHIPPER).isSuccess
        shipperStore.setPendingSync(id, pending = !synced)
        return synced
    }

    /**
     * Retries any pending-sync flags. Returns a [PendingSyncResult] so callers can log/telemetry
     * without re-querying the stores.
     */
    suspend fun retryPendingSyncs(userId: Long): PendingSyncResult {
        val id = userId.toInt()
        val carrierAttempted = carrierStore.isPendingSync(id)
        val carrierSynced = if (carrierAttempted) {
            repository.acknowledgeDisclaimer(CARRIER_TRIP).isSuccess.also { ok ->
                if (ok) carrierStore.setPendingSync(id, pending = false)
            }
        } else {
            false
        }
        val shipperAttempted = shipperStore.isPendingSync(id)
        val shipperSynced = if (shipperAttempted) {
            repository.acknowledgeDisclaimer(SHIPPER).isSuccess.also { ok ->
                if (ok) shipperStore.setPendingSync(id, pending = false)
            }
        } else {
            false
        }
        return PendingSyncResult(
            carrierAttempted = carrierAttempted,
            carrierSynced = carrierSynced,
            shipperAttempted = shipperAttempted,
            shipperSynced = shipperSynced,
        )
    }

    data class PendingSyncResult(
        val carrierAttempted: Boolean,
        val carrierSynced: Boolean,
        val shipperAttempted: Boolean,
        val shipperSynced: Boolean,
    )

    private companion object {
        const val CARRIER_TRIP = "carrier_trip"
        const val SHIPPER = "shipper"
    }
}
