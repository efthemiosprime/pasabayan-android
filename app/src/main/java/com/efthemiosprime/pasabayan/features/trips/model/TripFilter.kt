package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.SortOrder

data class TripFilter(
    val searchText: String = "",
    val origin: String = "",
    val destination: String = "",
    val radius: Double? = null,
    val sortBy: TripSortOption = TripSortOption.DEPARTURE_DATE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val cityId: Int? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val page: Int = 1,
) {
    val isEmpty: Boolean
        get() = searchText.isBlank() && origin.isBlank() && destination.isBlank() &&
            radius == null && cityId == null

    fun toQueryMap(): Map<String, String> = buildMap {
        if (searchText.isNotBlank()) put("q", searchText)
        if (origin.isNotBlank()) put("origin", origin)
        if (destination.isNotBlank()) put("destination", destination)
        radius?.let { put("radius", it.toString()) }
        put("sort_by", sortBy.wireValue)
        put("sort_order", if (sortOrder == SortOrder.ASCENDING) "asc" else "desc")
        cityId?.let { put("city_id", it.toString()) }
        lat?.let { put("lat", it.toString()) }
        lng?.let { put("lng", it.toString()) }
        if (page > 1) put("page", page.toString())
    }
}
