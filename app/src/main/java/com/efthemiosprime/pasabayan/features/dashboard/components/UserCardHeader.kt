package com.efthemiosprime.pasabayan.features.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.efthemiosprime.pasabayan.core.designsystem.component.PAvatar
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

/**
 * Reusable "this is the other party" header for explore-tab cards. Renders
 * a tappable row with avatar + name + verification dot + rating row +
 * chevron, mirroring iOS `CarrierPackageCard` and `TripCardView` headers
 * and consolidating with iOS `UserProfilePopover`'s entry point.
 *
 * The whole row is clickable when [onClick] is non-null; the chevron only
 * appears in that case. iOS uses a `Button { } label: { HStack }` with a
 * chevron-right glyph; Android uses Modifier.clickable on the Row.
 */
@Composable
fun UserCardHeader(
    user: UserSummary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    /** Optional trailing slot — e.g. a status badge that sits to the right. */
    trailing: (@Composable () -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(end = PasabayanSpacing.xs)
    } else {
        modifier.fillMaxWidth()
    }
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PAvatar(
            url = user.avatar,
            fallbackName = user.name,
            size = 36.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = user.name,
                    style = PasabayanTextStyles.Body.large,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                VerificationDot(level = user.verificationLevel)
            }
            RatingRow(formattedRating = user.formattedRating, totalRatings = user.totalRatings)
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun VerificationDot(level: String?) {
    val normalized = level?.lowercase()?.trim() ?: return
    val tint = when (normalized) {
        "verified" -> PasabayanColors.Success
        "premium" -> PasabayanColors.BadgeGold
        else -> return
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color = tint, shape = CircleShape),
    )
}

@Composable
private fun RatingRow(formattedRating: String, totalRatings: Int?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (totalRatings != null && totalRatings > 0) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = PasabayanColors.BadgeGold,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = formattedRating,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "($totalRatings)",
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = stringResource(R.string.carrier_package_card_no_ratings_yet),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "UserCardHeader — light")
@Preview(showBackground = true, name = "UserCardHeader — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserCardHeaderPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            // Tappable verified carrier
            UserCardHeader(
                user = UserSummary(
                    id = 1,
                    name = "Jose Esplana",
                    rating = "4.3",
                    totalRatings = 20,
                    verificationLevel = "verified",
                ),
                onClick = {},
            )
            // Non-tappable, no rating
            UserCardHeader(
                user = UserSummary(
                    id = 2,
                    name = "New Carrier",
                    rating = null,
                    totalRatings = null,
                    verificationLevel = "basic",
                ),
                onClick = null,
            )
        }
    }
}
