package com.efthemiosprime.pasabayan.features.notifications.model

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import java.util.concurrent.TimeUnit

/**
 * Localized relative-time formatter for notification timestamps. Mirrors iOS `timeAgo`.
 *
 * Pure with an injectable clock so tests don't depend on system time.
 */
object TimeAgoFormatter {

    fun format(
        context: Context,
        isoTimestamp: String?,
        nowMillis: Long = System.currentTimeMillis(),
    ): String {
        val parsed = DateTimeParsing.parseApiDateTime(isoTimestamp)
            ?: return context.getString(R.string.notifications_time_just_now)
        return formatMillis(context, parsed, nowMillis)
    }

    fun formatMillis(context: Context, eventMillis: Long, nowMillis: Long): String {
        val deltaMillis = (nowMillis - eventMillis).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(deltaMillis).toInt()
        val hours = TimeUnit.MILLISECONDS.toHours(deltaMillis).toInt()
        val days = TimeUnit.MILLISECONDS.toDays(deltaMillis).toInt()
        return when {
            minutes < 1 -> context.getString(R.string.notifications_time_just_now)
            minutes < 60 -> context.getString(R.string.notifications_time_minutes_ago, minutes)
            hours < 24 -> context.getString(R.string.notifications_time_hours_ago, hours)
            else -> context.getString(R.string.notifications_time_days_ago, days)
        }
    }
}
