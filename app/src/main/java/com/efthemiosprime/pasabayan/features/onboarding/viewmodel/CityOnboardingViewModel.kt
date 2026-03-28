package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.onboarding.model.CityPickerOption
import com.efthemiosprime.pasabayan.features.onboarding.services.CityOnboardingRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CityOnboardingUiState(
    val cities: List<CityPickerOption> = emptyList(),
    val selectedCity: CityPickerOption? = null,
    val isLoadingCities: Boolean = true,
    val isSaving: Boolean = false,
    val loadErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    val showPickerSheet: Boolean = false,
    val searchQuery: String = "",
    val showLocationStubMessage: Boolean = false,
)

@HiltViewModel
class CityOnboardingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: CityOnboardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityOnboardingUiState())
    val uiState: StateFlow<CityOnboardingUiState> = _uiState.asStateFlow()

    fun loadCities() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingCities = true, loadErrorMessage = null)
            }
            repository.fetchCitiesCanada().fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(cities = list, isLoadingCities = false)
                    }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(
                            isLoadingCities = false,
                            loadErrorMessage = e.toLocalizedUserMessage(),
                        )
                    }
                },
            )
        }
    }

    fun setShowPickerSheet(show: Boolean) {
        _uiState.update {
            if (!show) {
                it.copy(showPickerSheet = false, searchQuery = "")
            } else {
                it.copy(showPickerSheet = true)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCity(option: CityPickerOption) {
        _uiState.update {
            it.copy(
                selectedCity = option,
                showPickerSheet = false,
                searchQuery = "",
                saveErrorMessage = null,
            )
        }
    }

    fun onUseMyLocationStub() {
        _uiState.update { it.copy(showLocationStubMessage = true) }
    }

    /**
     * Persists [home_city_id] when a city is selected; always invokes [onFinished] after the attempt
     * (iOS continues past this step). With no selection, shows a validation message and does not finish.
     */
    fun saveAndContinue(onFinished: () -> Unit) {
        viewModelScope.launch {
            val sel = _uiState.value.selectedCity
            if (sel == null) {
                _uiState.update {
                    it.copy(saveErrorMessage = appContext.getString(R.string.onboarding_city_error_select))
                }
                return@launch
            }
            _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }
            repository.updateHomeCity(sel.id).fold(
                onSuccess = { },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(saveErrorMessage = e.toLocalizedUserMessage())
                    }
                },
            )
            _uiState.update { it.copy(isSaving = false) }
            onFinished()
        }
    }

    private fun Throwable.toLocalizedUserMessage(): String {
        (this as? DomainErrorMapperException)?.domainError?.let { return it.localizedMessage(appContext) }
        return message ?: appContext.getString(R.string.error_generic)
    }
}
