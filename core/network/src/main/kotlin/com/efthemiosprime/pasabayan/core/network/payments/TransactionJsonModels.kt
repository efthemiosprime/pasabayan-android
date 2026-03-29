package com.efthemiosprime.pasabayan.core.network.payments

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionJson(
    val id: Int,
    val shipper: TransactionUserJson? = null,
    val carrier: TransactionUserJson? = null,
    @SerialName("delivery_match_id") val deliveryMatchId: Int? = null,
    val amounts: TransactionAmountsJson? = null,
    val stripe: StripeInfoJson? = null,
    val status: String = "unknown",
    val description: String? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("payout_status") val payoutStatus: String? = null,
    @SerialName("payout_notes") val payoutNotes: String? = null,
    @SerialName("payout_completed_at") val payoutCompletedAt: String? = null,
    val payout: PayoutJson? = null,
    val tip: TipInfoJson? = null,
    val refund: RefundInfoJson? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("ephemeral_key") val ephemeralKey: String? = null,
    @SerialName("transaction_status") val transactionStatus: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class TransactionUserJson(
    val id: Int,
    val name: String = "",
    val email: String? = null,
    val avatar: String? = null,
)

@Serializable
data class TransactionAmountsJson(
    @Serializable(with = FlexibleDoubleSerializer::class) val total: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val subtotal: Double? = null,
    @SerialName("platform_fee") @Serializable(with = FlexibleDoubleSerializer::class) val platformFee: Double? = null,
    @SerialName("carrier_receives") @Serializable(with = FlexibleDoubleSerializer::class) val carrierReceives: Double? = null,
    val currency: String = "cad",
    @Serializable(with = FlexibleDoubleSerializer::class) val tip: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val tax: Double? = null,
    @SerialName("carrier_total") @Serializable(with = FlexibleDoubleSerializer::class) val carrierTotal: Double? = null,
    @SerialName("base_amount") @Serializable(with = FlexibleDoubleSerializer::class) val baseAmount: Double? = null,
)

@Serializable
data class StripeInfoJson(
    @SerialName("payment_intent_id") val paymentIntentId: String? = null,
    @SerialName("transfer_id") val transferId: String? = null,
    @SerialName("charge_id") val chargeId: String? = null,
    @SerialName("refund_id") val refundId: String? = null,
)

@Serializable
data class RefundInfoJson(
    @Serializable(with = FlexibleDoubleSerializer::class) val amount: Double? = null,
    val reason: String? = null,
    @SerialName("refunded_at") val refundedAt: String? = null,
)

@Serializable
data class PayoutJson(
    val status: String? = null,
    val notes: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
)

@Serializable
data class TipInfoJson(
    @Serializable(with = FlexibleDoubleSerializer::class) val amount: Double? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("stripe_payment_intent_id") val stripePaymentIntentId: String? = null,
)

// -- Request/Response types --

@Serializable
data class CreatePaymentRequestJson(
    @SerialName("delivery_match_id") val deliveryMatchId: Int,
    val amount: Double,
    val currency: String = "cad",
)

@Serializable
data class CreatePaymentResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: TransactionJson? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("ephemeral_key") val ephemeralKey: String? = null,
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("default_payment_method_id") val defaultPaymentMethodId: String? = null,
)

@Serializable
data class TransactionListResponseJson(
    val success: Boolean = false,
    val data: List<TransactionJson> = emptyList(),
)

@Serializable
data class TransactionResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: TransactionJson? = null,
)

@Serializable
data class PaymentActionResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: TransactionJson? = null,
)

@Serializable
data class ConfirmCaptureRequestJson(
    @SerialName("delivery_match_id") val deliveryMatchId: Int,
)

@Serializable
data class RefundRequestBodyJson(
    val reason: String,
    val amount: Double? = null,
    val description: String? = null,
)

@Serializable
data class RefundRequestDataJson(
    val id: Int = 0,
    @SerialName("transaction_id") val transactionId: Int? = null,
    @Serializable(with = FlexibleDoubleSerializer::class) val amount: Double? = null,
    val reason: String? = null,
    val description: String? = null,
    val status: String? = null,
    @SerialName("admin_notes") val adminNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class RefundStatusResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: RefundRequestDataJson? = null,
)

@Serializable
data class CancelRequestJson(
    val reason: String? = null,
)

@Serializable
data class TipRequestJson(
    val amount: Double,
)

@Serializable
data class TipResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: TransactionJson? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
)
