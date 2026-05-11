package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.features.profile.model.EditCarrierProfileField
import com.efthemiosprime.pasabayan.features.profile.model.EditCarrierProfileUiState
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
 * Drives `EditCarrierProfileSheet`. On `initialize` loads the existing carrier profile (if any) so
 * the form pre-fills with current capacity/pricing/bio/preferences. Save dispatches to
 * [ProfileRepository.updateCarrierProfile] when a profile already exists, otherwise
 * [ProfileRepository.createCarrierProfile] (which itself handles the 409 → GET fallback per
 * `09-profile-carrier-consent`). On a successful create we follow up with
 * [ProfileRepository.enableCarrier] (idempotent).
 */
@HiltViewModel
class EditCarrierProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(EditCarrierProfileUiState())
    val state: StateFlow<EditCarrierProfileUiState> = _state.asStateFlow()

    private var loadedProfile: CarrierProfileJson? = null

    fun initialize() {
        if (_state.value.isInitialized) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.fetchCarrierProfile()
            val profile = result.getOrNull()
            loadedProfile = profile
            _state.update {
                it.copy(
                    maxWeightKg = profile?.maxWeightCapacityKg?.toCleanString().orEmpty(),
                    maxSpaceLiters = profile?.maxSpaceCapacityLiters?.toCleanString().orEmpty(),
                    pricePerKgCad = profile?.defaultPricePerKg?.toCleanString().orEmpty(),
                    insuranceCoverageCad = profile?.insuranceCoverageAmount?.toCleanString()
                        .orEmpty(),
                    bio = profile?.bio.orEmpty(),
                    selectedPackageTypes = profile?.preferredPackageTypes?.toSet() ?: emptySet(),
                    selectedRestrictedItems = profile?.restrictedItems?.toSet() ?: emptySet(),
                    hasExistingProfile = profile != null,
                    isInitialized = true,
                    isLoading = false,
                )
            }
        }
    }

    fun onMaxWeightChange(value: String) {
        _state.update { it.copy(maxWeightKg = value, fieldErrors = it.fieldErrors - EditCarrierProfileField.MAX_WEIGHT, errorMessage = null) }
    }

    fun onMaxSpaceChange(value: String) {
        _state.update { it.copy(maxSpaceLiters = value, fieldErrors = it.fieldErrors - EditCarrierProfileField.MAX_SPACE, errorMessage = null) }
    }

    fun onPricePerKgChange(value: String) {
        _state.update { it.copy(pricePerKgCad = value, fieldErrors = it.fieldErrors - EditCarrierProfileField.PRICE, errorMessage = null) }
    }

    fun onInsuranceChange(value: String) {
        _state.update { it.copy(insuranceCoverageCad = value, fieldErrors = it.fieldErrors - EditCarrierProfileField.INSURANCE, errorMessage = null) }
    }

    fun onBioChange(value: String) {
        _state.update { it.copy(bio = value.take(MAX_BIO_LENGTH), errorMessage = null) }
    }

    fun togglePackageType(raw: String) {
        _state.update {
            val next = it.selectedPackageTypes.toMutableSet()
            if (!next.add(raw)) next.remove(raw)
            it.copy(selectedPackageTypes = next, errorMessage = null)
        }
    }

    fun toggleRestrictedItem(raw: String) {
        _state.update {
            val next = it.selectedRestrictedItems.toMutableSet()
            if (!next.add(raw)) next.remove(raw)
            it.copy(selectedRestrictedItems = next, errorMessage = null)
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun save() {
        val snapshot = _state.value
        if (snapshot.isFormBusy) return
        val weight = snapshot.maxWeightKg.toCleanDouble()
        val space = snapshot.maxSpaceLiters.toCleanDouble()
        val price = snapshot.pricePerKgCad.toCleanDouble()
        val insurance = snapshot.insuranceCoverageCad.takeIf { it.isNotBlank() }?.toCleanDouble()
        val errors = buildSet {
            if (weight == null || weight < 0.0) add(EditCarrierProfileField.MAX_WEIGHT)
            if (space == null || space < 0.0) add(EditCarrierProfileField.MAX_SPACE)
            if (price == null || price <= 0.0) add(EditCarrierProfileField.PRICE)
            if (snapshot.insuranceCoverageCad.isNotBlank() && (insurance == null || insurance < 0.0)) {
                add(EditCarrierProfileField.INSURANCE)
            }
        }
        if (errors.isNotEmpty()) {
            _state.update {
                it.copy(
                    fieldErrors = errors,
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.profile_carrier_error_required_fields,
                    ),
                )
            }
            return
        }
        _state.update {
            it.copy(
                isSaving = true,
                fieldErrors = emptySet(),
                errorMessage = null,
                successMessage = null,
            )
        }
        val body = CreateCarrierProfileRequestJson(
            preferredPickupCityId = loadedProfile?.preferredPickupCityId,
            maxWeightCapacityKg = weight!!,
            maxSpaceCapacityLiters = space!!,
            preferredPackageTypes = snapshot.selectedPackageTypes.takeIf { it.isNotEmpty() }
                ?.toList(),
            restrictedItems = snapshot.selectedRestrictedItems.takeIf { it.isNotEmpty() }
                ?.toList(),
            defaultPricePerKg = price!!,
            availableRoutes = loadedProfile?.availableRoutes,
            insuranceCoverageAmount = insurance,
            bio = snapshot.bio.takeIf { it.isNotBlank() },
        )
        viewModelScope.launch {
            val wasExisting = snapshot.hasExistingProfile
            val result = if (wasExisting) {
                repository.updateCarrierProfile(body)
            } else {
                repository.createCarrierProfile(body)
            }
            result.fold(
                onSuccess = { updated ->
                    loadedProfile = updated
                    if (!wasExisting) {
                        // Idempotent — swallow failure here, the carrier was already enabled or
                        // will be enabled by the next API call.
                        repository.enableCarrier()
                    }
                    _state.update {
                        it.copy(
                            isSaving = false,
                            hasExistingProfile = true,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_carrier_success,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    val err = (throwable as? DomainErrorMapperException)?.domainError
                        ?: DomainError.NetworkError(throwable)
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = err.localizedMessage(appContext),
                        )
                    }
                },
            )
        }
    }

    private fun Double.toCleanString(): String {
        // Avoid trailing ".0" for whole numbers — matches the iOS Double.formatted() default.
        return if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
    }

    private fun String.toCleanDouble(): Double? =
        trim().replace(',', '.').toDoubleOrNull()

    private companion object {
        const val MAX_BIO_LENGTH = 500
    }
}
