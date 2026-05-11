package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Tolerant parser for the four shapes the backend may return for
 * `GET /notifications/unread-count`. iOS parity: `UnreadCountResponse` decode chain in
 * `NotificationModels.swift`.
 *
 * Priority:
 *  1. Canonical: `{"success": true, "data": {"unread_count": 5}}`
 *  2. Data as bare int: `{"success": true, "data": 3}`
 *  3. Flat at root: `{"unread_count": 5}` / `{"unreadCount": 5}` / `{"count": 5}`
 *  4. Bare number: `5`
 *
 * Returns the parsed count, or `null` if no shape matched.
 */
object UnreadCountResponseParser {

    private val countKeys = listOf("unread_count", "unreadCount", "count")

    fun parse(raw: String, json: Json): Int? {
        val element = runCatching { json.parseToJsonElement(raw) }.getOrNull() ?: return null
        return parseElement(element)
    }

    fun parseElement(element: JsonElement): Int? {
        // Shape 4 — bare number
        (element as? JsonPrimitive)?.let { primitive ->
            return primitive.intOrNull
        }
        val root = element as? JsonObject ?: return null

        // Shape 1 — canonical { data: { unread_count: N } }
        (root["data"] as? JsonObject)?.let { dataObj ->
            extractCount(dataObj)?.let { return it }
        }
        // Shape 2 — { data: N }
        (root["data"] as? JsonPrimitive)?.intOrNull?.let { return it }

        // Shape 3 — flat at root
        return extractCount(root)
    }

    private fun extractCount(obj: JsonObject): Int? {
        for (key in countKeys) {
            (obj[key] as? JsonPrimitive)?.intOrNull?.let { return it }
        }
        return null
    }
}
