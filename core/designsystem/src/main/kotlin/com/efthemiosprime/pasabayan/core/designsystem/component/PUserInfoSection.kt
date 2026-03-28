package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanLayout
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles

/**
 * Avatar + name + optional rating + optional verification badge.
 * Takes primitive params so designsystem stays independent of :core:domain.
 * Feature modules map UserSummary fields to these params.
 *
 * @param avatarContent Optional composable slot for a loaded image (e.g. via Coil).
 *   Falls back to a letter-circle with the first character of [name].
 */
@Composable
fun PUserInfoSection(
    name: String,
    modifier: Modifier = Modifier,
    rating: String? = null,
    verificationLevel: String? = null,
    avatarContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        if (avatarContent != null) {
            Box(
                modifier = Modifier
                    .size(PasabayanLayout.avatarSizeMedium)
                    .clip(CircleShape),
            ) {
                avatarContent()
            }
        } else {
            LetterCircleAvatar(name = name)
        }

        // Name + meta
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                VerificationIcon(verificationLevel)
            }

            if (rating != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(PasabayanLayout.iconSizeSmall),
                        tint = PasabayanColors.BadgeGold,
                    )
                    Text(
                        text = rating,
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterCircleAvatar(name: String) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(PasabayanLayout.avatarSizeMedium)
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
private fun VerificationIcon(level: String?) {
    when (level?.lowercase()) {
        "verified" -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verified",
            modifier = Modifier.size(PasabayanLayout.iconSizeSmall),
            tint = PasabayanColors.Info,
        )
        "premium" -> Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Premium",
            modifier = Modifier.size(PasabayanLayout.iconSizeSmall),
            tint = PasabayanColors.BadgeGold,
        )
        else -> {}
    }
}
