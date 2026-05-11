package com.efthemiosprime.pasabayan.features.notifications.model

import android.content.Context
import com.efthemiosprime.pasabayan.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimeAgoFormatterTest {

    private val context = mockk<Context>().apply {
        every { getString(R.string.notifications_time_just_now) } returns "Just now"
        every { getString(R.string.notifications_time_minutes_ago, *anyVararg()) } answers {
            "${(invocation.args[1] as Array<*>)[0]} minutes ago"
        }
        every { getString(R.string.notifications_time_hours_ago, *anyVararg()) } answers {
            "${(invocation.args[1] as Array<*>)[0]} hours ago"
        }
        every { getString(R.string.notifications_time_days_ago, *anyVararg()) } answers {
            "${(invocation.args[1] as Array<*>)[0]} days ago"
        }
    }

    private val now = TimeUnit.DAYS.toMillis(20_000) // arbitrary stable "now"

    @Test
    fun `under one minute returns just now`() {
        val eventMillis = now - TimeUnit.SECONDS.toMillis(30)
        assertEquals("Just now", TimeAgoFormatter.formatMillis(context, eventMillis, now))
    }

    @Test
    fun `minutes resolution under one hour`() {
        val eventMillis = now - TimeUnit.MINUTES.toMillis(5)
        assertEquals("5 minutes ago", TimeAgoFormatter.formatMillis(context, eventMillis, now))
    }

    @Test
    fun `hours resolution under one day`() {
        val eventMillis = now - TimeUnit.HOURS.toMillis(3)
        assertEquals("3 hours ago", TimeAgoFormatter.formatMillis(context, eventMillis, now))
    }

    @Test
    fun `days resolution for older events`() {
        val eventMillis = now - TimeUnit.DAYS.toMillis(7)
        assertEquals("7 days ago", TimeAgoFormatter.formatMillis(context, eventMillis, now))
    }

    @Test
    fun `negative delta is clamped to just now`() {
        val eventMillis = now + TimeUnit.MINUTES.toMillis(5)
        assertEquals("Just now", TimeAgoFormatter.formatMillis(context, eventMillis, now))
    }

    @Test
    fun `null iso timestamp returns just now`() {
        assertEquals("Just now", TimeAgoFormatter.format(context, null, now))
    }
}
