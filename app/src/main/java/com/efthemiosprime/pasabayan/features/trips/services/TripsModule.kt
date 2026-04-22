package com.efthemiosprime.pasabayan.features.trips.services

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
annotation class TripsPrefs

@Module
@InstallIn(SingletonComponent::class)
abstract class TripsBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTripsRepository(impl: TripsRepositoryImpl): TripsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object TripsProviderModule {

    @Provides
    @Singleton
    @TripsPrefs
    fun provideTripsSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("pasabayan_trips", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideUsualTransportStore(@TripsPrefs prefs: SharedPreferences): UsualTransportStore =
        UsualTransportStore(prefs)

    @Provides
    @Singleton
    fun provideSavedRouteTemplatesStore(
        @TripsPrefs prefs: SharedPreferences,
        json: Json,
    ): SavedRouteTemplatesStore = SavedRouteTemplatesStore(prefs, json)

    @Provides
    @Singleton
    fun provideCarrierDisclaimerStore(@TripsPrefs prefs: SharedPreferences): CarrierDisclaimerStore =
        CarrierDisclaimerStore(prefs)

    @Provides
    @Singleton
    fun provideCarrierPreferencesFormStore(@TripsPrefs prefs: SharedPreferences): CarrierPreferencesFormStore =
        CarrierPreferencesFormStore(prefs)
}
