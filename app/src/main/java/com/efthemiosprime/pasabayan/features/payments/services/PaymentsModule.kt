package com.efthemiosprime.pasabayan.features.payments.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentsModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindStripeConfigRepository(impl: StripeConfigRepositoryImpl): StripeConfigRepository

    @Binds
    @Singleton
    abstract fun bindStripeConnectRepository(impl: StripeConnectRepositoryImpl): StripeConnectRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodsRepository(impl: PaymentMethodsRepositoryImpl): PaymentMethodsRepository

    @Binds
    @Singleton
    abstract fun bindReceiptRepository(impl: ReceiptRepositoryImpl): ReceiptRepository
}
