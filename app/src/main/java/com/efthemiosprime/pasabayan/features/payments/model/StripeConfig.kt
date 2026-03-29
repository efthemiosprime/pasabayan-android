package com.efthemiosprime.pasabayan.features.payments.model

data class StripeConfig(
    val mode: String,
    val publicKey: String,
    val currency: String,
    val minDeliveryPrice: Double,
    val senderFeePercentage: Double,
    val carrierFeePercentage: Double,
    val platformFeePercentage: Double,
) {
    val isSandbox: Boolean get() = mode == "sandbox"
    val isLive: Boolean get() = mode == "live"

    fun totalToPay(baseAmount: Double): Double =
        baseAmount * (1 + senderFeePercentage / 100)

    fun serviceFeeAmount(baseAmount: Double): Double =
        baseAmount * senderFeePercentage / 100

    fun carrierReceives(baseAmount: Double): Double =
        baseAmount * (1 - carrierFeePercentage / 100)

    fun validateMinimumPrice(price: Double): String? =
        if (price < minDeliveryPrice) "Minimum price is $${String.format("%.2f", minDeliveryPrice)}" else null

    fun validateProposedPrice(price: Double): String? {
        val minError = validateMinimumPrice(price)
        if (minError != null) return minError
        if (price > MAX_PROPOSED_PRICE) return "Maximum price is $${String.format("%.2f", MAX_PROPOSED_PRICE)}"
        return null
    }

    companion object {
        const val MAX_PROPOSED_PRICE = 9999.99
        const val DEFAULT_MIN_DELIVERY_PRICE = 5.00
        const val DEFAULT_SENDER_FEE_PERCENTAGE = 10.0
        const val DEFAULT_CARRIER_FEE_PERCENTAGE = 5.0
    }
}
