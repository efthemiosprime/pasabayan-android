package com.efthemiosprime.pasabayan.onboarding

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
}
