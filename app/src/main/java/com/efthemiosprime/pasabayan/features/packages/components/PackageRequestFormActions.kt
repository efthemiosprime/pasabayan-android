package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle

/**
 * Bottom-of-sheet action row for the shipper package-create wizard.
 *
 * Renders one of two layouts:
 * - **Submit:** a single submit button (state when [hasReachedFullReview]
 *   is true — i.e. the shipper has finished step 4 and is on the review).
 * - **Wizard nav:** Back + Next/Review buttons (state when the shipper is
 *   still stepping through pages 0–3).
 *
 * iOS parity: `Views/Components/PackageRequest/PackageRequestFormActions.swift`.
 */
@Composable
internal fun PackageRequestFormActions(
    hasReachedFullReview: Boolean,
    currentStep: Int,
    isFormValid: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (hasReachedFullReview) {
        PButton(
            text = stringResource(R.string.packages_create_submit),
            onClick = onSubmit,
            style = PButtonStyle.Submit,
            enabled = isFormValid && !isSubmitting,
            modifier = modifier.padding(PasabayanSpacing.lg),
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            if (currentStep > 0) {
                PButton(
                    text = stringResource(R.string.packages_create_back),
                    onClick = onBack,
                    style = PButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
            }
            PButton(
                text = if (currentStep == 3) {
                    stringResource(R.string.packages_create_review)
                } else {
                    stringResource(R.string.packages_create_next)
                },
                onClick = onNext,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, name = "Wizard step — light")
@Preview(showBackground = true, name = "Wizard step — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestFormActionsWizardPreview() {
    PasabayanTheme {
        PackageRequestFormActions(
            hasReachedFullReview = false,
            currentStep = 1,
            isFormValid = false,
            isSubmitting = false,
            onBack = {},
            onNext = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "Submit — light")
@Preview(showBackground = true, name = "Submit — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestFormActionsSubmitPreview() {
    PasabayanTheme {
        PackageRequestFormActions(
            hasReachedFullReview = true,
            currentStep = 3,
            isFormValid = true,
            isSubmitting = false,
            onBack = {},
            onNext = {},
            onSubmit = {},
        )
    }
}
