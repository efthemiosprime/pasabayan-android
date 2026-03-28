package com.efthemiosprime.pasabayan.features.dashboard.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

/**
 * Tab definition for the main bottom navigation.
 * Tab order mirrors iOS: Explore (0), Matches (1), role-specific (2), Messages (3), Profile (4).
 */
data class MainTab(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
)

object MainTabs {
    fun forRole(role: UserRole): List<MainTab> = listOf(
        MainTab(
            route = "explore",
            labelResId = R.string.dashboard_tab_explore,
            icon = Icons.Default.Explore,
        ),
        MainTab(
            route = "matches",
            labelResId = R.string.dashboard_tab_matches,
            icon = Icons.Default.SwapHoriz,
        ),
        when (role) {
            UserRole.CARRIER -> MainTab(
                route = "my_trips",
                labelResId = R.string.dashboard_tab_my_trips,
                icon = Icons.Outlined.LocalShipping,
            )
            UserRole.SHIPPER -> MainTab(
                route = "packages",
                labelResId = R.string.dashboard_tab_packages,
                icon = Icons.Default.Inventory2,
            )
        },
        MainTab(
            route = "messages",
            labelResId = R.string.dashboard_tab_messages,
            icon = Icons.Default.ChatBubble,
        ),
        MainTab(
            route = "profile",
            labelResId = R.string.dashboard_tab_profile,
            icon = Icons.Default.Person,
        ),
    )
}
