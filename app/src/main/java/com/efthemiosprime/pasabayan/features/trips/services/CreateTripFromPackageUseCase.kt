package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import javax.inject.Inject

class CreateTripFromPackageUseCase @Inject constructor(
    private val tripsRepository: TripsRepository,
) {
    suspend fun loadTemplate(packageId: Int): Result<TripTemplateData> =
        tripsRepository.loadTripTemplate(packageId)

    suspend fun createTrip(request: CreateTripFromPackageRequest): Result<Trip> =
        tripsRepository.createTripFromPackage(request)
}
