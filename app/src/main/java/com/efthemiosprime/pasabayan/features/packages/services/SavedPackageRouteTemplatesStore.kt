package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PickupTemplate(
    val pickupCountryCode: String,
    val pickupCity: String,
    val pickupAddress: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class HandoffTemplate(
    val deliveryCountryCode: String,
    val deliveryCity: String,
    val deliveryAddress: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Pickup + handoff route templates — max 5 each, sorted by createdAt desc.
 * Keys: `saved_pickup_templates_v1`, `saved_handoff_templates_v1`.
 */
@Singleton
class SavedPackageRouteTemplatesStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val json: Json,
) {
    private val pickupKey = "saved_pickup_templates_v1"
    private val handoffKey = "saved_handoff_templates_v1"
    private val maxTemplates = 5

    fun getPickupTemplates(): List<PickupTemplate> = loadPickupList()

    fun savePickupTemplate(template: PickupTemplate) {
        saveToList(pickupKey, template, ::loadPickupList)
    }

    fun getHandoffTemplates(): List<HandoffTemplate> = loadHandoffList()

    fun saveHandoffTemplate(template: HandoffTemplate) {
        saveHandoffToList(template)
    }

    private fun loadPickupList(): List<PickupTemplate> {
        val raw = prefs.getString(pickupKey, null) ?: return emptyList()
        return try {
            json.parseToJsonElement(raw).asJsonArray()
                .mapNotNull { element ->
                    val obj = element as? JsonObject ?: return@mapNotNull null
                    PickupTemplate(
                        pickupCountryCode = obj["pickupCountryCode"].asString(),
                        pickupCity = obj["pickupCity"].asString(),
                        pickupAddress = obj["pickupAddress"]?.asNullableString(),
                        createdAt = obj["createdAt"].asLongOrZero(),
                    )
                }
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadHandoffList(): List<HandoffTemplate> {
        val raw = prefs.getString(handoffKey, null) ?: return emptyList()
        return try {
            json.parseToJsonElement(raw).asJsonArray()
                .mapNotNull { element ->
                    val obj = element as? JsonObject ?: return@mapNotNull null
                    HandoffTemplate(
                        deliveryCountryCode = obj["deliveryCountryCode"].asString(),
                        deliveryCity = obj["deliveryCity"].asString(),
                        deliveryAddress = obj["deliveryAddress"]?.asNullableString(),
                        createdAt = obj["createdAt"].asLongOrZero(),
                    )
                }
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveToList(key: String, template: PickupTemplate, loader: () -> List<PickupTemplate>) {
        val current = loader().toMutableList()
        current.add(0, template)
        val trimmed = current.take(maxTemplates)
        prefs.edit().putString(
            key,
            buildJsonArray {
                trimmed.forEach { template ->
                    add(
                        JsonObject(
                            mapOf(
                                "pickupCountryCode" to JsonPrimitive(template.pickupCountryCode),
                                "pickupCity" to JsonPrimitive(template.pickupCity),
                                "pickupAddress" to (template.pickupAddress?.let(::JsonPrimitive) ?: JsonNull),
                                "createdAt" to JsonPrimitive(template.createdAt),
                            ),
                        ),
                    )
                }
            }.toString(),
        ).apply()
    }

    private fun saveHandoffToList(template: HandoffTemplate) {
        val current = loadHandoffList().toMutableList()
        current.add(0, template)
        val trimmed = current.take(maxTemplates)
        prefs.edit().putString(
            handoffKey,
            buildJsonArray {
                trimmed.forEach { template ->
                    add(
                        JsonObject(
                            mapOf(
                                "deliveryCountryCode" to JsonPrimitive(template.deliveryCountryCode),
                                "deliveryCity" to JsonPrimitive(template.deliveryCity),
                                "deliveryAddress" to (template.deliveryAddress?.let(::JsonPrimitive) ?: JsonNull),
                                "createdAt" to JsonPrimitive(template.createdAt),
                            ),
                        ),
                    )
                }
            }.toString(),
        ).apply()
    }

    private fun kotlinx.serialization.json.JsonElement?.asString(): String =
        (this as? JsonPrimitive)?.content.orEmpty()

    private fun kotlinx.serialization.json.JsonElement?.asNullableString(): String? {
        if (this == null || this is JsonNull) return null
        val content = (this as? JsonPrimitive)?.content.orEmpty()
        return content.ifBlank { null }
    }

    private fun kotlinx.serialization.json.JsonElement?.asLongOrZero(): Long =
        (this as? JsonPrimitive)?.content?.toLongOrNull() ?: 0L

    private fun kotlinx.serialization.json.JsonElement.asJsonArray(): JsonArray =
        this as? JsonArray ?: JsonArray(emptyList())
}
