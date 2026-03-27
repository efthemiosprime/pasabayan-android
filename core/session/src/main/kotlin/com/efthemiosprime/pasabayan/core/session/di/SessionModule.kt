package com.efthemiosprime.pasabayan.core.session.di

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import com.efthemiosprime.pasabayan.core.network.SessionInvalidationHandler
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthRepositoryImpl
import com.efthemiosprime.pasabayan.core.session.EncryptedTokenStore
import com.efthemiosprime.pasabayan.core.session.StoredAuthTokenProvider
import com.efthemiosprime.pasabayan.core.session.TokenClearingHandler
import com.efthemiosprime.pasabayan.core.session.TokenStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindTokenStore(impl: EncryptedTokenStore): TokenStore

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(impl: StoredAuthTokenProvider): AuthTokenProvider

    @Binds
    @Singleton
    abstract fun bindSessionInvalidation(impl: TokenClearingHandler): SessionInvalidationHandler

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
