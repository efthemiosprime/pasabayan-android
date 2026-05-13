package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage
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

    /** Shipper browse — available trips with optional filters. Legacy flat-list loader. */
    suspend fun loadAvailableTrips(filter: TripFilter = TripFilter()): Result<List<Trip>>

    /**
     * Paginated browse loader — returns the full Laravel envelope so the VM
     * can derive `hasMore` from `currentPage` vs `lastPage` instead of the
     * brittle "page came back empty" heuristic. iOS parity.
     */
    suspend fun loadAvailableTripsPage(
        filter: TripFilter,
        page: Int,
        perPage: Int = DEFAULT_PER_PAGE,
    ): Result<AvailableTripsPage>

    suspend fun loadPopularPackageRoutes(): Result<List<PopularRoute>>

    suspend fun loadRouteActivitySummary(): Result<RouteActivitySummary>

    suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>>

    suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData>

    suspend fun getTrip(id: Int): Result<Trip>

    suspend fun createTrip(request: com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson): Result<Trip>

    suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip>

    suspend fun updateTrip(id: Int, request: com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson): Result<Trip>

    /**
     * Transitions a planning trip to active via `POST /trips/{id}/activate`.
     * iOS parity: this is the only sanctioned activation path — `updateTrip` with `trip_status`
     * is dropped server-side.
     */
    suspend fun activateTrip(id: Int): Result<Trip>

    suspend fun deleteTrip(id: Int): Result<Unit>

    companion object {
        /** iOS-parity default page size — mirrors `Pagination.defaultPerPage`. */
        const val DEFAULT_PER_PAGE: Int = 15
    }
}
