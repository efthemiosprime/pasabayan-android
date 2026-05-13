package com.efthemiosprime.pasabayan.features.payments.components

import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RefundStatusBadgeConfigTest {

    @Test
    fun `each RefundStatus maps to a Pasabayan colour token`() {
        val expected = mapOf(
            RefundStatus.PENDING to PasabayanColors.Warning,
            RefundStatus.APPROVED to PasabayanColors.Info,
            RefundStatus.REJECTED to PasabayanColors.Error,
            RefundStatus.PROCESSED to PasabayanColors.Success,
        )
        expected.forEach { (status, colour) ->
            val config = RefundStatusBadgeConfig(status, label = "label")
            assertEquals("backgroundColor for $status", colour, config.backgroundColor)
            assertEquals("textColor for $status", colour, config.textColor)
        }
    }

    @Test
    fun `displayText returns the supplied label verbatim`() {
        val config = RefundStatusBadgeConfig(RefundStatus.APPROVED, label = "Custom label")
        assertEquals("Custom label", config.displayText)
    }

    @Test
    fun `every status case carries an icon`() {
        RefundStatus.entries.forEach { status ->
            val config = RefundStatusBadgeConfig(status, label = "x")
            assertNotNull("icon for $status", config.icon)
        }
    }
}
