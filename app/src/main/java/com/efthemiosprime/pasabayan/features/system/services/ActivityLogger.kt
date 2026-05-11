package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.network.system.ActivityLogJson
import com.efthemiosprime.pasabayan.core.network.system.SystemApi
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationAppScope
import com.efthemiosprime.pasabayan.features.system.model.ActivityLogType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fire-and-forget submitter for `POST /activity-logs`. iOS parity: `ActivityLogger.shared`.
 *
 * Drops the call silently when:
 *  - no authenticated user is present, OR
 *  - the resolved user has `id <= 0`.
 *
 * Submissions are queued through a single coroutine on [NotificationAppScope] so they don't
 * pile up requests in flight (the backend is rate-limited and per-log priority is "best
 * effort"). The scope already uses `Dispatchers.IO` + a `SupervisorJob` so a per-call failure
 * doesn't cancel the rest.
 */
interface ActivityLogger {
    fun log(
        action: String,
        description: String,
        logType: ActivityLogType,
        subjectType: String? = null,
        subjectId: Int? = null,
        properties: Map<String, String>? = null,
    )

    // -- iOS-parity convenience methods --

    fun logUserAction(action: String, description: String, subjectId: Int? = null) =
        log(action, description, ActivityLogType.USER, subjectId = subjectId)

    fun logPackageAction(action: String, description: String, packageId: Int) =
        log(action, description, ActivityLogType.PACKAGE, subjectType = "PackageRequest", subjectId = packageId)

    fun logTripAction(action: String, description: String, tripId: Int) =
        log(action, description, ActivityLogType.TRIP, subjectType = "CarrierTrip", subjectId = tripId)

    fun logMatchAction(action: String, description: String, matchId: Int) =
        log(action, description, ActivityLogType.MATCH, subjectType = "DeliveryMatch", subjectId = matchId)

    fun logTransactionAction(action: String, description: String, transactionId: Int) =
        log(action, description, ActivityLogType.TRANSACTION, subjectType = "Transaction", subjectId = transactionId)

    fun logRatingAction(action: String, description: String, ratingId: Int) =
        log(action, description, ActivityLogType.RATING, subjectType = "Rating", subjectId = ratingId)

    fun logSystemAction(action: String, description: String) =
        log(action, description, ActivityLogType.SYSTEM)
}

@Singleton
class DefaultActivityLogger @Inject constructor(
    private val systemApi: SystemApi,
    private val userProvider: ActivityLogUserProvider,
    private val deviceContext: ActivityLogDeviceContext,
    private val appScope: NotificationAppScope,
) : ActivityLogger {

    private val queue = Channel<ActivityLogJson>(capacity = QUEUE_CAPACITY)

    init {
        appScope.scope.launch {
            queue.consumeAsFlow().collect { entry ->
                runCatching { systemApi.logActivity(entry) }
            }
        }
    }

    override fun log(
        action: String,
        description: String,
        logType: ActivityLogType,
        subjectType: String?,
        subjectId: Int?,
        properties: Map<String, String>?,
    ) {
        val user = userProvider.current() ?: return
        if (!user.isLoggable) return
        val entry = ActivityLogJson(
            userId = user.id,
            userName = user.name,
            userEmail = user.email,
            userType = user.userType,
            action = action,
            description = description,
            logType = logType.rawValue,
            subjectType = subjectType,
            subjectId = subjectId,
            properties = properties,
            ipAddress = deviceContext.ipAddress,
            userAgent = deviceContext.userAgent,
        )
        queue.trySend(entry) // Drops silently when the buffer is full — diagnostics, not data.
    }

    companion object {
        /** Buffer is bounded so a stuck network doesn't grow memory unboundedly. */
        const val QUEUE_CAPACITY = 64
    }
}
