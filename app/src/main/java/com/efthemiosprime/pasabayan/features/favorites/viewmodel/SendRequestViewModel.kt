package com.efthemiosprime.pasabayan.features.favorites.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson
import com.efthemiosprime.pasabayan.features.favorites.model.SendRequestUiState
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SendRequestViewModel @Inject constructor(
    private val repository: FavoritesRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SendRequestUiState())
    val state: StateFlow<SendRequestUiState> = _state.asStateFlow()

    fun onPickupCityChange(value: String) {
        _state.update { it.copy(pickupCity = value, errorMessage = null) }
    }

    fun onPickupAddressChange(value: String) {
        _state.update { it.copy(pickupAddress = value, errorMessage = null) }
    }

    fun onPickupDateChange(value: String) {
        _state.update { it.copy(pickupDate = value, errorMessage = null) }
    }

    fun onDeliveryCityChange(value: String) {
        _state.update { it.copy(deliveryCity = value, errorMessage = null) }
    }

    fun onDeliveryAddressChange(value: String) {
        _state.update { it.copy(deliveryAddress = value, errorMessage = null) }
    }

    fun onDeliveryDateChange(value: String) {
        _state.update { it.copy(deliveryDate = value, errorMessage = null) }
    }

    fun onPackageTypeChange(value: String) {
        _state.update { it.copy(packageType = value, errorMessage = null) }
    }

    fun onPackageDescriptionChange(value: String) {
        _state.update { it.copy(packageDescription = value, errorMessage = null) }
    }

    fun onPackageWeightChange(value: String) {
        _state.update { it.copy(packageWeightKg = value, errorMessage = null) }
    }

    fun onOfferedPriceChange(value: String) {
        _state.update { it.copy(offeredPrice = value, errorMessage = null) }
    }

    fun onMessageChange(value: String) {
        _state.update { it.copy(shipperMessage = value, errorMessage = null) }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun submit(carrierId: Int) {
        val snapshot = _state.value
        if (!snapshot.isReadyToSubmit) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.favorites_send_request_error_required,
                    ),
                )
            }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            val body = SendDeliveryRequestJson(
                pickupCity = snapshot.pickupCity.trim(),
                pickupDatePreferred = snapshot.pickupDate.trim(),
                deliveryCity = snapshot.deliveryCity.trim(),
                deliveryDateNeeded = snapshot.deliveryDate.trim(),
                packageDescription = snapshot.packageDescription.trim(),
                packageWeightKg = snapshot.weightAsDouble ?: 0.0,
                packageType = snapshot.packageType.trim().ifBlank { "general" },
                pickupAddress = snapshot.pickupAddress.trim().takeIf { it.isNotEmpty() },
                deliveryAddress = snapshot.deliveryAddress.trim().takeIf { it.isNotEmpty() },
                offeredPrice = snapshot.priceAsDouble,
                shipperMessage = snapshot.shipperMessage.trim().takeIf { it.isNotEmpty() },
            )
            repository.sendDeliveryRequest(carrierId = carrierId, request = body).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.favorites_send_request_success,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    val err = (throwable as? DomainErrorMapperException)?.domainError
                        ?: DomainError.NetworkError(throwable)
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.localizedMessage(appContext),
                        )
                    }
                },
            )
        }
    }
}
