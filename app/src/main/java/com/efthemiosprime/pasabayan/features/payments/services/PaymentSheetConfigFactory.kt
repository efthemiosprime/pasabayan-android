package com.efthemiosprime.pasabayan.features.payments.services

import com.stripe.android.paymentsheet.PaymentSheet

/**
 * Builds Stripe [PaymentSheet.Configuration] from primitives. Shared between
 * [PaymentViewModel] (pay flow) and [TippingViewModel] (post-delivery tip flow).
 * iOS mirror: PaymentViewModel.swift `configurePaymentSheet`.
 */
object PaymentSheetConfigFactory {

    fun build(
        customerId: String?,
        ephemeralKey: String?,
        stripeIsSandbox: Boolean,
        currencyCode: String,
        merchantName: String = "Pasabayan",
        countryCode: String = "CA",
    ): PaymentSheet.Configuration {
        val customerConfig = customerConfigOrNull(customerId, ephemeralKey)
        if (customerConfig == null) {
            return PaymentSheet.Configuration(merchantName)
        }
        val googlePay = PaymentSheet.GooglePayConfiguration(
            environment = if (stripeIsSandbox) {
                PaymentSheet.GooglePayConfiguration.Environment.Test
            } else {
                PaymentSheet.GooglePayConfiguration.Environment.Production
            },
            countryCode = countryCode,
            currencyCode = currencyCode,
        )
        return PaymentSheet.Configuration(
            merchantName,
            customerConfig,
            googlePay,
        )
    }

    private fun customerConfigOrNull(
        customerId: String?,
        ephemeralKey: String?,
    ): PaymentSheet.CustomerConfiguration? {
        if (customerId.isNullOrBlank() || ephemeralKey.isNullOrBlank()) return null
        return PaymentSheet.CustomerConfiguration(
            id = customerId,
            ephemeralKeySecret = ephemeralKey,
        )
    }
}
