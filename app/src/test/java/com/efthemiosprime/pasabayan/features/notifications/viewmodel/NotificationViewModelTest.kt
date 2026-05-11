package com.efthemiosprime.pasabayan.features.notifications.viewmodel

import com.efthemiosprime.pasabayan.core.network.notifications.PaginationInfoJson
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationPage
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.services.DeviceTokenRegisterResult
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRepository
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRouter
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeRepo
    private lateinit var router: NotificationRouter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeRepo()
        router = NotificationRouter()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = NotificationViewModel(fakeRepo, router)

    // -- loadNotifications --

    @Test
    fun `loadNotifications populates list and pagination`() = runTest {
        fakeRepo.pageResult = Result.success(
            NotificationPage(
                notifications = listOf(notif(1, NotificationType.CHAT_MESSAGE)),
                pagination = PaginationInfoJson(currentPage = 1, lastPage = 2, perPage = 15, total = 30),
            ),
        )
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(1, state.notifications.size)
        assertFalse(state.isLoading)
        assertTrue(state.hasMore)
    }

    @Test
    fun `loadNextPage appends and respects hasMore`() = runTest {
        fakeRepo.pageResult = Result.success(
            NotificationPage(
                notifications = listOf(notif(1, NotificationType.CHAT_MESSAGE)),
                pagination = PaginationInfoJson(currentPage = 1, lastPage = 2, perPage = 15, total = 2),
            ),
        )
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()

        fakeRepo.pageResult = Result.success(
            NotificationPage(
                notifications = listOf(notif(2, NotificationType.CHAT_MESSAGE)),
                pagination = PaginationInfoJson(currentPage = 2, lastPage = 2, perPage = 15, total = 2),
            ),
        )
        vm.loadNextPage()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.notifications.size)
        assertFalse(state.hasMore)
    }

    // -- loadUnreadCounts --

    @Test
    fun `loadUnreadCounts populates all three counters in parallel`() = runTest {
        fakeRepo.totalUnread = 7
        fakeRepo.carrierUnread = 3
        fakeRepo.shipperUnread = 4
        val vm = viewModel()
        vm.loadUnreadCounts()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(7, state.unreadCount)
        assertEquals(3, state.carrierUnreadCount)
        assertEquals(4, state.shipperUnreadCount)
        assertEquals(3, fakeRepo.unreadCountCalls.size)
    }

    // -- markAsRead --

    @Test
    fun `markAsRead optimistically updates local state and decrements role count`() = runTest {
        val carrierNotif = notif(10, NotificationType.MATCH_REQUEST, recipientRole = "carrier")
        fakeRepo.pageResult = Result.success(
            NotificationPage(
                notifications = listOf(carrierNotif),
                pagination = null,
            ),
        )
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        // Seed counts so we can observe decrement
        fakeRepo.totalUnread = 5
        fakeRepo.carrierUnread = 5
        fakeRepo.shipperUnread = 2
        vm.loadUnreadCounts()
        advanceUntilIdle()
        fakeRepo.markAsReadResult = Result.success(Unit)

        vm.markAsRead(10)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.notifications.first().isRead)
        assertEquals(4, state.unreadCount)
        assertEquals(4, state.carrierUnreadCount)
        assertEquals(2, state.shipperUnreadCount)
        assertEquals(listOf(10), fakeRepo.markAsReadCalls)
    }

    @Test
    fun `markAsRead is a no-op when already read`() = runTest {
        val read = notif(11, NotificationType.MATCH_REQUEST).copy(isRead = true)
        fakeRepo.pageResult = Result.success(NotificationPage(listOf(read), null))
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        vm.markAsRead(11)
        advanceUntilIdle()
        assertTrue(fakeRepo.markAsReadCalls.isEmpty())
    }

    @Test
    fun `markAsRead reloads role counts on API failure`() = runTest {
        val n = notif(12, NotificationType.CHAT_MESSAGE, recipientRole = "shipper")
        fakeRepo.pageResult = Result.success(NotificationPage(listOf(n), null))
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        fakeRepo.markAsReadResult = Result.failure(RuntimeException("boom"))
        fakeRepo.totalUnread = 9
        fakeRepo.shipperUnread = 6
        vm.markAsRead(12)
        advanceUntilIdle()
        // Re-sync called after failure
        assertTrue(fakeRepo.unreadCountCalls.isNotEmpty())
        // Server-truth values applied
        assertEquals(9, vm.uiState.value.unreadCount)
        assertEquals(6, vm.uiState.value.shipperUnreadCount)
    }

    // -- markAllAsRead --

    @Test
    fun `markAllAsRead zeroes all counts and marks every notification read`() = runTest {
        val list = listOf(
            notif(20, NotificationType.MATCH_REQUEST, recipientRole = "carrier"),
            notif(21, NotificationType.CHAT_MESSAGE, recipientRole = "shipper"),
        )
        fakeRepo.pageResult = Result.success(NotificationPage(list, null))
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        fakeRepo.markAllAsReadResult = Result.success(2)

        vm.markAllAsRead()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.notifications.all { it.isRead })
        assertEquals(0, state.unreadCount)
        assertEquals(0, state.carrierUnreadCount)
        assertEquals(0, state.shipperUnreadCount)
    }

    // -- onNotificationTapped --

    @Test
    fun `onNotificationTapped routes via singleton router and marks read`() = runTest {
        val n = notif(30, NotificationType.MATCH_REQUEST).copy(
            data = NotificationData(matchId = 100),
        )
        fakeRepo.pageResult = Result.success(NotificationPage(listOf(n), null))
        val vm = viewModel()
        vm.loadNotifications()
        advanceUntilIdle()
        fakeRepo.markAsReadResult = Result.success(Unit)

        val collector = async(start = CoroutineStart.UNDISPATCHED) {
            router.events.first()
        }
        vm.onNotificationTapped(n)
        advanceUntilIdle()

        // Local state should reflect read after tap
        assertTrue(vm.uiState.value.notifications.first().isRead)
        assertEquals(listOf(30), fakeRepo.markAsReadCalls)
        collector.cancel()
    }

    // -- sendTestNotification --

    @Test
    fun `sendTestNotification sets SENT on success`() = runTest {
        fakeRepo.testResult = Result.success(Unit)
        val vm = viewModel()
        vm.sendTestNotification()
        advanceUntilIdle()
        assertEquals(TestNotificationStatus.SENT, vm.uiState.value.testNotificationStatus)
    }

    @Test
    fun `sendTestNotification sets FAILED on failure`() = runTest {
        fakeRepo.testResult = Result.failure(RuntimeException("nope"))
        val vm = viewModel()
        vm.sendTestNotification()
        advanceUntilIdle()
        assertEquals(TestNotificationStatus.FAILED, vm.uiState.value.testNotificationStatus)
    }

    // -- Helpers --

    private fun notif(
        id: Int,
        type: NotificationType,
        recipientRole: String? = null,
    ) = PushNotification(
        id = id,
        title = "t",
        body = "b",
        type = type,
        recipientRole = recipientRole,
        sentAt = "x",
        createdAt = "x",
    )
}

private class FakeRepo : NotificationRepository {
    var pageResult: Result<NotificationPage> = Result.failure(IllegalStateException("not set"))
    var markAsReadResult: Result<Unit> = Result.success(Unit)
    var markAllAsReadResult: Result<Int> = Result.success(0)
    var testResult: Result<Unit> = Result.success(Unit)
    val markAsReadCalls = mutableListOf<Int>()
    val unreadCountCalls = mutableListOf<String?>()

    var totalUnread: Int = 0
    var carrierUnread: Int = 0
    var shipperUnread: Int = 0

    override suspend fun registerDeviceToken(fcmToken: String): DeviceTokenRegisterResult =
        DeviceTokenRegisterResult.ServerError("not used")

    override suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit> = Result.success(Unit)

    override suspend fun fetchNotifications(page: Int, unreadOnly: Boolean?, role: String?) =
        pageResult

    override suspend fun getUnreadCount(role: String?): Result<Int> {
        unreadCountCalls += role
        return Result.success(
            when (role) {
                "carrier" -> carrierUnread
                "shipper" -> shipperUnread
                else -> totalUnread
            },
        )
    }

    override suspend fun markAsRead(id: Int): Result<Unit> {
        markAsReadCalls += id
        return markAsReadResult
    }

    override suspend fun markAllAsRead(): Result<Int> = markAllAsReadResult
    override suspend fun sendTestNotification(): Result<Unit> = testResult
}
