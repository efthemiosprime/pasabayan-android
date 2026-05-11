package com.efthemiosprime.pasabayan.features.locations.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationsModule {

    @Binds
    @Singleton
    abstract fun bindLocationCatalogRepository(
        impl: LocationCatalogRepositoryImpl,
    ): LocationCatalogRepository

    @Binds
    @Singleton
    abstract fun bindLocationCatalogStore(
        impl: SharedPreferencesLocationCatalogStore,
    ): LocationCatalogStore
}
