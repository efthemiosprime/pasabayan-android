package com.efthemiosprime.pasabayan.features.system.model

/**
 * Canonical `logType` values accepted by `POST /activity-logs`. iOS parity:
 * `ActivityLogType` in `ActivityLogger.swift`.
 */
enum class ActivityLogType(val rawValue: String) {
    USER("user"),
    PACKAGE("package"),
    TRIP("trip"),
    MATCH("match"),
    TRANSACTION("transaction"),
    RATING("rating"),
    SYSTEM("system"),
    CHAT("chat"),
    PREMIUM("premium"),
    AUTH("auth"),
    PAYMENT("payment"),
    BOOKING("booking"),
}
