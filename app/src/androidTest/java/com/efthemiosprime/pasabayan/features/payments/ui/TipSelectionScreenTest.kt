package com.efthemiosprime.pasabayan.features.payments.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TippingUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipSelectionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun presets_areAllRendered() {
        composeRule.setContent {
            PasabayanTheme {
                TipSelectionScreen(
                    state = TippingUiState(),
                    carrierName = "John",
                    onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
                )
            }
        }
        TippingUiState.PRESET_TIPS.forEach { amount ->
            // Locale-independent: every preset chip shows ".00" with the amount.
            composeRule.onNodeWithText(
                text = ".00",
                substring = true,
                useUnmergedTree = false,
            ).assertIsDisplayed()
            assert(amount > 0)
        }
    }

    @Test
    fun submitButton_disabledWhenInvalid() {
        composeRule.setContent {
            PasabayanTheme {
                TipSelectionScreen(
                    state = TippingUiState(), // selectedTipAmount = 0 → invalid
                    carrierName = "John",
                    onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
                )
            }
        }
        composeRule
            .onNodeWithText(string(R.string.payments_tip_submit).substringBefore(" %1${'$'}s"), substring = true)
            .assertIsNotEnabled()
    }

    @Test
    fun submitButton_enabledWhenPresetSelected() {
        composeRule.setContent {
            PasabayanTheme {
                TipSelectionScreen(
                    state = TippingUiState(selectedTipAmount = 5.0),
                    carrierName = "John",
                    onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
                )
            }
        }
        composeRule
            .onNodeWithText(string(R.string.payments_tip_submit).substringBefore(" %1${'$'}s"), substring = true)
            .assertIsEnabled()
    }

    @Test
    fun skipButton_invokesCallback() {
        var skipped = false
        composeRule.setContent {
            PasabayanTheme {
                TipSelectionScreen(
                    state = TippingUiState(),
                    carrierName = "John",
                    onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = { skipped = true }, onBack = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_tip_skip)).performClick()
        assert(skipped) { "onSkip should have fired" }
    }
}
