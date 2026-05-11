package com.efthemiosprime.pasabayan.features.ratings.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RatingsModule {

    @Binds
    @Singleton
    abstract fun bindRatingsRepository(impl: RatingsRepositoryImpl): RatingsRepository
}
