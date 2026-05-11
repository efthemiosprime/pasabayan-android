package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.profile.model.ConsentPreference
import com.efthemiosprime.pasabayan.features.profile.model.PrivacyPreferencesUiState
import com.efthemiosprime.pasabayan.features.profile.viewmodel.PrivacyPreferencesViewModel

object PrivacyPreferencesTestTags {
    const val Root = "privacy_preferences"
    fun toggleFor(preference: ConsentPreference) = "privacy_toggle_${preference.raw}"
    const val ConfirmDisable = "privacy_confirm_disable"
}

@Composable
fun PrivacyPreferencesSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrivacyPreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    PrivacyPreferencesContent(
        state = state,
        onToggle = viewModel::requestToggle,
        onConfirmDisable = viewModel::confirmPendingDisable,
        onCancelDisable = viewModel::cancelPendingDisable,
        onClose = onClose,
        modifier = modifier,
    )
    state.pendingDisable?.let { pending ->
        val descriptionRes = pending.disableConfirmRes ?: pending.descriptionRes
        AlertDialog(
            onDismissRequest = viewModel::cancelPendingDisable,
            title = { Text(stringResource(R.string.profile_privacy_confirm_disable_title)) },
            text = { Text(stringResource(descriptionRes)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmPendingDisable,
                    modifier = Modifier.testTag(PrivacyPreferencesTestTags.ConfirmDisable),
                ) {
                    Text(
                        text = stringResource(R.string.profile_privacy_confirm_turn_off),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelPendingDisable) {
                    Text(stringResource(R.string.profile_privacy_confirm_keep))
                }
            },
        )
    }
}

@Composable
fun PrivacyPreferencesContent(
    state: PrivacyPreferencesUiState,
    onToggle: (ConsentPreference, Boolean) -> Unit,
    onConfirmDisable: () -> Unit,
    onCancelDisable: () -> Unit,
    onClose: () -> Unit,
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
            .testTag(PrivacyPreferencesTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.profile_privacy_title),
                style = PasabayanTextStyles.Heading.h3,
            )
            Text(
                text = stringResource(R.string.profile_privacy_subtitle),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PCircularProgress()
            }
            return@Column
        }
        ConsentPreference.entries.forEach { preference ->
            ConsentToggleCard(
                preference = preference,
                value = state.valueFor(preference),
                onToggle = { desired -> onToggle(preference, desired) },
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
        PButton(
            text = stringResource(R.string.profile_privacy_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    // [onConfirmDisable] / [onCancelDisable] are wired through the parent sheet's AlertDialog.
    @Suppress("UnusedExpression")
    run {
        onConfirmDisable
        onCancelDisable
    }
}

@Composable
private fun ConsentToggleCard(
    preference: ConsentPreference,
    value: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Text(
                    text = stringResource(preference.labelRes),
                    style = PasabayanTextStyles.Body.medium,
                )
                Text(
                    text = stringResource(preference.descriptionRes),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = value,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag(PrivacyPreferencesTestTags.toggleFor(preference)),
            )
        }
    }
}

@Preview(showBackground = true, name = "Privacy — light")
@Preview(showBackground = true, name = "Privacy — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrivacyPreferencesPreview() {
    PasabayanTheme {
        PrivacyPreferencesContent(
            state = PrivacyPreferencesUiState(
                pushNotifications = true,
                locationTracking = true,
                analytics = false,
                marketingCommunications = true,
            ),
            onToggle = { _, _ -> },
            onConfirmDisable = {},
            onCancelDisable = {},
            onClose = {},
        )
    }
}
