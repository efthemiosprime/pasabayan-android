package com.efthemiosprime.pasabayan.core.domain.`enum`

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrandDirectionTest {

    @Test
    fun `fromApi maps lowercase api values`() {
        assertEquals(ErrandDirection.RECEIVE, ErrandDirection.fromApi("receive"))
        assertEquals(ErrandDirection.SEND, ErrandDirection.fromApi("send"))
        assertEquals(ErrandDirection.TASK, ErrandDirection.fromApi("task"))
    }

    @Test
    fun `fromApi normalizes mixed case`() {
        // Per the API contract a stray "Send" or "TASK" must NOT silently fall
        // through to RECEIVE — that would mask a backend bug.
        assertEquals(ErrandDirection.SEND, ErrandDirection.fromApi("Send"))
        assertEquals(ErrandDirection.TASK, ErrandDirection.fromApi("TASK"))
        assertEquals(ErrandDirection.RECEIVE, ErrandDirection.fromApi("Receive"))
    }

    @Test
    fun `fromApi defaults to RECEIVE for null and unknown values`() {
        // Legacy delivery rows arrive with no direction.
        assertEquals(ErrandDirection.RECEIVE, ErrandDirection.fromApi(null))
        assertEquals(ErrandDirection.RECEIVE, ErrandDirection.fromApi(""))
        assertEquals(ErrandDirection.RECEIVE, ErrandDirection.fromApi("nonsense"))
    }

    @Test
    fun `apiValue round-trips through fromApi`() {
        for (direction in ErrandDirection.entries) {
            assertEquals(direction, ErrandDirection.fromApi(direction.apiValue))
        }
    }

    @Test
    fun `apiValue is lowercase of name`() {
        assertEquals("receive", ErrandDirection.RECEIVE.apiValue)
        assertEquals("send", ErrandDirection.SEND.apiValue)
        assertEquals("task", ErrandDirection.TASK.apiValue)
    }
}
