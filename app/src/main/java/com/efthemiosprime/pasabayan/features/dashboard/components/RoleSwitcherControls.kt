package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

/**
 * Static role pill — used for identification only (in `UserHeaderCard` welcome panel and
 * the profile header). Role switching itself lives **only** in `DashboardTopBar`'s
 * `SwapHoriz` icon (single source of truth). iOS `RoleBadge` parity.
 */
@Composable
fun RoleChip(role: UserRole, modifier: Modifier = Modifier) {
    val (bgColor, icon, labelRes) = roleChipStyle(role)
    Row(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.15f), RoundedCornerShape(PasabayanRadius.sm))
            .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = bgColor,
        )
        Text(
            text = stringResource(labelRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = bgColor,
        )
    }
}

private fun roleChipStyle(role: UserRole): Triple<Color, ImageVector, Int> =
    when (role) {
        UserRole.CARRIER -> Triple(
            PasabayanColors.BadgeBlue,
            Icons.Default.Inventory2,
            R.string.dashboard_role_carrier,
        )
        UserRole.SHIPPER -> Triple(
            PasabayanColors.BadgePurple,
            Icons.Default.Send,
            R.string.dashboard_role_shipper,
        )
    }
