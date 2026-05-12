package com.efthemiosprime.pasabayan.features.trips.model

/**
 * Domain envelope for one page of `GET /trips/available`. Mirrors the
 * Laravel pagination block iOS reads via `AvailableTripsPaginatedResult`.
 *
 * `hasMore` is derived from `currentPage` vs `lastPage` — never from
 * "the page came back empty" (which was the brittle pre-Slice-3
 * heuristic). An empty page mid-stream (e.g. after client-side filtering
 * removes every entry on the page) is not the end of the stream.
 */
data class AvailableTripsPage(
    val trips: List<Trip>,
    val currentPage: Int,
    val lastPage: Int,
    val total: Int,
    val perPage: Int,
) {
    val hasMore: Boolean get() = currentPage < lastPage
}
