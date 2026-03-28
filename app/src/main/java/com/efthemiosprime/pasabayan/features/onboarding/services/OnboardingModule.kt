package com.efthemiosprime.pasabayan.features.onboarding.services

import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingPreferences(
        impl: SharedPreferencesOnboardingPreferences,
    ): OnboardingPreferences

    @Binds
    @Singleton
    abstract fun bindCityOnboardingRepository(
        impl: CityOnboardingRepositoryImpl,
    ): CityOnboardingRepository

    @Binds
    @Singleton
    abstract fun bindConsentOnboardingRepository(
        impl: ConsentOnboardingRepositoryImpl,
    ): ConsentOnboardingRepository
}
