package com.efthemiosprime.pasabayan.features.chat.viewmodel

import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.MatchInfo
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import com.efthemiosprime.pasabayan.features.chat.services.MessageDeliveryStatus
import com.efthemiosprime.pasabayan.features.chat.services.MessagesPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeConversationsRepo
    private lateinit var viewModel: ConversationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeConversationsRepo()
        viewModel = ConversationsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadConversations computes unread total`() = runTest {
        viewModel.loadConversations()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.conversations.size)
        assertEquals(3, viewModel.uiState.value.allUnreadCount)
        assertNull(viewModel.uiState.value.alertMessage)
    }

    @Test
    fun `markConversationOpened zeroes local unread count`() = runTest {
        viewModel.loadConversations()
        advanceUntilIdle()

        viewModel.markConversationOpened(2)
        assertEquals(1, viewModel.uiState.value.allUnreadCount)
    }
}

private class FakeConversationsRepo : ChatRepository {
    override suspend fun loadBroadcastingConfig(): Result<ReverbConfig> = Result.failure(Exception("not needed"))

    override suspend fun loadConversations(
        role: String?,
        status: String?,
        unreadOnly: Boolean?,
    ): Result<List<ConversationSummary>> = Result.success(
        listOf(
            conversation(1, 1),
            conversation(2, 2),
        ),
    )

    override suspend fun loadConversationDetail(conversationId: Int): Result<ConversationSummary> = Result.failure(Exception("not needed"))
    override suspend fun loadMessages(conversationId: Int, page: Int): Result<MessagesPage> = Result.failure(Exception("not needed"))
    override suspend fun sendMessage(conversationId: Int, message: String, type: String) = Result.failure<com.efthemiosprime.pasabayan.features.chat.model.MessageItem>(Exception("not needed"))
    override suspend fun deleteMessage(messageId: Int): Result<String?> = Result.failure(Exception("not needed"))
    override suspend fun markConversationRead(conversationId: Int): Result<Unit> = Result.failure(Exception("not needed"))
    override suspend fun markMessageRead(messageId: Int): Result<Unit> = Result.failure(Exception("not needed"))
    override suspend fun getMessageStatus(messageId: Int): Result<MessageDeliveryStatus> = Result.failure(Exception("not needed"))
    override suspend fun authenticateChannel(config: ReverbConfig, channelName: String, socketId: String): Result<String> = Result.failure(Exception("not needed"))

    private fun conversation(id: Int, unread: Int) = ConversationSummary(
        id = id,
        matchId = id,
        status = "active",
        statusDisplay = "Active",
        userRole = "shipper",
        otherParticipant = Participant(id + 100, "User$id", null, null),
        matchInfo = MatchInfo("A-B", null, null, null),
        unreadCount = unread,
        lastMessage = LastMessage(id, "Hello$id", "text", "User$id", null),
        lastMessageAt = null,
        createdAt = null,
    )
}

