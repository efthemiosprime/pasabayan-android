package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    userName: String,
    currentRole: UserRole,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
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
            Text(
                text = stringResource(R.string.dashboard_greeting, userName),
                style = PasabayanTextStyles.Heading.h5,
            )
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
