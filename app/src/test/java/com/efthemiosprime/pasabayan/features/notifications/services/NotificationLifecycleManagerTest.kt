package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import com.efthemiosprime.pasabayan.core.network.notifications.DeviceTokenInfoJson
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationPage
import kotlinx.coroutines.CoroutineScope
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationLifecycleManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeRepo
    private lateinit var fakeStore: FakeTokenStore
    private lateinit var authProvider: ToggleableAuthTokenProvider
    private lateinit var appScope: NotificationAppScope
    private lateinit var manager: DefaultNotificationLifecycleManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeRepo()
        fakeStore = FakeTokenStore()
        authProvider = ToggleableAuthTokenProvider()
        appScope = object : NotificationAppScope {
            override val scope: CoroutineScope = CoroutineScope(testDispatcher)
        }
        manager = DefaultNotificationLifecycleManager(
            repository = fakeRepo,
            tokenStore = fakeStore,
            authTokenProvider = authProvider,
            appScope = appScope,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onTokenRefreshed stores token even when unauthenticated`() = runTest {
        authProvider.token = null
        manager.onTokenRefreshed("fcm_abc")
        advanceUntilIdle()
        assertEquals("fcm_abc", fakeStore.getToken())
        // No registration call fired
        assertEquals(0, fakeRepo.registerCalls.size)
    }

    @Test
    fun `onTokenRefreshed registers immediately when authenticated`() = runTest {
        authProvider.token = "bearer_xyz"
        fakeRepo.registerResult = DeviceTokenRegisterResult.Registered(
            DeviceTokenInfoJson(id = 1, platform = "android", isActive = true, createdAt = "x"),
        )
        manager.onTokenRefreshed("fcm_abc")
        advanceUntilIdle()
        assertEquals(listOf("fcm_abc"), fakeRepo.registerCalls)
    }

    @Test
    fun `registerStoredTokenIfAuthenticated is no-op without token`() = runTest {
        authProvider.token = "bearer_xyz"
        fakeStore.setToken(null)
        val result = manager.registerStoredTokenIfAuthenticated()
        assertNull(result)
        assertEquals(0, fakeRepo.registerCalls.size)
    }

    @Test
    fun `registerStoredTokenIfAuthenticated is no-op when unauthenticated`() = runTest {
        authProvider.token = null
        fakeStore.setToken("fcm_abc")
        val result = manager.registerStoredTokenIfAuthenticated()
        assertNull(result)
        assertEquals(0, fakeRepo.registerCalls.size)
    }

    @Test
    fun `registerStoredTokenIfAuthenticated returns registration result`() = runTest {
        authProvider.token = "bearer_xyz"
        fakeStore.setToken("fcm_abc")
        fakeRepo.registerResult = DeviceTokenRegisterResult.Registered(
            DeviceTokenInfoJson(id = 7, platform = "android", isActive = true, createdAt = "x"),
        )
        val result = manager.registerStoredTokenIfAuthenticated()
        assertTrue(result is DeviceTokenRegisterResult.Registered)
        assertEquals(listOf("fcm_abc"), fakeRepo.registerCalls)
    }

    @Test
    fun `unregisterAndClear clears store on success`() = runTest {
        fakeStore.setToken("fcm_abc")
        fakeRepo.unregisterResult = Result.success(Unit)
        val result = manager.unregisterAndClear()
        assertTrue(result.isSuccess)
        assertNull(fakeStore.getToken())
        assertEquals(listOf("fcm_abc"), fakeRepo.unregisterCalls)
    }

    @Test
    fun `unregisterAndClear still clears store on failure`() = runTest {
        fakeStore.setToken("fcm_abc")
        fakeRepo.unregisterResult = Result.failure(RuntimeException("offline"))
        val result = manager.unregisterAndClear()
        assertTrue(result.isFailure)
        assertNull(
            "store should be cleared so we don't retry a stale token on next launch",
            fakeStore.getToken(),
        )
    }

    @Test
    fun `unregisterAndClear is no-op when no token stored`() = runTest {
        fakeStore.setToken(null)
        val result = manager.unregisterAndClear()
        assertTrue(result.isSuccess)
        assertFalse(fakeRepo.unregisterCalls.isNotEmpty())
    }
}

private class ToggleableAuthTokenProvider : AuthTokenProvider {
    var token: String? = null
    override fun currentToken(): String? = token
}

private class FakeTokenStore : FCMTokenStore {
    private var current: String? = null
    override fun getToken(): String? = current
    override fun setToken(token: String?) { current = token }
    override fun clear() { current = null }
}

private class FakeRepo : NotificationRepository {
    val registerCalls = mutableListOf<String>()
    val unregisterCalls = mutableListOf<String>()
    var registerResult: DeviceTokenRegisterResult =
        DeviceTokenRegisterResult.ServerError("not set")
    var unregisterResult: Result<Unit> = Result.failure(IllegalStateException("not set"))

    override suspend fun registerDeviceToken(fcmToken: String): DeviceTokenRegisterResult {
        registerCalls += fcmToken
        return registerResult
    }

    override suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit> {
        unregisterCalls += fcmToken
        return unregisterResult
    }

    override suspend fun fetchNotifications(page: Int, unreadOnly: Boolean?, role: String?) =
        Result.failure<NotificationPage>(IllegalStateException("not used"))
    override suspend fun getUnreadCount(role: String?) = Result.failure<Int>(IllegalStateException("not used"))
    override suspend fun markAsRead(id: Int) = Result.failure<Unit>(IllegalStateException("not used"))
    override suspend fun markAllAsRead() = Result.failure<Int>(IllegalStateException("not used"))
    override suspend fun sendTestNotification() = Result.failure<Unit>(IllegalStateException("not used"))
}
