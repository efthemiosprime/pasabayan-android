package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationRoutingTest {

    @Test
    fun `match types resolve to OpenMatch with match id`() {
        for (type in listOf(
            NotificationType.MATCH_REQUEST,
            NotificationType.MATCH_ACCEPTED,
            NotificationType.MATCH_DECLINED,
            NotificationType.MATCH_AUTO_CANCELLED,
        )) {
            val event = NotificationRouting.resolve(type, NotificationData(matchId = 42))
            assertEquals("$type should map to OpenMatch", NavigationEvent.OpenMatch(42), event)
        }
    }

    @Test
    fun `match types without match id return null`() {
        val event = NotificationRouting.resolve(NotificationType.MATCH_REQUEST, NotificationData())
        assertNull(event)
    }

    @Test
    fun `delivery types fall back to OpenMatchesTab when match id missing`() {
        for (type in listOf(
            NotificationType.DELIVERY_PICKED_UP,
            NotificationType.DELIVERY_IN_TRANSIT,
            NotificationType.DELIVERY_DELIVERED,
        )) {
            val event = NotificationRouting.resolve(type, NotificationData())
            assertEquals(NavigationEvent.OpenMatchesTab, event)
        }
    }

    @Test
    fun `delivery types prefer OpenMatch when match id present`() {
        val event = NotificationRouting.resolve(
            NotificationType.DELIVERY_PICKED_UP,
            NotificationData(matchId = 11),
        )
        assertEquals(NavigationEvent.OpenMatch(11), event)
    }

    @Test
    fun `chat falls back to conversations list without id`() {
        val event = NotificationRouting.resolve(NotificationType.CHAT_MESSAGE, NotificationData())
        assertEquals(NavigationEvent.OpenConversations, event)
    }

    @Test
    fun `chat with conversation id opens specific thread`() {
        val event = NotificationRouting.resolve(
            NotificationType.CHAT_MESSAGE,
            NotificationData(conversationId = 99),
        )
        assertEquals(NavigationEvent.OpenConversation(99), event)
    }

    @Test
    fun `payout payment and refund types open transactions`() {
        for (type in listOf(
            NotificationType.PAYMENT_RECEIVED,
            NotificationType.PAYMENT_RELEASED,
            NotificationType.PAYOUT_COMPLETED,
            NotificationType.PAYOUT_SCHEDULED,
            NotificationType.REFUND_PROCESSED,
        )) {
            assertEquals(
                "$type should open transactions",
                NavigationEvent.OpenTransactions,
                NotificationRouting.resolve(type, null),
            )
        }
    }

    @Test
    fun `tip received prefers transaction detail`() {
        val event = NotificationRouting.resolve(
            NotificationType.TIP_RECEIVED,
            NotificationData(transactionId = 555),
        )
        assertEquals(NavigationEvent.OpenTransactionDetail(555), event)
    }

    @Test
    fun `tip received falls back to transactions without id`() {
        val event = NotificationRouting.resolve(NotificationType.TIP_RECEIVED, NotificationData())
        assertEquals(NavigationEvent.OpenTransactions, event)
    }

    @Test
    fun `payment auto charge failed opens payment methods`() {
        val event = NotificationRouting.resolve(
            NotificationType.PAYMENT_AUTO_CHARGE_FAILED,
            null,
        )
        assertEquals(NavigationEvent.OpenPaymentMethods, event)
    }

    @Test
    fun `rating received opens ratings`() {
        assertEquals(
            NavigationEvent.OpenRatings,
            NotificationRouting.resolve(NotificationType.RATING_RECEIVED, null),
        )
    }

    @Test
    fun `premium approved and rejected open profile verification`() {
        assertEquals(
            NavigationEvent.OpenProfileVerification,
            NotificationRouting.resolve(NotificationType.PREMIUM_APPROVED, null),
        )
        assertEquals(
            NavigationEvent.OpenProfileVerification,
            NotificationRouting.resolve(NotificationType.PREMIUM_REJECTED, null),
        )
    }

    @Test
    fun `counter offer with match id opens counter-offer flow`() {
        val event = NotificationRouting.resolve(
            NotificationType.COUNTER_OFFER,
            NotificationData(matchId = 7),
        )
        assertEquals(NavigationEvent.OpenCounterOffer(7), event)
    }

    @Test
    fun `counter offer without match id returns null`() {
        assertNull(NotificationRouting.resolve(NotificationType.COUNTER_OFFER, NotificationData()))
    }

    @Test
    fun `test and UNKNOWN types return null`() {
        assertNull(NotificationRouting.resolve(NotificationType.TEST, null))
        assertNull(NotificationRouting.resolve(NotificationType.UNKNOWN, null))
    }
}
