package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.shared.model.CityCatalog

object TripCityAutocomplete {
    fun suggestions(
        query: String,
        limit: Int = 6,
    ): List<String> {
        if (CityCatalog.containsInPopularCanada(query)) {
            return emptyList()
        }
        return CityCatalog.popularCanadaSuggestions(query, limit)
    }
}
