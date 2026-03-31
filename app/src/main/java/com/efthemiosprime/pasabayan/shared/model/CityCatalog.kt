package com.efthemiosprime.pasabayan.shared.model

object CityCatalog {
    private val cityDataByCountry: Map<String, List<String>> = mapOf(
        CountryCatalog.CODE_CANADA to listOf(
            "Calgary, AB",
            "Edmonton, AB",
            "Halifax, NS",
            "Hamilton, ON",
            "Mississauga, ON",
            "Montreal, QC",
            "Ottawa, ON",
            "Quebec City, QC",
            "Toronto, ON",
            "Vancouver, BC",
            "Victoria, BC",
            "Winnipeg, MB",
        ),
        CountryCatalog.CODE_PHILIPPINES to listOf(
            "Cebu City, VII",
            "Davao City, XI",
            "Makati, NCR",
            "Manila, NCR",
            "Pasay, NCR",
            "Pasig, NCR",
            "Quezon City, NCR",
            "Taguig, NCR",
        ),
        CountryCatalog.CODE_INDIA to listOf(
            "Bengaluru, KA",
            "Chennai, TN",
            "Hyderabad, TG",
            "Kolkata, WB",
            "Mumbai, MH",
            "New Delhi, DL",
            "Pune, MH",
        ),
    )

    private val popularCanadianCities = listOf(
        "Calgary",
        "Edmonton",
        "Halifax",
        "Hamilton",
        "Mississauga",
        "Montreal",
        "Ottawa",
        "Quebec City",
        "Regina",
        "Saskatoon",
        "Toronto",
        "Vancouver",
        "Victoria",
        "Winnipeg",
    )

    fun cities(countryCode: String): List<String> =
        cityDataByCountry[countryCode].orEmpty()

    fun suggestionsForCountry(
        query: String,
        countryCode: String,
        limit: Int = 8,
    ): List<String> = suggestions(
        query = query,
        values = cities(countryCode),
        limit = limit,
    )

    fun containsInCountry(
        cityName: String,
        countryCode: String,
    ): Boolean = contains(
        query = cityName,
        values = cities(countryCode),
    )

    fun popularCanadaSuggestions(
        query: String,
        limit: Int = 6,
    ): List<String> = suggestions(
        query = query,
        values = popularCanadianCities,
        limit = limit,
    )

    fun containsInPopularCanada(cityName: String): Boolean = contains(
        query = cityName,
        values = popularCanadianCities,
    )

    private fun suggestions(
        query: String,
        values: List<String>,
        limit: Int,
    ): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return values
            .filter { it.contains(trimmed, ignoreCase = true) }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
            .take(limit)
    }

    private fun contains(
        query: String,
        values: List<String>,
    ): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        return values.any { it.equals(trimmed, ignoreCase = true) }
    }
}
