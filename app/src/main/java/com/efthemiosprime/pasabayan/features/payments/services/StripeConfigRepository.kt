package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig

interface StripeConfigRepository {
    suspend fun fetchConfig(): Result<StripeConfig>
}
