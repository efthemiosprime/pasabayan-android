package com.efthemiosprime.pasabayan.features.bookings.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingsModule {

    @Binds
    @Singleton
    abstract fun bindBookingsRepository(impl: BookingsRepositoryImpl): BookingsRepository

    @Binds
    @Singleton
    abstract fun bindMatchReceiptRepository(impl: MatchReceiptRepositoryImpl): MatchReceiptRepository
}
