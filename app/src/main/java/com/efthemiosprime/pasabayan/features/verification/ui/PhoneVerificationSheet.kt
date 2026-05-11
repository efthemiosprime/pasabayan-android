package com.efthemiosprime.pasabayan.features.verification.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.verification.model.PhoneVerificationUiState
import com.efthemiosprime.pasabayan.features.verification.viewmodel.PhoneVerificationViewModel

object PhoneVerificationTestTags {
    const val Root = "phone_verification"
    const val PhoneNumber = "phone_verification_number"
    const val Send = "phone_verification_send"
    const val Otp = "phone_verification_otp"
    const val Verify = "phone_verification_verify"
    const val Resend = "phone_verification_resend"
    const val ChangeNumber = "phone_verification_change_number"
}

@Composable
fun PhoneVerificationSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onVerified: () -> Unit = {},
    onUpgradeToPremium: (() -> Unit)? = null,
    viewModel: PhoneVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bootstrap() }
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) onVerified()
    }
    PhoneVerificationContent(
        state = state,
        onPhoneChange = viewModel::onPhoneNumberChange,
        onOtpChange = viewModel::onOtpCodeChange,
        onSend = viewModel::sendOtp,
        onVerify = viewModel::verifyOtp,
        onResend = viewModel::resendOtp,
        onChangeNumber = viewModel::reset,
        onClose = onClose,
        onUpgradeToPremium = onUpgradeToPremium,
        modifier = modifier,
    )
}

@Composable
fun PhoneVerificationContent(
    state: PhoneVerificationUiState,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSend: () -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onChangeNumber: () -> Unit,
    onClose: () -> Unit,
    onUpgradeToPremium: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.md,
            )
            .testTag(PhoneVerificationTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.verification_phone_title),
                style = PasabayanTextStyles.Heading.h3,
            )
            Text(
                text = stringResource(R.string.verification_phone_subtitle),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.isVerified) {
            VerifiedCard(onClose = onClose, onUpgradeToPremium = onUpgradeToPremium)
        } else if (state.isOtpSent) {
            OtpStepCard(
                state = state,
                onOtpChange = onOtpChange,
                onVerify = onVerify,
                onResend = onResend,
                onChangeNumber = onChangeNumber,
            )
        } else {
            PhoneStepCard(
                state = state,
                onPhoneChange = onPhoneChange,
                onSend = onSend,
            )
        }
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        state.successMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = msg, style = PasabayanTextStyles.Body.small)
            }
        }
    }
}

@Composable
private fun PhoneStepCard(
    state: PhoneVerificationUiState,
    onPhoneChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                POutlinedTextField(
                    value = state.countryCode,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.verification_phone_country_label)) },
                    enabled = false,
                    modifier = Modifier.width(110.dp),
                )
                POutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(R.string.verification_phone_number_label)) },
                    placeholder = {
                        Text(stringResource(R.string.verification_phone_number_placeholder))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = state.phoneNumber.isNotEmpty() && !state.isPhoneValid,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(PhoneVerificationTestTags.PhoneNumber),
                )
            }
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.verification_phone_send_action),
                    onClick = onSend,
                    enabled = state.isPhoneValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PhoneVerificationTestTags.Send),
                )
            }
        }
    }
}

@Composable
private fun OtpStepCard(
    state: PhoneVerificationUiState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onChangeNumber: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.verification_phone_otp_section_title),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(
                    R.string.verification_phone_otp_section_subtitle,
                    state.formattedE164Phone,
                ),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            POutlinedTextField(
                value = state.otpCode,
                onValueChange = onOtpChange,
                label = { Text(stringResource(R.string.verification_phone_otp_label)) },
                placeholder = { Text(stringResource(R.string.verification_phone_otp_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PhoneVerificationTestTags.Otp),
            )
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.verification_phone_verify_action),
                    onClick = onVerify,
                    enabled = state.isOtpValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PhoneVerificationTestTags.Verify),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.canResend) {
                    PButton(
                        text = stringResource(R.string.verification_phone_resend_action),
                        onClick = onResend,
                        modifier = Modifier.testTag(PhoneVerificationTestTags.Resend),
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.verification_phone_resend_in,
                            state.remainingResendSeconds,
                        ),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PButton(
                    text = stringResource(R.string.verification_phone_change_number),
                    onClick = onChangeNumber,
                    modifier = Modifier.testTag(PhoneVerificationTestTags.ChangeNumber),
                )
            }
        }
    }
}

@Composable
private fun VerifiedCard(onClose: () -> Unit, onUpgradeToPremium: (() -> Unit)?) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.verification_phone_success),
                style = PasabayanTextStyles.Heading.h5,
            )
            if (onUpgradeToPremium != null) {
                PButton(
                    text = stringResource(R.string.verification_premium_title),
                    onClick = onUpgradeToPremium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            PButton(
                text = stringResource(R.string.verification_phone_close),
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Phone — light")
@Preview(showBackground = true, name = "Phone — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhoneVerificationPreview() {
    PasabayanTheme {
        PhoneVerificationContent(
            state = PhoneVerificationUiState(phoneNumber = "5145551234"),
            onPhoneChange = {},
            onOtpChange = {},
            onSend = {},
            onVerify = {},
            onResend = {},
            onChangeNumber = {},
            onClose = {},
        )
    }
}
