package com.efthemiosprime.pasabayan.features.dashboard.model

import androidx.annotation.DrawableRes
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

/**
 * Tab definition for the main bottom navigation.
 * Tab order mirrors iOS: Explore (0), Matches (1), role-specific (2), Messages (3), Profile (4).
 * Uses custom drawable icons matching iOS asset catalog.
 */
data class MainTab(
    val route: String,
    val labelResId: Int,
    @DrawableRes val iconResId: Int,
)

object MainTabs {
    fun forRole(role: UserRole): List<MainTab> = listOf(
        MainTab(
            route = "explore",
            labelResId = R.string.dashboard_tab_explore,
            iconResId = R.drawable.ic_tab_explore,
        ),
        MainTab(
            route = "matches",
            labelResId = R.string.dashboard_tab_matches,
            iconResId = R.drawable.ic_tab_matches,
        ),
        when (role) {
            UserRole.CARRIER -> MainTab(
                route = "my_trips",
                labelResId = R.string.dashboard_tab_my_trips,
                iconResId = R.drawable.ic_tab_my_trips,
            )
            UserRole.SHIPPER -> MainTab(
                route = "packages",
                labelResId = R.string.dashboard_tab_packages,
                iconResId = R.drawable.ic_tab_packages,
            )
        },
        MainTab(
            route = "messages",
            labelResId = R.string.dashboard_tab_messages,
            iconResId = R.drawable.ic_tab_messages,
        ),
        MainTab(
            route = "profile",
            labelResId = R.string.dashboard_tab_profile,
            iconResId = R.drawable.ic_tab_profile,
        ),
    )
}
