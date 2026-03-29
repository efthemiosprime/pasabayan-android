package com.efthemiosprime.pasabayan.core.network.payments

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeConfigJson(
    val mode: String = "sandbox",
    @SerialName("public_key") val publicKey: String = "",
    val currency: String = "cad",
    @SerialName("min_delivery_price") @Serializable(with = FlexibleDoubleSerializer::class) val minDeliveryPrice: Double? = null,
    @SerialName("tax_enabled") val taxEnabled: Boolean = false,
    @SerialName("sender_fee_percentage") @Serializable(with = FlexibleDoubleSerializer::class) val senderFeePercentage: Double? = null,
    @SerialName("carrier_fee_percentage") @Serializable(with = FlexibleDoubleSerializer::class) val carrierFeePercentage: Double? = null,
    @SerialName("platform_fee_percentage") @Serializable(with = FlexibleDoubleSerializer::class) val platformFeePercentage: Double? = null,
)

@Serializable
data class StripeConfigResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: StripeConfigJson? = null,
)
