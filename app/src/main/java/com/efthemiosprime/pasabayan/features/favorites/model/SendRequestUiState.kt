package com.efthemiosprime.pasabayan.features.favorites.model

data class SendRequestUiState(
    val pickupCity: String = "",
    val pickupAddress: String = "",
    val pickupDate: String = "",
    val deliveryCity: String = "",
    val deliveryAddress: String = "",
    val deliveryDate: String = "",
    val packageType: String = "general",
    val packageDescription: String = "",
    val packageWeightKg: String = "",
    val offeredPrice: String = "",
    val shipperMessage: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val weightAsDouble: Double? get() = packageWeightKg.trim().replace(',', '.').toDoubleOrNull()
    val priceAsDouble: Double? get() = offeredPrice.trim().replace(',', '.').toDoubleOrNull()
    val isReadyToSubmit: Boolean get() = !isSubmitting &&
        pickupCity.isNotBlank() && deliveryCity.isNotBlank() &&
        pickupDate.isNotBlank() && deliveryDate.isNotBlank() &&
        packageDescription.isNotBlank() &&
        (weightAsDouble?.let { it > 0 } == true)
}
