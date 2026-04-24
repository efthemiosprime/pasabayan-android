package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.core.network.chat.ChannelAuthResponseJson
import com.efthemiosprime.pasabayan.core.network.chat.ChatApi
import com.efthemiosprime.pasabayan.core.network.chat.ConversationSummaryJson
import com.efthemiosprime.pasabayan.core.network.chat.ConversationsResponseJson
import com.efthemiosprime.pasabayan.core.network.chat.ParticipantJson
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryImplTest {

    private val chatApi: ChatApi = mockk()
    private val repository = ChatRepositoryImpl(
        chatApi = chatApi,
        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        },
    )

    @Test
    fun `loadConversations maps network DTOs to domain`() = runTest {
        coEvery {
            chatApi.getConversations(any(), any(), any())
        } returns Response.success(
            ConversationsResponseJson(
                data = listOf(
                    ConversationSummaryJson(
                        id = 5,
                        status = "active",
                        statusDisplay = "Active",
                        unreadCount = 2,
                        otherParticipant = ParticipantJson(
                            id = 2,
                            name = "Carrier",
                            avatar = null,
                            verificationLevel = "verified",
                        ),
                    ),
                ),
            ),
        )

        val result = repository.loadConversations()

        assertTrue(result.isSuccess)
        val conversations = result.getOrNull().orEmpty()
        assertEquals(1, conversations.size)
        assertEquals("Carrier", conversations.first().otherParticipant.name)
        assertEquals(2, conversations.first().unreadCount)
    }

    @Test
    fun `authenticateChannel returns auth signature`() = runTest {
        coEvery {
            chatApi.authenticateChannel(any(), any(), any())
        } returns Response.success(ChannelAuthResponseJson(auth = "signature"))

        val result = repository.authenticateChannel(
            config = ReverbConfig(
                key = "pasabayan",
                host = "api.pasabayan.com",
                port = 443,
                scheme = "https",
            ),
            channelName = "private-chat.10",
            socketId = "1234.5678",
        )

        assertTrue(result.isSuccess)
        assertEquals("signature", result.getOrNull())
    }
}

