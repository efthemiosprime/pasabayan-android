package com.efthemiosprime.pasabayan.core.domain.`enum`

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefundReasonTest {

    @Test
    fun `displayText values are stable`() {
        assertEquals("Package was damaged during delivery", RefundReason.DAMAGED.displayText)
        assertEquals("Package was never delivered", RefundReason.NOT_DELIVERED.displayText)
        assertEquals("Wrong item was delivered", RefundReason.WRONG_ITEM.displayText)
        assertEquals("Delivery was significantly late", RefundReason.LATE_DELIVERY.displayText)
        assertEquals("Only partial items were delivered", RefundReason.PARTIAL_DELIVERY.displayText)
        assertEquals("Other", RefundReason.OTHER.displayText)
    }

    @Test
    fun `requiresCustomInput is true only for OTHER`() {
        assertFalse(RefundReason.DAMAGED.requiresCustomInput)
        assertFalse(RefundReason.NOT_DELIVERED.requiresCustomInput)
        assertFalse(RefundReason.WRONG_ITEM.requiresCustomInput)
        assertFalse(RefundReason.LATE_DELIVERY.requiresCustomInput)
        assertFalse(RefundReason.PARTIAL_DELIVERY.requiresCustomInput)
        assertTrue(RefundReason.OTHER.requiresCustomInput)
    }
}
