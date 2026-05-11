package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.profile.model.CurrencyPreference
import com.efthemiosprime.pasabayan.features.profile.model.SettingsUiState
import com.efthemiosprime.pasabayan.features.profile.services.CacheClearer
import com.efthemiosprime.pasabayan.features.profile.services.PreferredCurrencyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currencyStore: PreferredCurrencyStore,
    private val cacheClearer: CacheClearer,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(currency = currencyStore.get()))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onCurrencyChange(currency: CurrencyPreference) {
        currencyStore.set(currency)
        _state.update { it.copy(currency = currency, errorMessage = null) }
    }

    fun clearCache() {
        if (_state.value.isClearingCache) return
        _state.update { it.copy(isClearingCache = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            val result = runCatching { cacheClearer.clear() }
            _state.update { state ->
                result.fold(
                    onSuccess = { cleared ->
                        state.copy(
                            isClearingCache = false,
                            infoMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_settings_cache_cleared_formatted,
                                cleared.bytesFreed / 1024L,
                            ),
                        )
                    },
                    onFailure = {
                        state.copy(
                            isClearingCache = false,
                            errorMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_settings_cache_cleared_error,
                            ),
                        )
                    },
                )
            }
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
