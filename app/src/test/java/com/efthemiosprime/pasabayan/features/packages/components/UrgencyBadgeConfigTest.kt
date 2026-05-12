package com.efthemiosprime.pasabayan.features.packages.components

import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrgencyBadgeConfigTest {

    @Test
    fun `displayText includes urgency icon and label`() {
        val config = UrgencyBadgeConfig(UrgencyLevel.NORMAL, "Normal")
        assertEquals("${UrgencyLevel.NORMAL.icon} Normal", config.displayText)
    }

    @Test
    fun `background color maps to expected intensity token for each level`() {
        val cases = mapOf(
            UrgencyLevel.LOW to PasabayanColors.BadgeGray,
            UrgencyLevel.NORMAL to PasabayanColors.BadgeBlue,
            UrgencyLevel.HIGH to PasabayanColors.BadgeAmber,
            UrgencyLevel.URGENT to PasabayanColors.Error,
            UrgencyLevel.EXPRESS to PasabayanColors.BadgePurple,
            UrgencyLevel.FLEXIBLE to PasabayanColors.BadgeTeal,
        )
        cases.forEach { (level, expected) ->
            val config = UrgencyBadgeConfig(level, level.name)
            assertEquals("level=$level", expected, config.backgroundColor)
        }
    }

    @Test
    fun `textColor mirrors backgroundColor for tonal contrast in compact variant`() {
        UrgencyLevel.values().forEach { level ->
            val config = UrgencyBadgeConfig(level, level.name)
            assertEquals(config.backgroundColor, config.textColor)
        }
    }

    @Test
    fun `every UrgencyLevel resolves to a non-null color (no missing branches)`() {
        UrgencyLevel.values().forEach { level ->
            val config = UrgencyBadgeConfig(level, level.name)
            assertTrue("level=$level missing color", config.backgroundColor.alpha > 0f)
        }
    }
}
