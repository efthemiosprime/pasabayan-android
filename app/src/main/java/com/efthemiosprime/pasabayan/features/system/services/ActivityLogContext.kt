package com.efthemiosprime.pasabayan.features.system.services

/**
 * Per-user context required to populate an `/activity-logs` payload. The
 * [com.efthemiosprime.pasabayan.features.system.services.ActivityLogger] pulls this from the
 * session each time it submits.
 */
data class ActivityLogUser(
    val id: Int,
    val name: String,
    val email: String,
    val userType: String,
) {
    val isLoggable: Boolean get() = id > 0
}

/** Resolves the current authenticated user (or `null` when no session is present). */
interface ActivityLogUserProvider {
    fun current(): ActivityLogUser?
}
