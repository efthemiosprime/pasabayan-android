package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsUiState

@Composable
fun PaymentMethodsSection(
    methodsState: PaymentMethodsUiState,
    onSetDefaultMethod: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_methods_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            methodsState.paymentMethods.take(3).forEach { method ->
                Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = method.displayName,
                        modifier = Modifier.weight(1f),
                        style = PasabayanTextStyles.Body.medium,
                    )
                    if (!method.isDefault) {
                        PButton(
                            text = stringResource(R.string.payments_profile_set_default),
                            onClick = { onSetDefaultMethod(method.id) },
                        )
                    }
                }
            }
            if (methodsState.paymentMethods.isEmpty()) {
                Text(
                    text = stringResource(R.string.payments_profile_idle),
                    modifier = Modifier.fillMaxWidth(),
                    style = PasabayanTextStyles.Body.small,
                )
            }
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
        )
    }
}
