package com.efthemiosprime.pasabayan.features.shipper.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShipperModule {
    @Binds
    @Singleton
    abstract fun bindShipperRepository(impl: ShipperRepositoryImpl): ShipperRepository
}
