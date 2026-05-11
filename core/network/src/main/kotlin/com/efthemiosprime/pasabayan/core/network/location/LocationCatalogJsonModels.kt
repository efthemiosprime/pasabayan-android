package com.efthemiosprime.pasabayan.core.network.location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response for `GET /locations/catalog` (iOS parity:
 * `LocationCatalogResponse` in `LocationCatalogModels.swift`).
 */
@Serializable
data class LocationCatalogResponseJson(
    val success: Boolean = false,
    val version: String = "",
    val data: List<CatalogCountryJson> = emptyList(),
)

@Serializable
data class CatalogCountryJson(
    val name: String = "",
    val iso2: String = "",
    val iso3: String = "",
    @SerialName("phone_code") val phoneCode: String? = null,
    val currency: String? = null,
    val states: List<CatalogStateJson> = emptyList(),
)

@Serializable
data class CatalogStateJson(
    val name: String = "",
    val code: String = "",
    val cities: List<CatalogCityJson> = emptyList(),
)

@Serializable
data class CatalogCityJson(
    val id: Int = 0,
    val name: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val aliases: List<CatalogAliasJson>? = null,
)

@Serializable
data class CatalogAliasJson(
    val alias: String = "",
    val type: String = "",
)
