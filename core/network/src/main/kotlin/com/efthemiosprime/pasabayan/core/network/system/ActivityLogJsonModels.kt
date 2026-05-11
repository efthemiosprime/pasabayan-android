package com.efthemiosprime.pasabayan.core.network.system

import kotlinx.serialization.Serializable

/**
 * Request body for `POST /activity-logs`. Wire keys are **camelCase** (no `@SerialName` mapping
 * needed) — that's what the backend expects per iOS `ActivityLogData`.
 *
 * Backend constraints worth knowing:
 *  - `ipAddress` is capped at 45 chars (`Android_{first 8 of device id}` fits comfortably).
 *  - `userId` must be `> 0`; the [com.efthemiosprime.pasabayan.features.system.services
 *    .ActivityLogger] skips submission when the current user has no id.
 */
@Serializable
data class ActivityLogJson(
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val userType: String,
    val action: String,
    val description: String,
    val logType: String,
    val subjectType: String? = null,
    val subjectId: Int? = null,
    val properties: Map<String, String>? = null,
    val ipAddress: String,
    val userAgent: String,
)

@Serializable
data class ActivityLogResponseJson(
    val success: Boolean = false,
    val message: String? = null,
)

@Serializable
data class HealthCheckResponseJson(
    val success: Boolean = false,
    val status: String? = null,
    val message: String? = null,
)
