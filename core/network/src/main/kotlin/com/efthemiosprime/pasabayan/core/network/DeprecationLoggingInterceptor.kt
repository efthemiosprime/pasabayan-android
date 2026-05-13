package com.efthemiosprime.pasabayan.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tripwire abstraction so the interceptor stays JVM-testable without
 * pulling in Robolectric to capture `android.util.Log`.
 */
fun interface DeprecationLogger {
    fun warn(message: String)
}

/**
 * Production binding. Emits a single `Log.w` per deprecated response on
 * debug builds; no-op in release so the cost is exactly one header read
 * per response in production.
 */
@Singleton
class AndroidDeprecationLogger @Inject constructor() : DeprecationLogger {
    override fun warn(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message)
        }
    }

    private companion object {
        const val TAG = "DeprecatedAPI"
    }
}

/**
 * Logs a warning when a response carries `Deprecation: true`, including the
 * `Link` successor-version value so the team notices unintended legacy
 * endpoint usage. Mirrors the iOS DEBUG-only watcher behavior.
 *
 * Installed in all builds; the side effect is gated by [AndroidDeprecationLogger]
 * which no-ops on release. Header parsing is cheap, so leaving the
 * interceptor in place in production is fine.
 */
class DeprecationLoggingInterceptor @Inject constructor(
    private val logger: DeprecationLogger,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.header(HEADER_DEPRECATION).equals("true", ignoreCase = true)) {
            val request = response.request
            val link = response.header(HEADER_LINK)
            logger.warn(
                buildString {
                    append("Deprecated endpoint hit: ")
                    append(request.method)
                    append(' ')
                    append(request.url)
                    if (link != null) {
                        append(" — Link: ")
                        append(link)
                    }
                },
            )
        }
        return response
    }

    private companion object {
        const val HEADER_DEPRECATION = "Deprecation"
        const val HEADER_LINK = "Link"
    }
}
