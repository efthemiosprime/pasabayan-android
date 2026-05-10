package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Role pill — shared by explore header and profile header (iOS `RoleBadge` / chip styling).
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

/**
 * iOS `CompactRolePicker` parity: tappable chip + chevron opens role menu (profile header).
 */
@Composable
fun CompactRolePickerRow(
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val (bgColor, icon, labelRes) = roleChipStyle(currentRole)
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(PasabayanRadius.sm))
                .clickable { expanded = true }
                .background(bgColor.copy(alpha = 0.15f), RoundedCornerShape(PasabayanRadius.sm))
                .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
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
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = bgColor,
            )
        }
        RoleSwitchDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            currentRole = currentRole,
            onSwitchRole = onSwitchRole,
        )
    }
}

/** Explore header: overflow icon opens the same role menu. */
@Composable
fun RoleSwitcherIconButton(
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = stringResource(R.string.dashboard_switch_role),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RoleSwitchDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            currentRole = currentRole,
            onSwitchRole = onSwitchRole,
        )
    }
}

@Composable
private fun RoleSwitchDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        UserRole.entries.forEach { role ->
            val isSelected = role == currentRole
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = when (role) {
                                UserRole.CARRIER -> stringResource(R.string.dashboard_role_carrier)
                                UserRole.SHIPPER -> stringResource(R.string.dashboard_role_shipper)
                            },
                        )
                    }
                },
                onClick = {
                    onDismissRequest()
                    if (!isSelected) onSwitchRole()
                },
            )
        }
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
