package com.efthemiosprime.pasabayan.features.packages.services

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PackagesPrefs

@Module
@InstallIn(SingletonComponent::class)
abstract class PackagesBindingsModule {

    @Binds
    @Singleton
    abstract fun bindPackagesRepository(impl: PackagesRepositoryImpl): PackagesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PackagesProviderModule {

    @Provides
    @Singleton
    @PackagesPrefs
    fun providePackagesSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("pasabayan_packages", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideShipperDisclaimerStore(@PackagesPrefs prefs: SharedPreferences): ShipperDisclaimerStore =
        ShipperDisclaimerStore(prefs)

    @Provides
    @Singleton
    fun provideSavedPackageDescriptionsStore(
        @PackagesPrefs prefs: SharedPreferences,
        json: Json,
    ): SavedPackageDescriptionsStore = SavedPackageDescriptionsStore(prefs, json)

    @Provides
    @Singleton
    fun provideSavedPackageRouteTemplatesStore(
        @PackagesPrefs prefs: SharedPreferences,
        json: Json,
    ): SavedPackageRouteTemplatesStore = SavedPackageRouteTemplatesStore(prefs, json)
}
