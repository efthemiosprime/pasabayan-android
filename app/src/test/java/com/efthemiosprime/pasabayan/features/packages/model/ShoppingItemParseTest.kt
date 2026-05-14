package com.efthemiosprime.pasabayan.features.packages.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parity with iOS `AvailablePackage.parsedShoppingList` —
 * accepts both stringified-JSON and raw-array forms; malformed input returns `[]`.
 */
class ShoppingItemParseTest {

    @Test
    fun `null payload returns empty list`() {
        assertTrue(parseShoppingItems(null).isEmpty())
    }

    @Test
    fun `json null payload returns empty list`() {
        assertTrue(parseShoppingItems(JsonNull).isEmpty())
    }

    @Test
    fun `raw JSON array decodes into items`() {
        val payload = Json.parseToJsonElement(
            """[
                {"item":"Milk 2L","quantity":"2","notes":"2% fat"},
                {"item":"Eggs","quantity":"1","notes":"Large"}
            ]""".trimIndent(),
        )
        val items = parseShoppingItems(payload)
        assertEquals(2, items.size)
        assertEquals("Milk 2L", items[0].item)
        assertEquals("2", items[0].quantity)
        assertEquals("2% fat", items[0].notes)
        assertEquals(null, items[1].notes.takeIf { it == "" })
    }

    @Test
    fun `stringified JSON array decodes into items`() {
        val payload = JsonPrimitive(
            """[{"item":"Bread","quantity":"3"},{"item":"Coffee","quantity":""}]""",
        )
        val items = parseShoppingItems(payload)
        assertEquals(2, items.size)
        assertEquals("Bread", items[0].item)
        assertEquals("3", items[0].quantity)
        assertEquals("Coffee", items[1].item)
        assertEquals("", items[1].quantity)
    }

    @Test
    fun `malformed string payload returns empty list`() {
        val payload = JsonPrimitive("not-json")
        assertTrue(parseShoppingItems(payload).isEmpty())
    }

    @Test
    fun `empty array returns empty list`() {
        val payload = Json.parseToJsonElement("[]")
        assertTrue(parseShoppingItems(payload).isEmpty())
    }

    @Test
    fun `entries missing item are dropped`() {
        val payload = Json.parseToJsonElement(
            """[{"item":"","quantity":"1"},{"quantity":"5"},{"item":"Apples","quantity":"2"}]""",
        )
        val items = parseShoppingItems(payload)
        assertEquals(1, items.size)
        assertEquals("Apples", items[0].item)
    }

    @Test
    fun `blank notes normalize to null`() {
        val payload = Json.parseToJsonElement(
            """[{"item":"Tea","quantity":"1","notes":"   "}]""",
        )
        val items = parseShoppingItems(payload)
        assertEquals(1, items.size)
        assertEquals(null, items[0].notes)
    }
}
