package com.efthemiosprime.pasabayan.core.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class PasabayanTokensTest {

    @Test
    fun spacing_semantic_aliases_match_scale() {
        assertEquals(PasabayanSpacing.cardPadding, PasabayanSpacing.lg)
        assertEquals(PasabayanSpacing.sectionSpacing, PasabayanSpacing.xxl)
        assertEquals(PasabayanSpacing.screenPadding, PasabayanSpacing.lg)
        assertEquals(PasabayanSpacing.buttonPadding, PasabayanSpacing.md)
        assertEquals(PasabayanSpacing.itemSpacing, PasabayanSpacing.sm)
        assertEquals(PasabayanSpacing.formFieldSpacing, PasabayanSpacing.xl)
    }

    @Test
    fun layout_common_sizes_match_spec() {
        assertEquals(16f, PasabayanLayout.iconSizeSmall.value, 0f)
        assertEquals(48f, PasabayanLayout.buttonHeightMedium.value, 0f)
    }

    @Test
    fun motion_durations_are_documented() {
        assertEquals(200, PasabayanMotion.FAST_MS)
        assertEquals(300, PasabayanMotion.MEDIUM_MS)
        assertEquals(500, PasabayanMotion.SLOW_MS)
    }

    @Test
    fun border_width_is_one_dp() {
        assertEquals(1f, PasabayanBorder.width.value, 0f)
    }
}
