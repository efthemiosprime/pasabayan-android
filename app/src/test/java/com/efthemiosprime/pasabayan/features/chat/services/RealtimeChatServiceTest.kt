package com.efthemiosprime.pasabayan.features.chat.services

import org.junit.Assert.assertEquals
import org.junit.Test

class RealtimeChatServiceTest {

    @Test
    fun `reconnect backoff uses exponential growth capped at 30 seconds`() {
        assertEquals(2, RealtimeChatServiceImpl.calculateReconnectDelaySeconds(1))
        assertEquals(4, RealtimeChatServiceImpl.calculateReconnectDelaySeconds(2))
        assertEquals(16, RealtimeChatServiceImpl.calculateReconnectDelaySeconds(4))
        assertEquals(30, RealtimeChatServiceImpl.calculateReconnectDelaySeconds(5))
        assertEquals(30, RealtimeChatServiceImpl.calculateReconnectDelaySeconds(7))
    }

    @Test
    fun `max reconnect attempts remains at five`() {
        assertEquals(5, RealtimeChatServiceImpl.MAX_RECONNECT_ATTEMPTS)
    }
}

