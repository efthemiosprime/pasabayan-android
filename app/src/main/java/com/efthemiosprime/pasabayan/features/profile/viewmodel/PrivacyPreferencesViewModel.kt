package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesDataJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.features.profile.model.ConsentPreference
import com.efthemiosprime.pasabayan.features.profile.model.PrivacyPreferencesUiState
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives `PrivacyPreferencesSheet`. Behavior matches iOS [PrivacyPreferencesViewModel]:
 *
 * - Loads all four toggles via `GET /profile/consent-preferences`.
 * - Toggles update **optimistically**, then `PUT /profile/consent-preferences` with a **scoped**
 *   single-key payload. On failure the local toggle is reverted (the same network call would
 *   otherwise have to round-trip just to re-fetch).
 * - Disabling `push_notifications` / `location_tracking` requires a confirmation step:
 *   `requestToggle` flips the toggle ON immediately (or pops a confirm if turning OFF), and
 *   [confirmPendingDisable] / [cancelPendingDisable] complete the gate.
 */
@HiltViewModel
class PrivacyPreferencesViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PrivacyPreferencesUiState())
    val state: StateFlow<PrivacyPreferencesUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.fetchConsentPreferences().fold(
                onSuccess = { data -> applyServerData(data) },
                onFailure = { applyError(it) },
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Entry point from a `Switch` `onCheckedChange`. Confirms before disabling sensitive toggles;
     * otherwise applies optimistically and PUTs.
     */
    fun requestToggle(preference: ConsentPreference, desired: Boolean) {
        if (!desired && preference.requiresDisableConfirm) {
            _state.update { it.copy(pendingDisable = preference) }
            return
        }
        applyAndPersist(preference, desired)
    }

    fun confirmPendingDisable() {
        val pending = _state.value.pendingDisable ?: return
        _state.update { it.copy(pendingDisable = null) }
        applyAndPersist(pending, false)
    }

    fun cancelPendingDisable() {
        _state.update { it.copy(pendingDisable = null) }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun applyAndPersist(preference: ConsentPreference, value: Boolean) {
        // Optimistic update.
        _state.update { previous ->
            previous.copy(
                pushNotifications = if (preference == ConsentPreference.PUSH_NOTIFICATIONS) value else previous.pushNotifications,
                locationTracking = if (preference == ConsentPreference.LOCATION_TRACKING) value else previous.locationTracking,
                analytics = if (preference == ConsentPreference.ANALYTICS) value else previous.analytics,
                marketingCommunications = if (preference == ConsentPreference.MARKETING) value else previous.marketingCommunications,
                errorMessage = null,
            )
        }
        val request = scopedUpdate(preference, value)
        viewModelScope.launch {
            repository.updateConsentPreferences(request).fold(
                onSuccess = { data -> applyServerData(data) },
                onFailure = { throwable ->
                    // Revert.
                    _state.update { previous ->
                        previous.copy(
                            pushNotifications = if (preference == ConsentPreference.PUSH_NOTIFICATIONS) !value else previous.pushNotifications,
                            locationTracking = if (preference == ConsentPreference.LOCATION_TRACKING) !value else previous.locationTracking,
                            analytics = if (preference == ConsentPreference.ANALYTICS) !value else previous.analytics,
                            marketingCommunications = if (preference == ConsentPreference.MARKETING) !value else previous.marketingCommunications,
                        )
                    }
                    applyError(throwable)
                },
            )
        }
    }

    private fun applyServerData(data: ConsentPreferencesDataJson) {
        _state.update {
            it.copy(
                pushNotifications = data.pushNotifications,
                locationTracking = data.locationTracking,
                analytics = data.analytics,
                marketingCommunications = data.marketingCommunications,
            )
        }
    }

    private fun applyError(throwable: Throwable) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _state.update { it.copy(errorMessage = err.localizedMessage(appContext)) }
    }

    private fun scopedUpdate(preference: ConsentPreference, value: Boolean) =
        when (preference) {
            ConsentPreference.PUSH_NOTIFICATIONS -> ConsentPreferencesUpdateJson(pushNotifications = value)
            ConsentPreference.LOCATION_TRACKING -> ConsentPreferencesUpdateJson(locationTracking = value)
            ConsentPreference.ANALYTICS -> ConsentPreferencesUpdateJson(analytics = value)
            ConsentPreference.MARKETING -> ConsentPreferencesUpdateJson(marketingCommunications = value)
        }
}
