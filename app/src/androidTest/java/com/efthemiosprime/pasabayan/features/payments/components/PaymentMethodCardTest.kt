package com.efthemiosprime.pasabayan.features.payments.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentMethodCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun defaultBadge_showsForDefaultCard() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodCard(
                    method = PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, isDefault = true),
                    onSetDefault = {},
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_methods_default_badge)).assertIsDisplayed()
        composeRule.onNodeWithText("Visa •••• 4242").assertIsDisplayed()
    }

    @Test
    fun defaultBadge_hiddenForNonDefaultCard() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodCard(
                    method = PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, isDefault = false),
                    onSetDefault = {},
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_methods_default_badge))
            .assertDoesNotExist()
    }

    @Test
    fun overflowMenu_opensAndShowsBothItems_forNonDefaultCard() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodCard(
                    method = PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, isDefault = false),
                    onSetDefault = {},
                    onRemove = {},
                )
            }
        }
        composeRule
            .onNodeWithContentDescription(string(R.string.payments_methods_more_options))
            .performClick()

        composeRule.onNodeWithText(string(R.string.payments_methods_set_default)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.payments_methods_remove)).assertIsDisplayed()
    }

    @Test
    fun overflowMenu_hidesSetDefault_forDefaultCard() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodCard(
                    method = PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, isDefault = true),
                    onSetDefault = {},
                    onRemove = {},
                )
            }
        }
        composeRule
            .onNodeWithContentDescription(string(R.string.payments_methods_more_options))
            .performClick()
        composeRule.onNodeWithText(string(R.string.payments_methods_set_default)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.payments_methods_remove)).assertIsDisplayed()
    }

    @Test
    fun removeMenuItem_opensConfirmDialog() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodCard(
                    method = PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, isDefault = false),
                    onSetDefault = {},
                    onRemove = {},
                )
            }
        }
        composeRule
            .onNodeWithContentDescription(string(R.string.payments_methods_more_options))
            .performClick()
        composeRule.onNodeWithText(string(R.string.payments_methods_remove)).performClick()

        composeRule
            .onNodeWithText(string(R.string.payments_methods_remove_confirm_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(string(R.string.payments_methods_remove_confirm_cancel))
            .assertIsDisplayed()
    }
}
