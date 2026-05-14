package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.core.network.chat.ChannelAuthResponseJson
import com.efthemiosprime.pasabayan.core.network.chat.ChatApi
import com.efthemiosprime.pasabayan.core.network.chat.ConversationSummaryJson
import com.efthemiosprime.pasabayan.core.network.chat.ConversationsResponseJson
import com.efthemiosprime.pasabayan.core.network.chat.PaginatedConversationsJson
import com.efthemiosprime.pasabayan.core.network.chat.ParticipantJson
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `loadConversations maps legacy flat array to single-page envelope`() = runTest {
        coEvery {
            chatApi.getConversations(any(), any(), any(), any(), any())
        } returns Response.success(
            ConversationsResponseJson(
                conversations = listOf(
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
        val page = result.getOrNull()!!
        assertEquals(1, page.conversations.size)
        assertEquals("Carrier", page.conversations.first().otherParticipant.name)
        assertEquals(2, page.conversations.first().unreadCount)
        assertEquals(1, page.currentPage)
        assertEquals(1, page.lastPage)
        assertFalse(page.hasMore)
    }

    @Test
    fun `loadConversations forwards pagination params and surfaces hasMore from paginator`() = runTest {
        coEvery {
            chatApi.getConversations(any(), any(), any(), any(), any())
        } returns Response.success(
            ConversationsResponseJson(
                data = PaginatedConversationsJson(
                    data = listOf(
                        ConversationSummaryJson(
                            id = 9,
                            status = "active",
                            statusDisplay = "Active",
                            unreadCount = 0,
                            otherParticipant = ParticipantJson(id = 3, name = "Shipper", avatar = null),
                        ),
                    ),
                    currentPage = 2,
                    lastPage = 4,
                    perPage = 15,
                    total = 47,
                ),
            ),
        )

        val result = repository.loadConversations(
            role = "shipper",
            status = "active",
            unreadOnly = true,
            page = 2,
            perPage = 15,
        )

        assertTrue(result.isSuccess)
        val page = result.getOrNull()!!
        assertEquals(2, page.currentPage)
        assertEquals(4, page.lastPage)
        assertTrue(page.hasMore)
        coVerify {
            chatApi.getConversations(
                role = "shipper",
                status = "active",
                unreadOnly = true,
                page = 2,
                perPage = 15,
            )
        }
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

