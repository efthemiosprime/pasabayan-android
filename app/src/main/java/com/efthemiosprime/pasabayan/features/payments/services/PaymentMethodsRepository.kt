package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.network.payments.SetupIntentDataJson
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay

interface PaymentMethodsRepository {
    suspend fun loadPaymentMethods(): Result<List<PaymentMethodDisplay>>
    suspend fun loadDefaultPaymentMethod(): Result<String?>
    suspend fun createSetupIntent(): Result<SetupIntentDataJson>
    suspend fun removePaymentMethod(methodId: String): Result<Unit>
    suspend fun setDefaultPaymentMethod(methodId: String): Result<Unit>
}
