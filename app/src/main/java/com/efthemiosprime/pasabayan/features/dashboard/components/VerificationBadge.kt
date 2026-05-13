package com.efthemiosprime.pasabayan.features.dashboard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel

/**
 * Tiny verification indicator. iOS parity: verified accounts show a green check;
 * premium accounts show a gold star; basic accounts show nothing — the absence
 * is the signal.
 *
 * Shared between [DashboardTopBar] (top-bar inline next to the user name) and
 * the profile header (also next to the name). Single source of truth — promote
 * to `:core:designsystem` if a third consumer outside the app module emerges.
 */
@Composable
fun VerificationBadge(
    level: VerificationLevel,
    modifier: Modifier = Modifier,
    size: Dp = DefaultBadgeSize,
) {
    when (level) {
        VerificationLevel.VERIFIED -> Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.dashboard_verification_verified),
            tint = PasabayanColors.Success,
            modifier = modifier.size(size),
        )
        VerificationLevel.PREMIUM -> Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = stringResource(R.string.dashboard_verification_premium),
            tint = PasabayanColors.BadgeGold,
            modifier = modifier.size(size),
        )
        VerificationLevel.BASIC -> Unit
    }
}

private val DefaultBadgeSize = 14.dp
