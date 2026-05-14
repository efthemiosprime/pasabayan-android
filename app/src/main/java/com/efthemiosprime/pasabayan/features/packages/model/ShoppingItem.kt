package com.efthemiosprime.pasabayan.features.packages.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val item: String,
    val quantity: String,
    val notes: String? = null,
)

/**
 * Parse a `shopping_list` JSON payload into [ShoppingItem]s.
 *
 * Mirrors iOS `AvailablePackage.parsedShoppingList` — accepts both the legacy
 * stringified-JSON form (`"\"[{\\\"item\\\":...}]\""`) and the modern array
 * form (`[{ "item": ..., "quantity": ..., "notes": ... }]`). Malformed or
 * absent payloads return an empty list rather than throwing.
 */
fun parseShoppingItems(payload: JsonElement?): List<ShoppingItem> {
    val array: JsonArray = when (payload) {
        null -> return emptyList()
        is JsonArray -> payload
        is JsonPrimitive -> {
            val raw = payload.contentOrNull?.takeIf { it.isNotBlank() } ?: return emptyList()
            runCatching { Json.parseToJsonElement(raw).jsonArray }.getOrNull() ?: return emptyList()
        }
        else -> return emptyList()
    }
    return array.mapNotNull { entry ->
        val obj = entry as? JsonObject ?: return@mapNotNull null
        val item = obj["item"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: return@mapNotNull null
        val quantity = obj["quantity"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val notes = obj["notes"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        ShoppingItem(item = item, quantity = quantity, notes = notes)
    }
}
