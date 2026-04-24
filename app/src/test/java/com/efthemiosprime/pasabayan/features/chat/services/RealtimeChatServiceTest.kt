package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeChatServiceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

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

    @Test
    fun `connect flow extracts socket id authenticates subscribes and emits chat message`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()

        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth-signature")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        val collectedMessageIds = mutableListOf<Int>()
        val collectJob = backgroundScope.launch {
            service.incomingMessages.collect { collectedMessageIds += it.id }
        }

        val config = ReverbConfig("pasabayan", "reverb.example.com", 443, "https")
        service.connect(config)
        service.subscribe(42)

        listenerSlot.captured.onOpen(webSocket, mockk<Response>(relaxed = true))
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"connection_established","data":"{\"socket_id\":\"1001.2002\"}"}""",
        )
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":"{\"chat_message\":${messageJson(id = 501, text = "hello realtime")}}"}""",
        )
        advanceUntilIdle()

        assertTrue(service.isConnected.value)
        assertEquals("1001.2002", service.socketId.value)
        coVerify {
            chatRepository.authenticateChannel(any(), "private-chat.42", "1001.2002")
        }
        verify {
            webSocket.send(match<String> { payload ->
                payload.contains("pusher:subscribe") &&
                    payload.contains("private-chat.42") &&
                    payload.contains("auth-signature")
            })
        }
        assertEquals(listOf(501), collectedMessageIds)

        collectJob.cancel()
    }

    @Test
    fun `parser handles wrapped object payload and direct object payload`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        val ids = mutableListOf<Int>()
        val collectJob = backgroundScope.launch {
            service.incomingMessages.collect { ids += it.id }
        }

        service.connect(ReverbConfig("pasabayan", "reverb.example.com", 443, "https"))

        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":{"chat_message":${messageJson(id = 601, text = "wrapped object")}}}""",
        )
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":${messageJson(id = 602, text = "direct object")}}""",
        )
        advanceUntilIdle()

        assertEquals(listOf(601, 602), ids)
        collectJob.cancel()
    }

    @Test
    fun `parser handles JSON-string and double-quoted JSON-string payload variants`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        val ids = mutableListOf<Int>()
        val collectJob = backgroundScope.launch {
            service.incomingMessages.collect { ids += it.id }
        }

        service.connect(ReverbConfig("pasabayan", "reverb.example.com", 443, "https"))

        val dataObject = """{"chat_message":${messageJson(id = 701, text = "string wrapped")}}"""
        val encoded = json.encodeToString(dataObject)
        val doubleEncoded = json.encodeToString(encoded)
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":$encoded}""",
        )
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":$doubleEncoded}""",
        )
        advanceUntilIdle()

        assertEquals(listOf(701, 701), ids)
        collectJob.cancel()
    }

    @Test
    fun `parser ignores malformed chat payload without crashing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        var emissionCount = 0
        val collectJob = backgroundScope.launch {
            service.incomingMessages.collect { emissionCount += 1 }
        }

        service.connect(ReverbConfig("pasabayan", "reverb.example.com", 443, "https"))
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"chat.message","data":"not-json"}""",
        )
        advanceUntilIdle()

        assertEquals(0, emissionCount)
        collectJob.cancel()
    }

    @Test
    fun `connection established event resubscribes existing channels across reconnects`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        service.connect(ReverbConfig("pasabayan", "reverb.example.com", 443, "https"))
        service.subscribe(11)
        service.subscribe(22)

        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"connection_established","data":"{\"socket_id\":\"1.1\"}"}""",
        )
        listenerSlot.captured.onMessage(
            webSocket,
            """{"event":"connection_established","data":"{\"socket_id\":\"2.2\"}"}""",
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) { chatRepository.authenticateChannel(any(), "private-chat.11", "2.2") }
        coVerify(atLeast = 1) { chatRepository.authenticateChannel(any(), "private-chat.22", "2.2") }
    }

    @Test
    fun `reconnect emits failed attempts then max exceeded event`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val okHttpClient = mockk<OkHttpClient>()
        val chatRepository = mockk<ChatRepository>()
        val webSocket = mockk<WebSocket>(relaxed = true)
        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket
        coEvery { chatRepository.authenticateChannel(any(), any(), any()) } returns Result.success("auth")

        val service = RealtimeChatServiceImpl(okHttpClient, chatRepository, json, dispatcher)
        val events = mutableListOf<RealtimeChatEvent>()
        val collectJob = backgroundScope.launch {
            service.events.collect { events += it }
        }

        service.connect(ReverbConfig("pasabayan", "reverb.example.com", 443, "https"))
        repeat(RealtimeChatServiceImpl.MAX_RECONNECT_ATTEMPTS + 1) {
            listenerSlot.captured.onFailure(webSocket, RuntimeException("boom"), null)
        }
        advanceUntilIdle()

        val failedAttempts = events.filterIsInstance<RealtimeChatEvent.ConnectionFailed>().map { it.attempt }
        assertEquals(listOf(1, 2, 3, 4, 5), failedAttempts)
        assertTrue(events.last() is RealtimeChatEvent.MaxReconnectExceeded)

        collectJob.cancel()
    }

    private fun messageJson(id: Int, text: String): String = """
        {
          "id": $id,
          "message": "$text",
          "message_type": "text",
          "sender": {"id": 1, "name": "User", "avatar": null},
          "is_read": false,
          "created_at": "2026-04-24T00:00:00Z",
          "read_receipts": {},
          "attachments": [],
          "can_edit": false,
          "can_delete": true,
          "is_deleted": false
        }
    """.trimIndent()
}

