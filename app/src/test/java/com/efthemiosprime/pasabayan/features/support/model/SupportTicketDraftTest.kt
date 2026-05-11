package com.efthemiosprime.pasabayan.features.support.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportTicketDraftTest {

    private val validDraft = SupportTicketDraft(
        category = SupportCategory.DELIVERY_ISSUES,
        subject = "Carrier never arrived",
        email = "user@example.com",
        priority = SupportPriority.HIGH,
        description = "I scheduled pickup at 2pm but the carrier did not show up.",
    )

    @Test
    fun `isSubmittable is true for a fully valid draft`() {
        assertTrue(validDraft.isSubmittable)
    }

    @Test
    fun `isSubmittable is false without category`() {
        assertFalse(validDraft.copy(category = null).isSubmittable)
    }

    @Test
    fun `isSubmittable is false when subject too short`() {
        assertFalse(validDraft.copy(subject = "Hi").isSubmittable)
    }

    @Test
    fun `isSubmittable is false when email missing at sign`() {
        assertFalse(validDraft.copy(email = "userexample.com").isSubmittable)
    }

    @Test
    fun `isSubmittable is false when email missing dot`() {
        assertFalse(validDraft.copy(email = "user@example").isSubmittable)
    }

    @Test
    fun `isSubmittable is false when description too short`() {
        assertFalse(validDraft.copy(description = "short").isSubmittable)
    }

    @Test
    fun `MAX_ATTACHMENTS guards the chip list size`() {
        // sanity check that the upper bound matches what the UI uses
        assertEquals(5, SupportTicketDraft.MAX_ATTACHMENTS)
    }

    // Category lookup helpers

    @Test
    fun `SupportCategory fromRaw resolves canonical aliases`() {
        assertEquals(SupportCategory.ACCOUNT_PROFILE, SupportCategory.fromRaw("account_profile"))
        assertEquals(SupportCategory.OTHER, SupportCategory.fromRaw("other"))
        assertNull(SupportCategory.fromRaw("unknown_category"))
    }

    @Test
    fun `SupportPriority fromRaw resolves canonical aliases`() {
        assertEquals(SupportPriority.LOW, SupportPriority.fromRaw("low"))
        assertEquals(SupportPriority.HIGH, SupportPriority.fromRaw("high"))
        assertNull(SupportPriority.fromRaw("urgent"))
    }
}
