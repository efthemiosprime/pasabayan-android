package com.efthemiosprime.pasabayan.core.network.payments

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject

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
    val metadata: Map<String, JsonElement>? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("payout_status") val payoutStatus: String? = null,
    @SerialName("payout_notes") val payoutNotes: String? = null,
    @SerialName("payout_completed_at") val payoutCompletedAt: String? = null,
    val payout: PayoutJson? = null,
    val tip: TipInfoJson? = null,
    val refund: RefundInfoJson? = null,
    val timestamps: TransactionTimestampsJson? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("ephemeral_key") val ephemeralKey: String? = null,
    @SerialName("transaction_status") val transactionStatus: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class TransactionTimestampsJson(
    @SerialName("authorized_at") val authorizedAt: String? = null,
    @SerialName("captured_at") val capturedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("failed_at") val failedAt: String? = null,
    @SerialName("payout_completed_at") val payoutCompletedAt: String? = null,
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

@Serializable(with = CreatePaymentResponseJsonSerializer::class)
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
private data class CreatePaymentResponseJsonSurrogate(
    val success: Boolean = false,
    val message: String? = null,
    val data: TransactionJson? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("ephemeral_key") val ephemeralKey: String? = null,
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("default_payment_method_id") val defaultPaymentMethodId: String? = null,
)

/**
 * iOS parity: read `client_secret` from top-level first, then `data.client_secret`.
 */
object CreatePaymentResponseJsonSerializer : KSerializer<CreatePaymentResponseJson> {
    override val descriptor: SerialDescriptor =
        CreatePaymentResponseJsonSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): CreatePaymentResponseJson {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("CreatePaymentResponseJson supports JSON only")
        val element = jsonDecoder.decodeJsonElement()
        val surrogate = jsonDecoder.json.decodeFromJsonElement(
            CreatePaymentResponseJsonSurrogate.serializer(),
            element.jsonObject,
        )
        val resolvedClientSecret = surrogate.clientSecret ?: surrogate.data?.clientSecret
        return CreatePaymentResponseJson(
            success = surrogate.success,
            message = surrogate.message,
            data = surrogate.data,
            clientSecret = resolvedClientSecret,
            customerId = surrogate.customerId,
            ephemeralKey = surrogate.ephemeralKey,
            publicKey = surrogate.publicKey,
            defaultPaymentMethodId = surrogate.defaultPaymentMethodId,
        )
    }

    override fun serialize(encoder: Encoder, value: CreatePaymentResponseJson) {
        val surrogate = CreatePaymentResponseJsonSurrogate(
            success = value.success,
            message = value.message,
            data = value.data,
            clientSecret = value.clientSecret ?: value.data?.clientSecret,
            customerId = value.customerId,
            ephemeralKey = value.ephemeralKey,
            publicKey = value.publicKey,
            defaultPaymentMethodId = value.defaultPaymentMethodId,
        )
        encoder.encodeSerializableValue(CreatePaymentResponseJsonSurrogate.serializer(), surrogate)
    }
}

/**
 * Dual-shape decoder for `/payments`:
 * - **Paginated envelope (new contract):** `{success, message, data: {current_page, data: [...], ...}}`
 *   (Laravel paginator). Strip the envelope and surface the inner array.
 * - **Legacy flat shape:** `{success, data: [...]}`. Use as-is.
 *
 * Mirrors iOS `TransactionListResponse` in `PaymentModels.swift` — keep both branches
 * until the backend cut-over is finalised, then drop the legacy fall-back.
 */
@Serializable(with = TransactionListResponseJsonSerializer::class)
data class TransactionListResponseJson(
    val success: Boolean = false,
    val data: List<TransactionJson> = emptyList(),
)

@Serializable
private data class TransactionListResponseJsonSurrogate(
    val success: Boolean = false,
    val data: List<TransactionJson> = emptyList(),
)

object TransactionListResponseJsonSerializer : KSerializer<TransactionListResponseJson> {
    override val descriptor: SerialDescriptor =
        TransactionListResponseJsonSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): TransactionListResponseJson {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("TransactionListResponseJson supports JSON only")
        val root = jsonDecoder.decodeJsonElement().jsonObject
        val success = (root["success"] as? JsonPrimitive)?.booleanOrNull ?: true
        val dataElement = root["data"]

        // Paginated envelope: `data` is an object containing a nested `data` array.
        if (dataElement is JsonObject) {
            val inner = dataElement["data"] as? JsonArray ?: JsonArray(emptyList())
            val items = inner.map {
                jsonDecoder.json.decodeFromJsonElement(TransactionJson.serializer(), it)
            }
            return TransactionListResponseJson(success = success, data = items)
        }

        // Legacy flat shape: `data` is the array itself (or missing).
        val items = (dataElement as? JsonArray)?.map {
            jsonDecoder.json.decodeFromJsonElement(TransactionJson.serializer(), it)
        } ?: emptyList()
        return TransactionListResponseJson(success = success, data = items)
    }

    override fun serialize(encoder: Encoder, value: TransactionListResponseJson) {
        encoder.encodeSerializableValue(
            TransactionListResponseJsonSurrogate.serializer(),
            TransactionListResponseJsonSurrogate(value.success, value.data),
        )
    }
}

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
    @SerialName("transaction_id") val transactionId: Int = 0,
    @Serializable(with = FlexibleDoubleSerializer::class) val amount: Double? = null,
    val reason: String? = null,
    val description: String? = null,
    val status: String = "",
    @SerialName("admin_notes") val adminNotes: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("processed_at") val processedAt: String? = null,
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
