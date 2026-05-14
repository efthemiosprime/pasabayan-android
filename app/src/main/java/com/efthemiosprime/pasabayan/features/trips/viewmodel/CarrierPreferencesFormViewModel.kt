package com.efthemiosprime.pasabayan.features.trips.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.features.locations.services.LocationCatalogRepository
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.features.trips.model.CarrierPreferencesFormUiState
import com.efthemiosprime.pasabayan.features.trips.services.CarrierPreferencesFormStore
import com.efthemiosprime.pasabayan.features.trips.services.UsualTransportStore
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
 * Drives `CarrierPreferencesFormSheet` — the slim 4-field "Set Your Defaults" form.
 * iOS parity: `Pasabayan/Features/Profile/ViewModels/CarrierPreferencesFormViewModel.swift`.
 *
 * `load()` fetches the existing carrier profile and seeds fields plus the user's last-saved
 * `UsualTransportStore` value. `save()` validates weight/space, resolves
 * `preferred_pickup_city_id` from the location catalog (or `null` when blank), and dispatches
 * to [ProfileRepository.updateCarrierProfile] when a profile exists or
 * [ProfileRepository.createCarrierProfile] otherwise (repository handles the 409 → GET
 * fallback). On success, persists [UsualTransportStore] and fires
 * [ProfileRepository.enableCarrier] (idempotent, swallow failure).
 *
 * Preserves untouched server-side fields from the loaded profile (preferredPackageTypes,
 * restrictedItems, defaultPricePerKg, availableRoutes, insuranceCoverageAmount, bio) so the
 * slim form does not blank them out.
 */
@HiltViewModel
class CarrierPreferencesFormViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val locationCatalogRepository: LocationCatalogRepository,
    private val usualTransportStore: UsualTransportStore,
    private val acknowledgedStore: CarrierPreferencesFormStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(CarrierPreferencesFormUiState())
    val state: StateFlow<CarrierPreferencesFormUiState> = _state.asStateFlow()

    private var loadedProfile: CarrierProfileJson? = null

    fun isAcknowledged(userId: Long): Boolean = acknowledgedStore.isAcknowledged(userId)

    fun markAcknowledged(userId: Long) {
        acknowledgedStore.markAcknowledged(userId)
    }

    fun load() {
        if (_state.value.isInitialized || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            locationCatalogRepository.refreshIfNeeded()
            val userId = authRepository.currentUser().value?.id
            val result = profileRepository.fetchCarrierProfile()
            val profile = result.getOrNull()
            loadedProfile = profile
            val displayName = profile?.preferredPickupCity?.let {
                if (it.stateCode.isNotBlank()) "${it.name}, ${it.stateCode}" else it.name
            }.orEmpty().ifEmpty {
                profile?.preferredPickupCityId?.let { cityId ->
                    locationCatalogRepository.snapshot.value.displayNameFor(cityId).orEmpty()
                }.orEmpty()
            }
            val savedTransport = userId
                ?.let { usualTransportStore.get(it.toInt()) }
                ?: TransportationMethod.CAR
            _state.update {
                it.copy(
                    preferredPickupCityId = profile?.preferredPickupCityId,
                    preferredPickupCityText = displayName,
                    maxWeightKg = profile?.maxWeightCapacityKg
                        ?.takeIf { v -> v > 0.0 }
                        ?.let(::formatWhole)
                        .orEmpty(),
                    maxSpaceLiters = profile?.maxSpaceCapacityLiters
                        ?.takeIf { v -> v > 0.0 }
                        ?.let(::formatWhole)
                        .orEmpty(),
                    usualTransport = savedTransport,
                    hasExistingProfile = profile?.id != null && profile.setupRequired != true,
                    isInitialized = true,
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.let(::userMessageFor),
                )
            }
        }
    }

    fun onPreferredPickupCityChange(cityName: String) {
        val trimmed = cityName.trim()
        val cityId = if (trimmed.isEmpty()) null
        else locationCatalogRepository.snapshot.value.cityIdFor(trimmed)
        _state.update {
            it.copy(
                preferredPickupCityText = cityName,
                preferredPickupCityId = cityId,
                errorMessage = null,
            )
        }
    }

    fun onMaxWeightChange(value: String) {
        _state.update { it.copy(maxWeightKg = value, errorMessage = null) }
    }

    fun onMaxSpaceChange(value: String) {
        _state.update { it.copy(maxSpaceLiters = value, errorMessage = null) }
    }

    fun onUsualTransportChange(method: TransportationMethod) {
        _state.update { it.copy(usualTransport = method, errorMessage = null) }
    }

    fun consumeSaved() {
        _state.update { it.copy(saved = false) }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun save() {
        val snapshot = _state.value
        if (snapshot.isBusy) return
        val userId = authRepository.currentUser().value?.id ?: return

        val weight = snapshot.maxWeightKg.toCleanDouble()
        if (weight == null || weight < 0.0 || weight > MAX_WEIGHT_KG) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        R.string.carrier_prefs_validation_max_weight_required,
                    ),
                )
            }
            return
        }
        val spaceTrimmed = snapshot.maxSpaceLiters.trim()
        val space: Double = if (spaceTrimmed.isEmpty()) {
            0.0
        } else {
            val parsed = spaceTrimmed.toCleanDouble()
            if (parsed == null || parsed < 0.0 || parsed > MAX_SPACE_LITERS) {
                _state.update {
                    it.copy(
                        errorMessage = appContext.getString(
                            R.string.carrier_prefs_validation_max_space_invalid,
                        ),
                    )
                }
                return
            }
            parsed
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        val trimmedCityText = snapshot.preferredPickupCityText.trim()
        val preferredCityIdToSend: Int? = when {
            trimmedCityText.isEmpty() -> null
            snapshot.preferredPickupCityId != null -> snapshot.preferredPickupCityId
            else -> locationCatalogRepository.snapshot.value.cityIdFor(trimmedCityText)
        }

        val existing = loadedProfile
        val defaultPrice = existing?.defaultPricePerKg ?: DEFAULT_PRICE_PER_KG_FOR_CREATE
        val request = CreateCarrierProfileRequestJson(
            preferredPickupCityId = preferredCityIdToSend,
            maxWeightCapacityKg = weight,
            maxSpaceCapacityLiters = space,
            preferredPackageTypes = existing?.preferredPackageTypes,
            restrictedItems = existing?.restrictedItems,
            defaultPricePerKg = defaultPrice,
            availableRoutes = existing?.availableRoutes,
            insuranceCoverageAmount = existing?.insuranceCoverageAmount,
            bio = existing?.bio,
        )

        viewModelScope.launch {
            val wasExisting = snapshot.hasExistingProfile
            val result = if (wasExisting) {
                profileRepository.updateCarrierProfile(request)
            } else {
                profileRepository.createCarrierProfile(request)
            }
            result.fold(
                onSuccess = { updated ->
                    loadedProfile = updated ?: loadedProfile
                    usualTransportStore.set(userId.toInt(), snapshot.usualTransport)
                    if (!wasExisting) {
                        // Idempotent — swallow failure (carrier already enabled, or next API
                        // call will surface the real error).
                        profileRepository.enableCarrier()
                    }
                    _state.update {
                        it.copy(
                            isSaving = false,
                            hasExistingProfile = true,
                            saved = true,
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = userMessageFor(throwable),
                        )
                    }
                },
            )
        }
    }

    private fun userMessageFor(throwable: Throwable): String {
        val domain = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        return domain.localizedMessage(appContext)
    }

    private fun String.toCleanDouble(): Double? =
        trim().replace(',', '.').toDoubleOrNull()

    private fun formatWhole(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

    private companion object {
        const val MAX_WEIGHT_KG = 999.99
        const val MAX_SPACE_LITERS = 999.99
        const val DEFAULT_PRICE_PER_KG_FOR_CREATE = 5.0
    }
}
