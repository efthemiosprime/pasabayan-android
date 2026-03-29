package com.efthemiosprime.pasabayan.core.network.payments

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleNotNullSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentReceiptJson(
    val id: Int,
    @SerialName("receipt_number") val receiptNumber: String = "",
    @SerialName("receipt_url") val receiptUrl: String? = null,
    val date: String = "",
    @SerialName("date_formatted") val dateFormatted: String? = null,
    val role: String = "",
    @SerialName("other_party") val otherParty: OtherPartyJson? = null,
    val amount: ReceiptAmountJson? = null,
    val status: String = "",
    val delivery: ReceiptDeliveryJson? = null,
)

@Serializable
data class OtherPartyJson(
    val id: Int = 0,
    val name: String = "",
    @SerialName("verification_level") val verificationLevel: String? = null,
)

@Serializable
data class ReceiptAmountJson(
    @Serializable(with = FlexibleDoubleNotNullSerializer::class) val total: Double = 0.0,
    @SerialName("carrier_amount") @Serializable(with = FlexibleDoubleSerializer::class) val carrierAmount: Double? = null,
    @SerialName("platform_fee") @Serializable(with = FlexibleDoubleSerializer::class) val platformFee: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val tip: Double? = null,
    val currency: String = "cad",
)

@Serializable
data class ReceiptDeliveryJson(
    @SerialName("pickup_city") val pickupCity: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("package_title") val packageTitle: String? = null,
)

@Serializable
data class PaymentReceiptListResponseJson(
    val success: Boolean = false,
    val data: List<PaymentReceiptJson> = emptyList(),
    val meta: ReceiptPaginationMetaJson? = null,
    val message: String? = null,
)

@Serializable
data class ReceiptPaginationMetaJson(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    val total: Int = 0,
)

@Serializable
data class SinglePaymentReceiptResponseJson(
    val success: Boolean = false,
    val data: PaymentReceiptJson? = null,
    val message: String? = null,
)
