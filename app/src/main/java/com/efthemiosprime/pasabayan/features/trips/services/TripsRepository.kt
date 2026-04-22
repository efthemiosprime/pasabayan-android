package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest

interface TripsRepository {

    /** Carrier's own trips — falls back to `/carrier/trips` if `/trips` returns role error. */
    suspend fun loadCarrierTrips(): Result<List<Trip>>

    /** Shipper browse — available trips with optional filters. */
    suspend fun loadAvailableTrips(filter: TripFilter = TripFilter()): Result<List<Trip>>

    suspend fun loadPopularPackageRoutes(): Result<List<PopularRoute>>

    suspend fun loadRouteActivitySummary(): Result<RouteActivitySummary>

    suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>>

    suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData>

    suspend fun getTrip(id: Int): Result<Trip>

    suspend fun createTrip(request: com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson): Result<Trip>

    suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip>

    suspend fun updateTrip(id: Int, request: com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson): Result<Trip>

    suspend fun deleteTrip(id: Int): Result<Unit>
}
