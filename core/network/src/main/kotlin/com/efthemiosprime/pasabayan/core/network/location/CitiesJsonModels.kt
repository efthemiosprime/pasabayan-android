package com.efthemiosprime.pasabayan.core.network.location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

@Serializable
data class CitiesResponseJson(
    val success: Boolean,
    val data: List<CityOptionJson> = emptyList(),
)

/**
 * Parity with iOS [CityOption] — `lat` / `lng` may be JSON number or string.
 */
@Serializable
data class CityOptionJson(
    val id: Int,
    val name: String,
    val display: String,
    @SerialName("state_code") val stateCode: String = "",
    val lat: JsonElement? = null,
    val lng: JsonElement? = null,
) {
    fun latString(): String = elementToCoordString(lat)
    fun lngString(): String = elementToCoordString(lng)

    private fun elementToCoordString(el: JsonElement?): String {
        val p = el as? JsonPrimitive ?: return ""
        if (p.isString) return p.content
        p.doubleOrNull?.let { return it.toString() }
        p.longOrNull?.let { return it.toString() }
        return p.content
    }
}
