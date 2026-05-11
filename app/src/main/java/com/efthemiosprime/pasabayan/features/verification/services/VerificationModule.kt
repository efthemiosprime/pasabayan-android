package com.efthemiosprime.pasabayan.features.verification.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VerificationModule {

    @Binds
    @Singleton
    abstract fun bindVerificationRepository(
        impl: VerificationRepositoryImpl,
    ): VerificationRepository
}
