package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanLayout
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Reusable user-information card used by detail screens to surface the counterparty
 * (shipper for carrier-side flows, carrier for shipper-side flows). iOS parity:
 * `shipperInfoSection` in `RequestToCarrySheet` and the carrier header in
 * `BrowseTripsView.TripCardView`.
 *
 * Stays free of `:core:domain` so the design system remains a leaf module —
 * feature modules map their `UserSummary` fields onto these params.
 *
 * @param avatarContent Optional composable slot for a loaded image (e.g. Coil
 *   `AsyncImage`). Falls back to a circular initial-letter badge.
 * @param trailingContent Optional trailing slot for action affordances such as
 *   a chat button.
 * @param warningContent Optional block rendered below the row, used for the
 *   unverified-shipper trust warning on iOS.
 * @param onClick When non-null, the row becomes tappable (e.g. open profile).
 */
@Composable
fun PUserInfoCard(
    name: String,
    title: String,
    modifier: Modifier = Modifier,
    rating: String? = null,
    totalRatings: Int? = null,
    verificationLevel: String? = null,
    noRatingsLabel: String? = null,
    onClick: (() -> Unit)? = null,
    avatarContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    warningContent: @Composable (() -> Unit)? = null,
) {
    PDetailSheetCard(modifier = modifier) {
        PDetailSectionTitle(text = title)
        val rowModifier = Modifier
            .fillMaxWidth()
            .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(name = name, avatarContent = avatarContent)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = PasabayanTextStyles.Body.large,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    VerificationBadge(level = verificationLevel)
                }
                RatingRow(
                    rating = rating,
                    totalRatings = totalRatings,
                    noRatingsLabel = noRatingsLabel,
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (warningContent != null) {
            warningContent()
        }
    }
}

@Composable
private fun UserAvatar(
    name: String,
    avatarContent: @Composable (() -> Unit)?,
) {
    if (avatarContent != null) {
        Box(
            modifier = Modifier
                .size(PasabayanLayout.avatarSizeMedium)
                .clip(CircleShape),
        ) {
            avatarContent()
        }
    } else {
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
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun VerificationBadge(level: String?) {
    val normalized = level?.lowercase()?.trim() ?: return
    when (normalized) {
        "verified" -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(PasabayanLayout.iconSizeSmall),
            tint = PasabayanColors.Info,
        )
        "premium" -> Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(PasabayanLayout.iconSizeSmall),
            tint = PasabayanColors.BadgeGold,
        )
        else -> {}
    }
}

@Composable
private fun RatingRow(
    rating: String?,
    totalRatings: Int?,
    noRatingsLabel: String?,
) {
    val hasRating = rating != null && (totalRatings ?: 0) > 0
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasRating) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = PasabayanColors.BadgeGold,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = rating!!,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "($totalRatings)",
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (noRatingsLabel != null) {
            Text(
                text = noRatingsLabel,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "PUserInfoCard — light")
@Preview(
    showBackground = true,
    name = "PUserInfoCard — dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PUserInfoCardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            PUserInfoCard(
                name = "Jane Shipper",
                title = "Sender Information",
                rating = "4.7",
                totalRatings = 12,
                verificationLevel = "verified",
                noRatingsLabel = "No ratings yet",
                onClick = {},
            )
            PUserInfoCard(
                name = "Alex Carrier",
                title = "Carrier Information",
                rating = null,
                totalRatings = 0,
                verificationLevel = "basic",
                noRatingsLabel = "No ratings yet",
            )
        }
    }
}
