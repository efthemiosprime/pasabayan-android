package com.efthemiosprime.pasabayan.core.network.payments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payments-specific API error payload contract from `06-payments-stripe.md`.
 */
@Serializable
data class PaymentApiErrorResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val errors: Map<String, List<String>>? = null,
    val transaction: TransactionJson? = null,
    @SerialName("client_secret") val clientSecret: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("ephemeral_key") val ephemeralKey: String? = null,
    @SerialName("public_key") val publicKey: String? = null,
)
