package com.efthemiosprime.pasabayan.features.profile.services

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import java.time.Clock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ProfilePrefs

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileBindingsModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindProfileAttentionRepository(
        impl: ProfileAttentionRepositoryImpl,
    ): ProfileAttentionRepository

    @Binds
    @Singleton
    abstract fun bindBadgeSummaryRepository(
        impl: BadgeSummaryRepositoryImpl,
    ): BadgeSummaryRepository

    @Binds
    @Singleton
    abstract fun bindImageCompressor(impl: BitmapImageCompressor): ImageCompressor
}

@Module
@InstallIn(SingletonComponent::class)
object ProfileProviderModule {

    @Provides
    @Singleton
    @ProfilePrefs
    fun provideProfileSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("pasabayan_profile", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePreferredCurrencyStore(@ProfilePrefs prefs: SharedPreferences): PreferredCurrencyStore =
        PreferredCurrencyStore(prefs)

    /**
     * Process-wide lifecycle for the badge summary observer. Pulled from a
     * provider so tests can inject a `LifecycleRegistry` directly.
     */
    @Provides
    @Singleton
    fun provideProcessLifecycle(): Lifecycle = ProcessLifecycleOwner.get().lifecycle

    @Provides
    @Singleton
    fun provideSystemClock(): Clock = Clock.systemUTC()
}
