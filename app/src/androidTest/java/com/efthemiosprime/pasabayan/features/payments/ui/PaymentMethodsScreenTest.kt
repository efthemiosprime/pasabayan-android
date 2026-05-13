package com.efthemiosprime.pasabayan.features.payments.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentMethodsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun listState_rendersEveryCard() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodsContent(
                    state = PaymentMethodsUiState(
                        paymentMethods = listOf(
                            PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, isDefault = true),
                            PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, isDefault = false),
                        ),
                    ),
                    onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
                    onViewTransactions = {}, onRetry = {},
                )
            }
        }
        composeRule.onNodeWithText("Visa •••• 4242").assertIsDisplayed()
        composeRule.onNodeWithText("Mastercard •••• 5555").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyPromptAndAddCta() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodsContent(
                    state = PaymentMethodsUiState(),
                    onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
                    onViewTransactions = {}, onRetry = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_methods_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.payments_methods_add_card)).assertIsDisplayed()
    }

    @Test
    fun errorState_showsRetryButton() {
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodsContent(
                    state = PaymentMethodsUiState(errorMessage = "Network failed"),
                    onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
                    onViewTransactions = {}, onRetry = {},
                )
            }
        }
        composeRule.onNodeWithText("Network failed").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.payments_methods_retry)).assertIsDisplayed()
    }

    @Test
    fun addCardButton_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            PasabayanTheme {
                PaymentMethodsContent(
                    state = PaymentMethodsUiState(),
                    onBack = {}, onAddCard = { clicked = true }, onSetDefault = {}, onRemove = {},
                    onViewTransactions = {}, onRetry = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.payments_methods_add_card)).performClick()
        assert(clicked) { "onAddCard should have fired" }
    }
}
