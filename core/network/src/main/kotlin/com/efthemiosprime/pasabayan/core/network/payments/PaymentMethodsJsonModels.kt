package com.efthemiosprime.pasabayan.core.network.payments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodApiJson(
    val id: String = "",
    val card: CardDetailsJson? = null,
)

@Serializable
data class CardDetailsJson(
    val brand: String = "",
    val last4: String = "",
    @SerialName("exp_month") val expMonth: Int = 0,
    @SerialName("exp_year") val expYear: Int = 0,
)

@Serializable
data class PaymentMethodsResponseJson(
    val success: Boolean = false,
    val data: List<PaymentMethodApiJson> = emptyList(),
)

@Serializable
data class SetupIntentResponseJson(
    val success: Boolean = false,
    val data: SetupIntentDataJson? = null,
)

@Serializable
data class SetupIntentDataJson(
    @SerialName("client_secret") val clientSecret: String = "",
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("ephemeral_key") val ephemeralKey: String = "",
)

@Serializable
data class DefaultPaymentMethodResponseJson(
    val success: Boolean = false,
    val data: DefaultPaymentMethodDataJson? = null,
)

@Serializable
data class DefaultPaymentMethodDataJson(
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
)

@Serializable
data class SetDefaultPaymentMethodResponseJson(
    val success: Boolean = false,
    val message: String? = null,
)

@Serializable
data class DeletePaymentMethodResponseJson(
    val success: Boolean = false,
    val message: String? = null,
)
