package com.efthemiosprime.pasabayan.features.verification.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason

object VerifyPhonePromptTestTags {
    const val Root = "verify_phone_prompt"
    const val Title = "verify_phone_prompt_title"
    const val Body = "verify_phone_prompt_body"
    const val VerifyCta = "verify_phone_prompt_verify"
    const val DismissCta = "verify_phone_prompt_dismiss"
}

/**
 * Action-blocking prompt shown when a phone-verification-gated action is attempted by a
 * user with an unverified phone. Stateless and reusable across the four gated paths
 * (create package, create trip, book trip, request to carry).
 *
 * @param onVerifyNow Caller chains the existing phone verification entry (e.g. show
 *                    [PhoneVerificationSheet]). The prompt does not auto-resume the action.
 */
@Composable
fun VerifyPhonePromptSheet(
    reason: VerifyPhoneReason,
    onVerifyNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        VerifyPhonePromptContent(
            reason = reason,
            onVerifyNow = onVerifyNow,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun VerifyPhonePromptContent(
    reason: VerifyPhoneReason,
    onVerifyNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.lg,
            )
            .testTag(VerifyPhonePromptTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.verification_gate_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag(VerifyPhonePromptTestTags.Title),
        )
        Text(
            text = stringResource(reason.bodyRes),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(VerifyPhonePromptTestTags.Body),
        )
        PButton(
            text = stringResource(R.string.verification_gate_cta_verify),
            onClick = onVerifyNow,
            style = PButtonStyle.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(VerifyPhonePromptTestTags.VerifyCta),
        )
        PButton(
            text = stringResource(R.string.verification_gate_cta_dismiss),
            onClick = onDismiss,
            style = PButtonStyle.Tertiary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(VerifyPhonePromptTestTags.DismissCta),
        )
    }
}

@Preview(showBackground = true, name = "VerifyPhonePrompt — light")
@Preview(
    showBackground = true,
    name = "VerifyPhonePrompt — dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VerifyPhonePromptCreatePackagePreview() {
    PasabayanTheme {
        VerifyPhonePromptContent(
            reason = VerifyPhoneReason.CreatePackage,
            onVerifyNow = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "VerifyPhonePrompt — book trip")
@Composable
private fun VerifyPhonePromptBookTripPreview() {
    PasabayanTheme {
        VerifyPhonePromptContent(
            reason = VerifyPhoneReason.BookTrip,
            onVerifyNow = {},
            onDismiss = {},
        )
    }
}
