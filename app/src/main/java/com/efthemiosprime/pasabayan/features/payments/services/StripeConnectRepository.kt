package com.efthemiosprime.pasabayan.features.payments.services

import com.efthemiosprime.pasabayan.features.payments.model.StripeConnectStatus

interface StripeConnectRepository {
    suspend fun startOnboarding(): Result<String>
    suspend fun checkStatus(): Result<StripeConnectStatus>
    suspend fun getDashboardUrl(): Result<String>
}
