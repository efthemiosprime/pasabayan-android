package com.efthemiosprime.pasabayan.features.payments.model

data class PaymentMethodDisplay(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
) {
    val displayName: String
        get() = "${brand.replaceFirstChar { it.uppercase() }} •••• $last4"

    val expiryDisplay: String
        get() = "${expMonth.toString().padStart(2, '0')}/${(expYear % 100).toString().padStart(2, '0')}"
}
