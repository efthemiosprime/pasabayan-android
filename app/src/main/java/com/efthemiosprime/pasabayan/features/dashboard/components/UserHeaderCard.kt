package com.efthemiosprime.pasabayan.features.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

/**
 * User header card — parity with iOS `UserHeaderCard.swift`.
 * Avatar + welcome + name + role chip + verification badge + three-dot menu.
 */
@Composable
fun UserHeaderCard(
    userName: String,
    currentRole: UserRole,
    verificationLevel: String?,
    avatarUrl: String?,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    avatarContent: @Composable (() -> Unit)? = null,
) {
    val welcomeText = when (currentRole) {
        UserRole.CARRIER -> stringResource(R.string.dashboard_carrier_welcome)
        UserRole.SHIPPER -> stringResource(R.string.dashboard_shipper_welcome)
    }

    PCard(modifier = modifier.fillMaxWidth(), variant = PCardVariant.Large) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // Avatar
                if (avatarContent != null) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                    ) {
                        avatarContent()
                    }
                } else {
                    LetterAvatar(name = userName)
                }

                // Name + welcome
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = PasabayanSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = welcomeText,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = userName,
                        style = PasabayanTextStyles.Heading.h5,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                RoleSwitcherIconButton(
                    currentRole = currentRole,
                    onSwitchRole = onSwitchRole,
                )
            }

            // Role chip + verification badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoleChip(role = currentRole)
                VerificationBadgeChip(level = verificationLevel)
            }
        }
    }
}

@Composable
private fun LetterAvatar(name: String) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun VerificationBadgeChip(level: String?) {
    val normalized = level?.lowercase()?.trim() ?: "basic"
    val (icon, color, labelRes) = when (normalized) {
        "verified" -> Triple(Icons.Default.CheckCircle, PasabayanColors.Success, R.string.dashboard_verification_verified)
        "premium" -> Triple(Icons.Default.Star, PasabayanColors.BadgeGold, R.string.dashboard_verification_premium)
        else -> Triple(Icons.Default.CheckCircle, PasabayanColors.Border, R.string.dashboard_verification_basic)
    }
    Row(
        modifier = Modifier
            .background(
                if (normalized == "basic") Color.Transparent else color.copy(alpha = 0.15f),
                RoundedCornerShape(PasabayanRadius.sm),
            )
            .then(
                if (normalized == "basic") {
                    Modifier.background(Color.Transparent, RoundedCornerShape(PasabayanRadius.sm))
                } else Modifier
            )
            .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color,
        )
        Text(
            text = stringResource(labelRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (normalized == "basic") MaterialTheme.colorScheme.onSurfaceVariant else color,
        )
    }
}

@Preview(showBackground = true, name = "UserHeader Carrier — light")
@Preview(showBackground = true, name = "UserHeader Carrier — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserHeaderCardCarrierPreview() {
    PasabayanTheme {
        UserHeaderCard(
            userName = "Bong Suyat",
            currentRole = UserRole.CARRIER,
            verificationLevel = "basic",
            avatarUrl = null,
            onSwitchRole = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "UserHeader Shipper — light")
@Composable
private fun UserHeaderCardShipperPreview() {
    PasabayanTheme {
        UserHeaderCard(
            userName = "Bong Suyat",
            currentRole = UserRole.SHIPPER,
            verificationLevel = "verified",
            avatarUrl = null,
            onSwitchRole = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
