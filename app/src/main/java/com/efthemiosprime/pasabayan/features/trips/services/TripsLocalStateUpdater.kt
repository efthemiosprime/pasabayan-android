package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest

interface TripsLocalStateUpdater {
    fun onTripCreationSucceeded(userId: Long, request: CreateTripFromPackageRequest)

    fun onCarrierDisclaimerAcknowledgeAttempt(userId: Long, syncSucceeded: Boolean)

    suspend fun retryPendingCarrierDisclaimerSync(
        userId: Long,
        syncAction: suspend () -> Result<Unit>,
    ): Boolean
}
