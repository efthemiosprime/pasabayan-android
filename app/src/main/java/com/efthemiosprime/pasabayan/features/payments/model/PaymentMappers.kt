package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodApiJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentReceiptJson
import com.efthemiosprime.pasabayan.core.network.payments.StripeConfigJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson

fun TransactionJson.toDomain(): Transaction {
    val rawStatus = transactionStatus ?: status
    val parsedStatus = try {
        TransactionStatus.valueOf(rawStatus.uppercase())
    } catch (_: Exception) {
        TransactionStatus.UNKNOWN
    }

    return Transaction(
        id = id,
        transactionStatus = parsedStatus,
        deliveryMatchId = deliveryMatchId,
        description = description,
        shipperName = shipper?.name,
        carrierName = carrier?.name,
        totalAmount = amounts?.total ?: 0.0,
        subtotal = amounts?.subtotal,
        platformFee = amounts?.platformFee,
        carrierReceives = amounts?.carrierReceives,
        currency = amounts?.currency ?: "cad",
        tipAmount = amounts?.tip,
        clientSecret = clientSecret,
        customerId = customerId,
        ephemeralKey = ephemeralKey,
        payoutStatus = payoutStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun StripeConfigJson.toDomain(): StripeConfig = StripeConfig(
    mode = mode,
    publicKey = publicKey,
    currency = currency,
    minDeliveryPrice = minDeliveryPrice ?: StripeConfig.DEFAULT_MIN_DELIVERY_PRICE,
    senderFeePercentage = senderFeePercentage ?: StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
    carrierFeePercentage = carrierFeePercentage ?: StripeConfig.DEFAULT_CARRIER_FEE_PERCENTAGE,
    platformFeePercentage = platformFeePercentage ?: StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
)

fun PaymentMethodApiJson.toDomain(isDefault: Boolean = false): PaymentMethodDisplay = PaymentMethodDisplay(
    id = id,
    brand = card?.brand ?: "unknown",
    last4 = card?.last4 ?: "????",
    expMonth = card?.expMonth ?: 0,
    expYear = card?.expYear ?: 0,
    isDefault = isDefault,
)

fun PaymentReceiptJson.toDomain(): PaymentReceipt = PaymentReceipt(
    id = id,
    receiptNumber = receiptNumber,
    receiptUrl = receiptUrl,
    date = date,
    dateFormatted = dateFormatted,
    role = role,
    otherPartyName = otherParty?.name,
    totalAmount = amount?.total ?: 0.0,
    carrierAmount = amount?.carrierAmount,
    platformFee = amount?.platformFee,
    tipAmount = amount?.tip,
    currency = amount?.currency ?: "cad",
    status = status,
    pickupCity = delivery?.pickupCity,
    deliveryCity = delivery?.deliveryCity,
    packageTitle = delivery?.packageTitle,
)
