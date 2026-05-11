package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.profile.model.CurrencyPreference
import com.efthemiosprime.pasabayan.features.profile.model.SettingsUiState
import com.efthemiosprime.pasabayan.features.profile.viewmodel.SettingsViewModel

object SettingsTestTags {
    const val Root = "settings_root"
    const val Currency = "settings_currency"
    const val ClearCache = "settings_clear_cache"
    const val CarrierPrefs = "settings_carrier_prefs"
    const val Privacy = "settings_privacy"
    const val Export = "settings_export"
    const val AccountData = "settings_account_data"
    const val SignOut = "settings_sign_out"
}

@Composable
fun SettingsScreen(
    currentRole: UserRole,
    onOpenCarrierPreferences: () -> Unit,
    onOpenPrivacyPreferences: () -> Unit,
    onOpenAccountManagement: () -> Unit,
    onSignOut: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        currentRole = currentRole,
        onCurrencyChange = viewModel::onCurrencyChange,
        onClearCache = viewModel::clearCache,
        onOpenCarrierPreferences = onOpenCarrierPreferences,
        onOpenPrivacyPreferences = onOpenPrivacyPreferences,
        onOpenAccountManagement = onOpenAccountManagement,
        onSignOut = onSignOut,
        onOpenTerms = onOpenTerms,
        modifier = modifier,
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    currentRole: UserRole,
    onCurrencyChange: (CurrencyPreference) -> Unit,
    onClearCache: () -> Unit,
    onOpenCarrierPreferences: () -> Unit,
    onOpenPrivacyPreferences: () -> Unit,
    onOpenAccountManagement: () -> Unit,
    onSignOut: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val versionName = rememberVersionName()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.md,
            )
            .testTag(SettingsTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        PreferencesSection(
            state = state,
            currentRole = currentRole,
            onCurrencyChange = onCurrencyChange,
        )
        MoreSection(
            currentRole = currentRole,
            onOpenCarrierPreferences = onOpenCarrierPreferences,
            onOpenPrivacyPreferences = onOpenPrivacyPreferences,
        )
        StorageSection(
            state = state,
            onClearCache = onClearCache,
            onOpenAccountManagement = onOpenAccountManagement,
        )
        AccountSection(
            onOpenAccountManagement = onOpenAccountManagement,
            onSignOut = onSignOut,
        )
        AboutSection(versionName = versionName, onOpenTerms = onOpenTerms)
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        state.infoMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = msg, style = PasabayanTextStyles.Body.small)
            }
        }
    }
}

@Composable
private fun PreferencesSection(
    state: SettingsUiState,
    currentRole: UserRole,
    onCurrencyChange: (CurrencyPreference) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_settings_section_preferences),
                style = PasabayanTextStyles.Heading.h5,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_settings_role_label),
                    style = PasabayanTextStyles.Body.medium,
                )
                Text(
                    text = stringResource(
                        when (currentRole) {
                            UserRole.SHIPPER -> R.string.dashboard_role_shipper
                            UserRole.CARRIER -> R.string.dashboard_role_carrier
                        },
                    ),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = stringResource(R.string.profile_settings_currency_label),
                    style = PasabayanTextStyles.Body.medium,
                )
                CurrencyPreference.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencyChange(currency) }
                            .padding(vertical = PasabayanSpacing.xs)
                            .testTag(SettingsTestTags.Currency + "_${currency.code}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    ) {
                        RadioButton(
                            selected = currency == state.currency,
                            onClick = { onCurrencyChange(currency) },
                        )
                        Text(
                            text = stringResource(currency.labelRes),
                            style = PasabayanTextStyles.Body.medium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.profile_settings_currency_helper),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MoreSection(
    currentRole: UserRole,
    onOpenCarrierPreferences: () -> Unit,
    onOpenPrivacyPreferences: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.profile_settings_section_more),
                style = PasabayanTextStyles.Heading.h5,
            )
            if (currentRole == UserRole.CARRIER) {
                SettingsRow(
                    label = stringResource(R.string.profile_settings_carrier_preferences),
                    onClick = onOpenCarrierPreferences,
                    testTag = SettingsTestTags.CarrierPrefs,
                )
            }
            SettingsRow(
                label = stringResource(R.string.profile_settings_privacy),
                onClick = onOpenPrivacyPreferences,
                testTag = SettingsTestTags.Privacy,
            )
        }
    }
}

@Composable
private fun StorageSection(
    state: SettingsUiState,
    onClearCache: () -> Unit,
    onOpenAccountManagement: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_settings_section_storage),
                style = PasabayanTextStyles.Heading.h5,
            )
            if (state.isClearingCache) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.profile_settings_clear_cache),
                    onClick = onClearCache,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsTestTags.ClearCache),
                )
            }
            PButton(
                text = stringResource(R.string.profile_settings_export),
                onClick = onOpenAccountManagement,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SettingsTestTags.Export),
            )
        }
    }
}

@Composable
private fun AccountSection(
    onOpenAccountManagement: () -> Unit,
    onSignOut: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.profile_settings_section_account),
                style = PasabayanTextStyles.Heading.h5,
            )
            SettingsRow(
                label = stringResource(R.string.profile_settings_account_data),
                onClick = onOpenAccountManagement,
                testTag = SettingsTestTags.AccountData,
            )
            SettingsRow(
                label = stringResource(R.string.profile_settings_sign_out),
                onClick = onSignOut,
                testTag = SettingsTestTags.SignOut,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun AboutSection(versionName: String, onOpenTerms: () -> Unit) {
    val appName = stringResource(R.string.app_name)
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.profile_settings_section_about),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.profile_settings_about_version, appName, versionName),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsRow(
                label = stringResource(R.string.profile_settings_about_terms),
                onClick = onOpenTerms,
                testTag = null,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    testTag: String?,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.sm)
            .let { if (testTag != null) it.testTag(testTag) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.medium,
            color = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun rememberVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: ""
        }.getOrDefault("")
    }
}

@Preview(showBackground = true, name = "Settings — light")
@Preview(showBackground = true, name = "Settings — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsPreview() {
    PasabayanTheme {
        SettingsContent(
            state = SettingsUiState(currency = CurrencyPreference.CAD),
            currentRole = UserRole.CARRIER,
            onCurrencyChange = {},
            onClearCache = {},
            onOpenCarrierPreferences = {},
            onOpenPrivacyPreferences = {},
            onOpenAccountManagement = {},
            onSignOut = {},
            onOpenTerms = {},
        )
    }
}
