package com.efthemiosprime.pasabayan.features.chat.viewmodel

import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.MatchInfo
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import com.efthemiosprime.pasabayan.features.chat.services.ConversationsPage
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `loadConversations computes unread total and seeds pagination state`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 1), conversation(2, 2)),
            currentPage = 1,
            lastPage = 3,
            total = 30,
        )

        viewModel.loadConversations()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.conversations.size)
        assertEquals(3, state.allUnreadCount)
        assertEquals(1, state.currentPage)
        assertEquals(3, state.lastPage)
        assertTrue(state.hasMore)
        assertNull(state.alertMessage)
    }

    @Test
    fun `markConversationOpened zeroes local unread count`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 1), conversation(2, 2)),
            currentPage = 1,
            lastPage = 1,
            total = 2,
        )

        viewModel.loadConversations()
        advanceUntilIdle()

        viewModel.markConversationOpened(2)
        assertEquals(1, viewModel.uiState.value.allUnreadCount)
    }

    @Test
    fun `loadConversations forwards role status unread and pagination params`() = runTest {
        viewModel.loadConversations(role = "shipper", status = "active", unreadOnly = true)
        advanceUntilIdle()

        assertEquals("shipper", repository.lastRole)
        assertEquals("active", repository.lastStatus)
        assertEquals(true, repository.lastUnreadOnly)
        assertEquals(1, repository.lastPage)
        assertEquals(ChatRepository.DEFAULT_PER_PAGE, repository.lastPerPage)
    }

    @Test
    fun `loadNextConversationsPage appends results and dedupes by id`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 1), conversation(2, 0)),
            currentPage = 1,
            lastPage = 2,
            total = 3,
        )
        repository.pages[2] = ConversationsPage(
            // page 2 returns conversation #2 (because of activity bump) plus #3
            conversations = listOf(conversation(2, 0), conversation(3, 0)),
            currentPage = 2,
            lastPage = 2,
            total = 3,
        )

        viewModel.loadConversations()
        advanceUntilIdle()
        viewModel.loadNextConversationsPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.conversations.size) // dedup removed the duplicate id=2
        assertEquals(listOf(1, 2, 3), state.conversations.map { it.id })
        assertEquals(2, state.currentPage)
        assertFalse(state.hasMore)
        assertFalse(state.isLoadingMore)
        assertNull(state.loadMoreError)
    }

    @Test
    fun `loadNextConversationsPage is a no-op when already at last page`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 0)),
            currentPage = 1,
            lastPage = 1,
            total = 1,
        )

        viewModel.loadConversations()
        advanceUntilIdle()

        viewModel.loadNextConversationsPage()
        advanceUntilIdle()

        // No second-page call was made
        assertEquals(1, repository.lastPage)
    }

    @Test
    fun `loadNextConversationsPage reuses current filters from initial load`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 0)),
            currentPage = 1,
            lastPage = 3,
            total = 30,
        )
        repository.pages[2] = ConversationsPage(
            conversations = listOf(conversation(2, 0)),
            currentPage = 2,
            lastPage = 3,
            total = 30,
        )

        viewModel.loadConversations(role = "carrier", status = "active", unreadOnly = false)
        advanceUntilIdle()

        viewModel.loadNextConversationsPage()
        advanceUntilIdle()

        assertEquals("carrier", repository.lastRole)
        assertEquals("active", repository.lastStatus)
        assertEquals(false, repository.lastUnreadOnly)
        assertEquals(2, repository.lastPage)
    }

    @Test
    fun `loadNextConversationsPage surfaces error and retry recovers`() = runTest {
        repository.pages[1] = ConversationsPage(
            conversations = listOf(conversation(1, 0)),
            currentPage = 1,
            lastPage = 2,
            total = 2,
        )
        repository.failPage2WithMessage = "Network unavailable"

        viewModel.loadConversations()
        advanceUntilIdle()

        viewModel.loadNextConversationsPage()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.loadMoreError)
        assertFalse(viewModel.uiState.value.isLoadingMore)
        assertEquals(1, viewModel.uiState.value.conversations.size)

        // Retry: clear the failure and verify success
        repository.failPage2WithMessage = null
        repository.pages[2] = ConversationsPage(
            conversations = listOf(conversation(2, 0)),
            currentPage = 2,
            lastPage = 2,
            total = 2,
        )

        viewModel.retryLoadMoreConversations()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadMoreError)
        assertEquals(2, viewModel.uiState.value.conversations.size)
    }

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

private class FakeConversationsRepo : ChatRepository {
    var lastRole: String? = null
    var lastStatus: String? = null
    var lastUnreadOnly: Boolean? = null
    var lastPage: Int = 0
    var lastPerPage: Int = 0
    val pages: MutableMap<Int, ConversationsPage> = mutableMapOf()
    var failPage2WithMessage: String? = null

    override suspend fun loadBroadcastingConfig(): Result<ReverbConfig> = Result.failure(Exception("not needed"))

    override suspend fun loadConversations(
        role: String?,
        status: String?,
        unreadOnly: Boolean?,
        page: Int,
        perPage: Int,
    ): Result<ConversationsPage> {
        lastRole = role
        lastStatus = status
        lastUnreadOnly = unreadOnly
        lastPage = page
        lastPerPage = perPage
        if (page == 2 && failPage2WithMessage != null) {
            return Result.failure(RuntimeException(failPage2WithMessage))
        }
        return Result.success(
            pages[page] ?: ConversationsPage(emptyList(), page, page, 0),
        )
    }

    override suspend fun loadConversationDetail(conversationId: Int): Result<ConversationSummary> = Result.failure(Exception("not needed"))
    override suspend fun loadMessages(conversationId: Int, page: Int): Result<MessagesPage> = Result.failure(Exception("not needed"))
    override suspend fun sendMessage(conversationId: Int, message: String, type: String) = Result.failure<com.efthemiosprime.pasabayan.features.chat.model.MessageItem>(Exception("not needed"))
    override suspend fun deleteMessage(messageId: Int): Result<String?> = Result.failure(Exception("not needed"))
    override suspend fun markConversationRead(conversationId: Int): Result<Unit> = Result.failure(Exception("not needed"))
    override suspend fun markMessageRead(messageId: Int): Result<Unit> = Result.failure(Exception("not needed"))
    override suspend fun getMessageStatus(messageId: Int): Result<MessageDeliveryStatus> = Result.failure(Exception("not needed"))
    override suspend fun authenticateChannel(config: ReverbConfig, channelName: String, socketId: String): Result<String> = Result.failure(Exception("not needed"))
}

