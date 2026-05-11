package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotificationRoute
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRouterTest {

    @Test
    fun `emitFromPush emits resolved event`() = runTest {
        val router = NotificationRouter()
        val route = PushNotificationRoute(
            type = NotificationType.MATCH_REQUEST,
            data = NotificationData(matchId = 5),
        )
        val collector = async(start = CoroutineStart.UNDISPATCHED) {
            router.events.first()
        }
        val accepted = router.emitFromPush(route)
        assertTrue(accepted)
        assertEquals(NavigationEvent.OpenMatch(5), collector.await())
    }

    @Test
    fun `emitFromInApp emits resolved event`() = runTest {
        val router = NotificationRouter()
        val notif = PushNotification(
            id = 1,
            title = "x",
            body = "y",
            type = NotificationType.CHAT_MESSAGE,
            data = NotificationData(conversationId = 99),
            sentAt = "x",
            createdAt = "x",
        )
        val collector = async(start = CoroutineStart.UNDISPATCHED) {
            router.events.first()
        }
        router.emitFromInApp(notif)
        assertEquals(NavigationEvent.OpenConversation(99), collector.await())
    }

    @Test
    fun `emitFromPush returns false for non-actionable type`() {
        val router = NotificationRouter()
        val route = PushNotificationRoute(type = NotificationType.TEST)
        assertFalse(router.emitFromPush(route))
    }

    @Test
    fun `both push and in-app emissions reach single observer`() = runTest {
        val router = NotificationRouter()
        val collector = async(start = CoroutineStart.UNDISPATCHED) {
            router.events.take(2).toList()
        }
        router.emitFromPush(
            PushNotificationRoute(
                type = NotificationType.MATCH_REQUEST,
                data = NotificationData(matchId = 1),
            ),
        )
        router.emitFromInApp(
            PushNotification(
                id = 2, title = "x", body = "y",
                type = NotificationType.CHAT_MESSAGE,
                data = NotificationData(conversationId = 7),
                sentAt = "x", createdAt = "x",
            ),
        )
        val events = collector.await()
        assertEquals(
            listOf(NavigationEvent.OpenMatch(1), NavigationEvent.OpenConversation(7)),
            events,
        )
    }
}
