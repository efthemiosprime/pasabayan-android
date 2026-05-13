package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    userName: String,
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    verificationLevel: VerificationLevel = VerificationLevel.BASIC,
    notificationsUnreadCount: Int = 0,
    onOpenNotifications: (() -> Unit)? = null,
) {
    val roleLabel = when (currentRole) {
        UserRole.CARRIER -> stringResource(R.string.dashboard_role_carrier)
        UserRole.SHIPPER -> stringResource(R.string.dashboard_role_shipper)
    }
    val roleColor = when (currentRole) {
        UserRole.CARRIER -> PasabayanColors.BadgeBlue
        UserRole.SHIPPER -> PasabayanColors.BadgePurple
    }

    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Text(
                    text = userName,
                    style = PasabayanTextStyles.Heading.h5,
                )
                VerificationBadge(level = verificationLevel)
            }
        },
        actions = {
            Text(
                text = roleLabel,
                style = PasabayanTextStyles.Caption.large,
                color = roleColor,
                modifier = Modifier
                    .background(
                        roleColor.copy(alpha = 0.15f),
                        RoundedCornerShape(PasabayanRadius.badge),
                    )
                    .padding(
                        horizontal = PasabayanSpacing.sm,
                        vertical = PasabayanSpacing.xs,
                    ),
            )
            IconButton(onClick = onSwitchRole) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = stringResource(R.string.dashboard_switch_role),
                )
            }
            if (onOpenNotifications != null) {
                NotificationsBellAction(
                    unreadCount = notificationsUnreadCount,
                    onClick = onOpenNotifications,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

/**
 * iOS parity: a bell icon with an unread count badge. Tapping opens the comprehensive
 * notifications screen. The badge caps overflow at "99+" to match iOS.
 */
@Composable
private fun NotificationsBellAction(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    Box {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.dashboard_open_notifications),
            )
        }
        if (unreadCount > 0) {
            val badgeText = if (unreadCount > 99) {
                stringResource(R.string.dashboard_notifications_badge_overflow)
            } else {
                unreadCount.toString()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .background(PasabayanColors.Error, CircleShape)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text(
                    text = badgeText,
                    style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
            }
        }
    }
}
