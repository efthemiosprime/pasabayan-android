package com.efthemiosprime.pasabayan.features.payments.components

import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PayoutStatusBadgeConfigTest {

    @Test
    fun `each PayoutStatus maps to a Pasabayan colour token`() {
        val expected = mapOf(
            PayoutStatus.PENDING to PasabayanColors.BadgeGray,
            PayoutStatus.PROCESSING to PasabayanColors.Warning,
            PayoutStatus.ON_HOLD to PasabayanColors.BadgeAmber,
            PayoutStatus.SCHEDULED to PasabayanColors.Info,
            PayoutStatus.COMPLETED to PasabayanColors.Success,
            PayoutStatus.FAILED to PasabayanColors.Error,
        )
        expected.forEach { (status, colour) ->
            val config = PayoutStatusBadgeConfig(status, label = "label")
            assertEquals("backgroundColor for $status", colour, config.backgroundColor)
            assertEquals("textColor for $status", colour, config.textColor)
        }
    }

    @Test
    fun `displayText returns the supplied label verbatim`() {
        val config = PayoutStatusBadgeConfig(PayoutStatus.COMPLETED, label = "Custom label")
        assertEquals("Custom label", config.displayText)
    }

    @Test
    fun `every status case carries an icon`() {
        PayoutStatus.entries.forEach { status ->
            val config = PayoutStatusBadgeConfig(status, label = "x")
            assertNotNull("icon for $status", config.icon)
        }
    }
}
