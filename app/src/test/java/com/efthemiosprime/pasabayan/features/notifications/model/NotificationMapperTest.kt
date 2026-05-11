package com.efthemiosprime.pasabayan.features.notifications.model

import com.efthemiosprime.pasabayan.core.network.notifications.NotificationDataJson
import com.efthemiosprime.pasabayan.core.network.notifications.PushNotificationJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationMapperTest {

    @Test
    fun `maps DTO to domain with known type`() {
        val dto = PushNotificationJson(
            id = 1,
            title = "New Request",
            body = "Alice wants to ship a package",
            type = "match_request",
            data = NotificationDataJson(matchId = 42, requesterName = "Alice"),
            recipientRole = "carrier",
            sentAt = "2026-04-01T10:00:00Z",
            isRead = false,
            createdAt = "2026-04-01T10:00:00Z",
        )
        val domain = NotificationMapper.toDomain(dto)
        assertEquals(1, domain.id)
        assertEquals(NotificationType.MATCH_REQUEST, domain.type)
        assertEquals(42, domain.data?.matchId)
        assertEquals("Alice", domain.data?.requesterName)
        assertFalse(domain.isRead)
    }

    @Test
    fun `maps unknown type to UNKNOWN`() {
        val dto = PushNotificationJson(
            id = 2,
            title = "x",
            body = "y",
            type = "future_unknown_type",
            sentAt = "x",
            createdAt = "y",
        )
        val domain = NotificationMapper.toDomain(dto)
        assertEquals(NotificationType.UNKNOWN, domain.type)
    }

    @Test
    fun `maps null data to null`() {
        val dto = PushNotificationJson(
            id = 3,
            title = "x",
            body = "y",
            type = "chat_message",
            sentAt = "x",
            createdAt = "y",
        )
        val domain = NotificationMapper.toDomain(dto)
        assertNull(domain.data)
    }

    @Test
    fun `maps full counter-offer data payload`() {
        val dto = NotificationDataJson(
            matchId = 200,
            newPrice = 75.5,
            originalPrice = 60.0,
            priceDifference = 15.5,
            direction = "up",
            counterOffererName = "Bob",
            counterOffererId = 9,
            counterOfferRound = 2,
            initiatedBy = "shipper",
            remainingCounterOffers = 1,
            canCounterOffer = true,
        )
        val data = NotificationMapper.toDomain(dto)
        assertEquals(200, data.matchId)
        assertEquals(75.5, data.newPrice!!, 0.001)
        assertEquals("up", data.direction)
        assertEquals(2, data.counterOfferRound)
        assertEquals(true, data.canCounterOffer)
        assertNotNull(data.counterOffererName)
    }
}
