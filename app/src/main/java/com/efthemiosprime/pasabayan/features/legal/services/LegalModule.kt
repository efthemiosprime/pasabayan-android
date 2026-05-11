package com.efthemiosprime.pasabayan.features.legal.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LegalModule {
    @Binds
    @Singleton
    abstract fun bindLegalRepository(impl: LegalRepositoryImpl): LegalRepository
}
