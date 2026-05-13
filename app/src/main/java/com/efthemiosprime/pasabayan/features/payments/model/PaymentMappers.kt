package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.network.payments.OtherPartyJson
import com.efthemiosprime.pasabayan.core.network.payments.PayoutJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodApiJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentReceiptJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptAmountJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptDeliveryJson
import com.efthemiosprime.pasabayan.core.network.payments.RefundInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import com.efthemiosprime.pasabayan.core.network.payments.StripeConfigJson
import com.efthemiosprime.pasabayan.core.network.payments.StripeInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.TipInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionAmountsJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionTimestampsJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionUserJson
import kotlin.math.max

fun TransactionJson.toDomain(): Transaction {
    val rawStatus = transactionStatus ?: status
    val parsedStatus = runCatching {
        TransactionStatus.valueOf(rawStatus.uppercase())
    }.getOrDefault(TransactionStatus.UNKNOWN)

    val parsedPayoutStatus = payoutStatus.toPayoutStatusOrNull()

    val builtTimestamps = timestamps?.toDomain()
        ?: TransactionTimestamps(
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: createdAt ?: "",
            payoutCompletedAt = payoutCompletedAt,
        )

    return Transaction(
        id = id,
        shipper = shipper?.toDomain(),
        carrier = carrier?.toDomain(),
        deliveryMatchId = deliveryMatchId,
        amounts = amounts?.toDomain(),
        stripe = stripe?.toDomain(),
        transactionStatus = parsedStatus,
        description = description,
        metadata = metadata,
        refund = refund?.toDomain(),
        timestamps = builtTimestamps,
        clientSecret = clientSecret,
        payoutStatus = parsedPayoutStatus,
        payoutNotes = payoutNotes,
        payoutCompletedAt = payoutCompletedAt,
        payout = payout?.toDomain(),
        tip = tip?.toDomain(),
        customerId = customerId,
        ephemeralKey = ephemeralKey,
    )
}

fun TransactionUserJson.toDomain() = TransactionUser(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
)

fun TransactionAmountsJson.toDomain(): TransactionAmounts {
    val resolvedTotal = total ?: 0.0
    val resolvedPlatformFee = platformFee ?: 0.0
    val resolvedTip = tip ?: 0.0
    val resolvedCarrierReceives = carrierReceives
        ?: max(0.0, resolvedTotal - resolvedPlatformFee)
    return TransactionAmounts(
        total = resolvedTotal,
        subtotal = subtotal,
        platformFee = resolvedPlatformFee,
        carrierReceives = resolvedCarrierReceives,
        currency = currency,
        tip = resolvedTip,
        tax = tax,
        carrierTotal = carrierTotal ?: (resolvedCarrierReceives + resolvedTip),
        baseAmount = baseAmount,
    )
}

fun StripeInfoJson.toDomain() = StripeInfo(
    paymentIntentId = paymentIntentId,
    transferId = transferId,
    chargeId = chargeId,
    refundId = refundId,
)

fun RefundInfoJson.toDomain() = RefundInfo(
    amount = amount,
    reason = reason,
    refundedAt = refundedAt,
)

fun PayoutJson.toDomain() = Payout(
    status = status.toPayoutStatusOrNull(),
    notes = notes,
    completedAt = completedAt,
)

fun TipInfoJson.toDomain() = TipInfo(
    amount = amount ?: 0.0,
    paidAt = paidAt,
    stripePaymentIntentId = stripePaymentIntentId,
)

fun TransactionTimestampsJson.toDomain() = TransactionTimestamps(
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: createdAt ?: "",
    authorizedAt = authorizedAt,
    capturedAt = capturedAt,
    completedAt = completedAt,
    failedAt = failedAt,
    payoutCompletedAt = payoutCompletedAt,
)

private fun String?.toPayoutStatusOrNull(): PayoutStatus? = this?.let { raw ->
    runCatching { PayoutStatus.valueOf(raw.uppercase()) }.getOrNull()
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
    otherParty = otherParty?.toDomain(),
    amount = amount?.toDomain() ?: PaymentReceiptAmount(total = 0.0),
    delivery = delivery?.toDomain(),
    status = status,
)

fun OtherPartyJson.toDomain() = OtherParty(
    id = id,
    name = name,
    verificationLevel = verificationLevel,
)

fun ReceiptAmountJson.toDomain() = PaymentReceiptAmount(
    total = total,
    carrierAmount = carrierAmount,
    platformFee = platformFee,
    tip = tip,
    currency = currency,
)

fun ReceiptDeliveryJson.toDomain() = PaymentReceiptDelivery(
    pickupCity = pickupCity,
    deliveryCity = deliveryCity,
    packageTitle = packageTitle,
)

fun RefundRequestDataJson.toDomain(): RefundRequest = RefundRequest(
    id = id,
    transactionId = transactionId,
    status = status.toRefundStatusOrDefault(),
    amount = amount,
    reason = reason.toRefundReasonOrNull(),
    reasonText = reason,
    description = description,
    adminNotes = adminNotes,
    reviewedAt = reviewedAt,
    processedAt = processedAt,
    createdAt = createdAt,
)

/**
 * iOS sends the preset reason's *display text* as the wire string (e.g.
 * "Package was damaged during delivery"). Match by displayText to recover the enum;
 * unmatched text (OTHER, custom) returns null and the caller keeps `reasonText`.
 */
private fun String?.toRefundReasonOrNull(): RefundReason? = this?.let { raw ->
    RefundReason.entries.firstOrNull { it.displayText == raw }
}

private fun String.toRefundStatusOrDefault(): RefundStatus = runCatching {
    RefundStatus.valueOf(uppercase())
}.getOrDefault(RefundStatus.PENDING)
