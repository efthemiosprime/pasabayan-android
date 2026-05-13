package com.efthemiosprime.pasabayan.features.payments.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.features.payments.viewmodel.RefundUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefundSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun reasonChips_rendered() {
        composeRule.setContent {
            PasabayanTheme {
                RefundSheetContent(
                    state = RefundUiState(),
                    transactionId = 500,
                    transactionTotal = "$165.00 CAD",
                    onSelectReason = {}, onCustomReasonChange = {},
                    onAdditionalDetailsChange = {}, onPartialToggle = {},
                    onPartialAmountChange = {}, onSubmit = {},
                )
            }
        }
        // At least the first preset is rendered in the chip row.
        composeRule.onNodeWithText(RefundReason.DAMAGED.displayText).assertIsDisplayed()
    }

    @Test
    fun submit_disabledWhenNoReasonSelected() {
        composeRule.setContent {
            PasabayanTheme {
                RefundSheetContent(
                    state = RefundUiState(),
                    transactionId = 500,
                    transactionTotal = "$165.00 CAD",
                    onSelectReason = {}, onCustomReasonChange = {},
                    onAdditionalDetailsChange = {}, onPartialToggle = {},
                    onPartialAmountChange = {}, onSubmit = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_refund_submit)).assertIsNotEnabled()
    }

    @Test
    fun submit_enabledWhenPresetReasonSelected() {
        composeRule.setContent {
            PasabayanTheme {
                RefundSheetContent(
                    state = RefundUiState(selectedReason = RefundReason.DAMAGED),
                    transactionId = 500,
                    transactionTotal = "$165.00 CAD",
                    onSelectReason = {}, onCustomReasonChange = {},
                    onAdditionalDetailsChange = {}, onPartialToggle = {},
                    onPartialAmountChange = {}, onSubmit = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_refund_submit)).assertIsEnabled()
    }

    @Test
    fun submit_disabledForShortCustomReason() {
        composeRule.setContent {
            PasabayanTheme {
                RefundSheetContent(
                    state = RefundUiState(
                        selectedReason = RefundReason.OTHER,
                        customReason = "short", // < 10 chars
                    ),
                    transactionId = 500,
                    transactionTotal = "$165.00 CAD",
                    onSelectReason = {}, onCustomReasonChange = {},
                    onAdditionalDetailsChange = {}, onPartialToggle = {},
                    onPartialAmountChange = {}, onSubmit = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_refund_submit)).assertIsNotEnabled()
    }

    @Test
    fun partialToggle_revealsAmountField() {
        composeRule.setContent {
            PasabayanTheme {
                RefundSheetContent(
                    state = RefundUiState(
                        isPartialRefund = true,
                        partialAmount = "25.00",
                        selectedReason = RefundReason.DAMAGED,
                    ),
                    transactionId = 500,
                    transactionTotal = "$165.00 CAD",
                    onSelectReason = {}, onCustomReasonChange = {},
                    onAdditionalDetailsChange = {}, onPartialToggle = {},
                    onPartialAmountChange = {}, onSubmit = {},
                )
            }
        }
        composeRule
            .onNodeWithText(string(R.string.payments_refund_partial_amount_label))
            .assertIsDisplayed()
    }
}
