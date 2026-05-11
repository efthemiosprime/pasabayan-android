package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.session.TokenStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {

    @Binds
    @Singleton
    abstract fun bindActivityLogger(impl: DefaultActivityLogger): ActivityLogger

    @Binds
    @Singleton
    abstract fun bindHealthCheckService(impl: DefaultHealthCheckService): HealthCheckService

    @Binds
    @Singleton
    abstract fun bindActivityLogDeviceContext(
        impl: AndroidActivityLogDeviceContext,
    ): ActivityLogDeviceContext

    @Binds
    @Singleton
    abstract fun bindActivityLogUserProvider(
        impl: SessionActivityLogUserProvider,
    ): ActivityLogUserProvider
}

/**
 * Default [ActivityLogUserProvider] implementation. The app's session today exposes the
 * [TokenStore] but not a strongly-typed `currentUser`; until that wiring lands, the logger
 * stays a no-op (returns `null`) so submissions are deferred safely. Callers can override
 * this binding in tests via Hilt or replace the binding once user-state plumbing exists.
 */
@Singleton
class SessionActivityLogUserProvider @Inject constructor(
    @Suppress("unused") private val tokenStore: TokenStore,
) : ActivityLogUserProvider {

    @Volatile
    private var snapshot: ActivityLogUser? = null

    /** Call from auth flows to keep [current] in sync with the latest [AuthUser]. */
    fun update(user: AuthUser?, userType: String?) {
        snapshot = user?.takeIf { it.id > 0 }?.let {
            ActivityLogUser(
                id = it.id.toInt(),
                name = it.name,
                email = it.email,
                userType = userType ?: "user",
            )
        }
    }

    fun clear() {
        snapshot = null
    }

    override fun current(): ActivityLogUser? = snapshot
}
