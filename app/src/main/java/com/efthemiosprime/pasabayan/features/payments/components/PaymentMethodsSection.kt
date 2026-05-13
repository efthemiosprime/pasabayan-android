package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsUiState

/**
 * Profile-hub summary section. Shows up to 3 saved cards via [PaymentMethodCard]
 * and links to the full [com.efthemiosprime.pasabayan.features.payments.ui.PaymentMethodsScreen]
 * for management.
 */
@Composable
fun PaymentMethodsSection(
    methodsState: PaymentMethodsUiState,
    onSetDefaultMethod: (String) -> Unit,
    onRemoveMethod: (String) -> Unit,
    onManagePaymentMethods: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_methods_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            if (methodsState.paymentMethods.isEmpty()) {
                Text(
                    text = stringResource(R.string.payments_profile_methods_empty),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                methodsState.paymentMethods.take(3).forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        onSetDefault = { onSetDefaultMethod(method.id) },
                        onRemove = { onRemoveMethod(method.id) },
                    )
                }
            }
            PButton(
                text = stringResource(R.string.payments_profile_methods_manage),
                onClick = onManagePaymentMethods,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentMethods light")
@Preview(showBackground = true, name = "PaymentMethods dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentMethodsSectionPreview() {
    PasabayanTheme {
        PaymentMethodsSection(
            methodsState = PaymentMethodsUiState(
                paymentMethods = listOf(
                    PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, true),
                    PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
                ),
            ),
            onSetDefaultMethod = {},
            onRemoveMethod = {},
            onManagePaymentMethods = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentMethods empty")
@Composable
private fun PaymentMethodsSectionEmptyPreview() {
    PasabayanTheme {
        PaymentMethodsSection(
            methodsState = PaymentMethodsUiState(),
            onSetDefaultMethod = {},
            onRemoveMethod = {},
            onManagePaymentMethods = {},
        )
    }
}
