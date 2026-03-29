package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson

interface StripeConnectRepository {
    suspend fun startOnboarding(): Result<String>
    suspend fun checkStatus(): Result<StripeConnectStatusJson>
    suspend fun getDashboardUrl(): Result<String>
}
