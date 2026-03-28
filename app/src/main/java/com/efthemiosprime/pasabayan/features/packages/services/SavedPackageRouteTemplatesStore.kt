package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    fun getPickupTemplates(): List<PickupTemplate> = loadList(pickupKey)

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
            json.decodeFromString<List<PickupTemplate>>(raw).sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadHandoffList(): List<HandoffTemplate> {
        val raw = prefs.getString(handoffKey, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<HandoffTemplate>>(raw).sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private inline fun <reified T> loadList(key: String): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<T>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveToList(key: String, template: PickupTemplate, loader: () -> List<PickupTemplate>) {
        val current = loader().toMutableList()
        current.add(0, template)
        val trimmed = current.take(maxTemplates)
        prefs.edit().putString(key, json.encodeToString(trimmed)).apply()
    }

    private fun saveHandoffToList(template: HandoffTemplate) {
        val current = loadHandoffList().toMutableList()
        current.add(0, template)
        val trimmed = current.take(maxTemplates)
        prefs.edit().putString(handoffKey, json.encodeToString(trimmed)).apply()
    }
}
