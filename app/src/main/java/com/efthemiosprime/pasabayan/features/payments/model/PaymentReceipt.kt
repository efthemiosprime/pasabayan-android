package com.efthemiosprime.pasabayan.features.payments.model

data class PaymentReceipt(
    val id: Int,
    val receiptNumber: String,
    val receiptUrl: String?,
    val date: String,
    val dateFormatted: String?,
    val role: String,
    val otherPartyName: String?,
    val totalAmount: Double,
    val carrierAmount: Double?,
    val platformFee: Double?,
    val tipAmount: Double?,
    val currency: String,
    val status: String,
    val pickupCity: String?,
    val deliveryCity: String?,
    val packageTitle: String?,
) {
    val isShipper: Boolean get() = role == "shipper"

    val routeDescription: String
        get() {
            if (pickupCity != null && deliveryCity != null) return "$pickupCity → $deliveryCity"
            return otherPartyName ?: ""
        }

    val displayAmount: String
        get() = String.format("$%.2f", totalAmount)
}
