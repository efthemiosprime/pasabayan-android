package com.efthemiosprime.pasabayan.features.shipper.services

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShipperPrefs

@Module
@InstallIn(SingletonComponent::class)
abstract class ShipperBindingsModule {
    @Binds
    @Singleton
    abstract fun bindShipperRepository(impl: ShipperRepositoryImpl): ShipperRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ShipperProviderModule {

    @Provides
    @Singleton
    @ShipperPrefs
    fun provideShipperSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("pasabayan_shipper", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideRecentSearchStore(
        @ShipperPrefs prefs: SharedPreferences,
        json: Json,
    ): RecentSearchStore = RecentSearchStore(prefs, json)
}
