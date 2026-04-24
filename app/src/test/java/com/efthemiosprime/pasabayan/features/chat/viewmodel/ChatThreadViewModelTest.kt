package com.efthemiosprime.pasabayan.features.chat.viewmodel

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.MatchInfo
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig
import com.efthemiosprime.pasabayan.features.chat.model.Sender
import com.efthemiosprime.pasabayan.features.chat.services.ChatMergeLogic
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import com.efthemiosprime.pasabayan.features.chat.services.MessageDeliveryStatus
import com.efthemiosprime.pasabayan.features.chat.services.MessagesPage
import com.efthemiosprime.pasabayan.features.chat.services.RealtimeChatEvent
import com.efthemiosprime.pasabayan.features.chat.services.RealtimeChatService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatThreadViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var fakeRealtime: FakeRealtimeChatService
    private lateinit var viewModel: ChatThreadViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeRepository = FakeChatRepository()
        fakeRealtime = FakeRealtimeChatService()
        viewModel = ChatThreadViewModel(
            chatRepository = fakeRepository,
            realtimeChatService = fakeRealtime,
            chatMergeLogic = ChatMergeLogic(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openConversation loads initial page and marks conversation read`() = runTest {
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.conversationId)
        assertEquals(1, fakeRepository.markConversationReadCalls)
        assertEquals(2, viewModel.uiState.value.messages.size)
        assertTrue(viewModel.uiState.value.isComposerEnabled)
    }

    @Test
    fun `sendMessage optimistic replace on success`() = runTest {
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        viewModel.sendMessage("Hello")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any { it.id == 501 && it.message == "Hello" })
        assertFalse(viewModel.uiState.value.failedMessageTempIds.contains(-1))
    }

    @Test
    fun `sendMessage failure marks temp message as failed`() = runTest {
        fakeRepository.sendFailure = Exception("send failed")
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        viewModel.sendMessage("Fail me")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failedMessageTempIds.isNotEmpty())
    }

    @Test
    fun `retrySend reattempts failed temp message`() = runTest {
        fakeRepository.sendFailure = Exception("send failed")
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()
        viewModel.sendMessage("retry me")
        advanceUntilIdle()

        fakeRepository.sendFailure = null
        val failedId = viewModel.uiState.value.failedMessageTempIds.first()
        viewModel.retrySend(failedId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any { it.id == 501 && it.message == "retry me" })
    }

    @Test
    fun `markMessageRead dedupes duplicate ids`() = runTest {
        viewModel.markMessageRead(10)
        viewModel.markMessageRead(10)
        viewModel.markMessageRead(11)
        advanceUntilIdle()

        assertEquals(listOf(10, 11), fakeRepository.markMessageReadCalls)
    }

    @Test
    fun `loadMoreMessages prepends unique older messages and advances page`() = runTest {
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        viewModel.loadMoreMessages()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.messages.size)
        assertEquals(listOf(1, 2, 3), viewModel.uiState.value.messages.map { it.id })
        assertEquals(3, viewModel.uiState.value.messages.last().id)
        assertEquals(null, viewModel.uiState.value.nextPage)
    }

    @Test
    fun `openConversation with closed status disables composer`() = runTest {
        viewModel.openConversation(conversationId = 10, status = "closed")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isComposerEnabled)
    }

    @Test
    fun `decode failure recovers optimistic message from latest page`() = runTest {
        fakeRepository.sendFailure = DomainErrorMapperException(DomainError.InvalidResponse)
        fakeRepository.pageOneMessages = listOf(
            testMessage(2, "old"),
            testMessage(3, "another"),
            testMessage(880, "Recover me"),
        )
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        viewModel.sendMessage("Recover me")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.any { it.id == 880 && it.message == "Recover me" })
        assertTrue(viewModel.uiState.value.failedMessageTempIds.isEmpty())
    }

    @Test
    fun `openConversation disables composer on unauthorized mapped error`() = runTest {
        fakeRepository.firstPageFailure = DomainErrorMapperException(DomainError.Unauthorized)

        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isComposerEnabled)
    }

    @Test
    fun `polling starts when disconnected stops when connected and resumes on disconnect`() = runTest {
        viewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        fakeRealtime.emitPolling(listOf(testMessage(4)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.messages.any { it.id == 4 })

        fakeRealtime.emitConnection(true)
        advanceUntilIdle()
        fakeRealtime.emitPolling(listOf(testMessage(5)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.messages.any { it.id == 5 })

        fakeRealtime.emitConnection(false)
        advanceUntilIdle()
        fakeRealtime.emitPolling(listOf(testMessage(5)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.messages.any { it.id == 5 })
    }

    @Test
    fun `high volume polling coalesces updates into delayed apply`() = runTest {
        var nowMs = 1_000L
        val highVolumeViewModel = ChatThreadViewModel(
            chatRepository = fakeRepository,
            realtimeChatService = fakeRealtime,
            chatMergeLogic = ChatMergeLogic(),
        )
        highVolumeViewModel.setNowMsProviderForTesting { nowMs }
        fakeRepository.pageOneMessages = (1..201).map { testMessage(it) }

        highVolumeViewModel.openConversation(conversationId = 10, status = "active")
        advanceUntilIdle()

        highVolumeViewModel.applyPolledMessagesForTesting((1..202).map { testMessage(it) })
        advanceUntilIdle()
        assertEquals(202, highVolumeViewModel.uiState.value.messages.maxOf { it.id })

        nowMs = 1_200L
        highVolumeViewModel.applyPolledMessagesForTesting((1..203).map { testMessage(it) })
        advanceUntilIdle()
        assertEquals(202, highVolumeViewModel.uiState.value.messages.maxOf { it.id })

        nowMs = 2_200L
        advanceTimeBy(900L)
        advanceUntilIdle()
        assertEquals(203, highVolumeViewModel.uiState.value.messages.maxOf { it.id })
    }
}

private class FakeChatRepository : ChatRepository {
    var sendFailure: Throwable? = null
    var firstPageFailure: Throwable? = null
    var pageOneMessages: List<MessageItem> = listOf(testMessage(2), testMessage(3))
    var pageTwoMessages: List<MessageItem> = listOf(testMessage(1), testMessage(2))
    var markConversationReadCalls = 0
    val markMessageReadCalls = mutableListOf<Int>()

    override suspend fun loadBroadcastingConfig(): Result<ReverbConfig> = Result.success(
        ReverbConfig("pasabayan", "example.com", 443, "https"),
    )

    override suspend fun loadConversations(
        role: String?,
        status: String?,
        unreadOnly: Boolean?,
    ): Result<List<ConversationSummary>> = Result.success(
        listOf(
            ConversationSummary(
                id = 1,
                matchId = 1,
                status = "active",
                statusDisplay = "Active",
                userRole = "shipper",
                otherParticipant = Participant(2, "Carrier", null, null),
                matchInfo = MatchInfo("A-B", null, null, null),
                unreadCount = 1,
                lastMessage = LastMessage(1, "Hi", "text", null, null),
                lastMessageAt = null,
                createdAt = null,
            ),
        ),
    )

    override suspend fun loadConversationDetail(conversationId: Int): Result<ConversationSummary> {
        throw NotImplementedError()
    }

    override suspend fun loadMessages(conversationId: Int, page: Int): Result<MessagesPage> {
        if (page == 1 && firstPageFailure != null) {
            return Result.failure(firstPageFailure!!)
        }
        return if (page == 1) {
            Result.success(
                MessagesPage(
                    messages = pageOneMessages,
                    currentPage = 1,
                    lastPage = 2,
                ),
            )
        } else {
            Result.success(
                MessagesPage(
                    messages = pageTwoMessages,
                    currentPage = 2,
                    lastPage = 2,
                ),
            )
        }
    }

    override suspend fun sendMessage(conversationId: Int, message: String, type: String): Result<MessageItem> {
        return if (sendFailure != null) {
            Result.failure(sendFailure!!)
        } else {
            Result.success(testMessage(501, message))
        }
    }

    override suspend fun deleteMessage(messageId: Int): Result<String?> = Result.success("2026-04-24T00:00:00Z")

    override suspend fun markConversationRead(conversationId: Int): Result<Unit> {
        markConversationReadCalls += 1
        return Result.success(Unit)
    }

    override suspend fun markMessageRead(messageId: Int): Result<Unit> {
        markMessageReadCalls += messageId
        return Result.success(Unit)
    }

    override suspend fun getMessageStatus(messageId: Int): Result<MessageDeliveryStatus> = Result.success(
        MessageDeliveryStatus("read", emptyMap()),
    )

    override suspend fun authenticateChannel(
        config: ReverbConfig,
        channelName: String,
        socketId: String,
    ): Result<String> = Result.success("auth-token")

}

private class FakeRealtimeChatService : RealtimeChatService {
    private val _isConnected = MutableStateFlow(false)
    private val _events = MutableSharedFlow<RealtimeChatEvent>()
    private val _incomingMessages = MutableSharedFlow<MessageItem>()
    private val _socketId = MutableStateFlow<String?>(null)
    private val _pollingMessages = MutableSharedFlow<List<MessageItem>>()

    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    override val events: Flow<RealtimeChatEvent> = _events.asSharedFlow()
    override val incomingMessages: Flow<MessageItem> = _incomingMessages.asSharedFlow()
    override val socketId: StateFlow<String?> = _socketId.asStateFlow()

    override fun connect(config: ReverbConfig) = Unit
    override fun disconnect() = Unit
    override fun subscribe(conversationId: Int) = Unit
    override fun unsubscribe(conversationId: Int) = Unit
    override fun pollingMessages(conversationId: Int): Flow<List<MessageItem>> = _pollingMessages.asSharedFlow()

    suspend fun emitConnection(connected: Boolean) {
        _isConnected.emit(connected)
    }

    suspend fun emitPolling(messages: List<MessageItem>) {
        _pollingMessages.emit(messages)
    }
}

private fun testMessage(id: Int, text: String = "message-$id") = MessageItem(
    id = id,
    message = text,
    messageType = "text",
    sender = Sender(1, "User", null),
    isRead = false,
    createdAt = "",
    formattedMessage = null,
    messageTypeDisplay = null,
    readAt = null,
    readReceipts = emptyMap(),
    deliveryStatus = "sent",
    deliveredAt = null,
    attachments = emptyList(),
    canEdit = false,
    canDelete = true,
    isDeleted = false,
    deletedAt = null,
    metadata = null,
)

