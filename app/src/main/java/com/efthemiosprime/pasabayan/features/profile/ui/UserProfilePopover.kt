package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.features.profile.viewmodel.UserProfilePopoverViewModel

/**
 * Reusable user-profile bottom sheet. Parity with iOS
 * `UserProfilePopover.swift`.
 *
 * Tappable from the [com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader]
 * rows on explore-tab cards. Renders:
 * - Header (avatar + name + verification + member-since + rating)
 * - Performance stats card (only when viewing self as carrier)
 * - "Add to Favorites" toggle (only when shipper viewing carrier)
 * - Ratings & Reviews section: histogram + paginated review cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePopover(
    userId: Int,
    userName: String?,
    userAvatar: String?,
    verificationLevel: String?,
    initialRating: Double?,
    initialTotalRatings: Int?,
    userRole: UserRole?,
    memberSince: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    /** Current authenticated user id — drives `viewerIsSelf` and Performance stats visibility. */
    currentUserId: Int? = null,
    /** Current user's active role — drives "Add to Favorites" visibility. */
    currentUserRole: UserRole = UserRole.SHIPPER,
    viewModel: UserProfilePopoverViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val viewerIsSelf = currentUserId != null && currentUserId == userId
    val shouldShowFavorite = !viewerIsSelf &&
        userRole == UserRole.CARRIER &&
        currentUserRole == UserRole.SHIPPER

    LaunchedEffect(userId) {
        viewModel.load(
            userId = userId,
            viewerIsSelf = viewerIsSelf,
            shouldCheckFavorite = shouldShowFavorite,
        )
    }

    PModalBottomSheet(onDismissRequest = onDismiss) {
        PDetailSheetScaffold(
            title = stringResource(R.string.profile_user_profile_title),
            closeContentDescription = stringResource(R.string.profile_user_profile_close),
            onClose = onDismiss,
            modifier = modifier.testTag(POPOVER_TEST_TAG),
        ) {
            if (shouldShowFavorite) {
                FavoriteToggleRow(
                    isFavorite = state.isFavorite,
                    isToggling = state.isTogglingFavorite,
                    isChecking = state.isCheckingFavorite,
                    onToggle = { viewModel.toggleFavorite(userId) },
                )
            }

            ProfileHeader(
                userId = userId,
                userName = userName,
                verificationLevel = verificationLevel,
                initialRating = initialRating,
                initialTotalRatings = initialTotalRatings,
                userRole = userRole,
                memberSince = memberSince,
            )

            PDivider()

            if (viewerIsSelf && userRole == UserRole.CARRIER) {
                PerformanceStatsSection(
                    isLoading = state.isLoadingStats,
                    error = state.statsErrorMessage,
                    stats = state.carrierStats,
                    rating = initialRating,
                )
                PDivider()
            }

            RatingsAndReviewsSection(
                totalRatings = initialTotalRatings,
                reviews = state.reviews,
                ratingDistribution = state.ratingDistribution,
                isLoading = state.isLoadingReviews,
                isLoadingMore = state.isLoadingMoreReviews,
                hasMore = state.reviewsHasMore,
                onLoadMore = { viewModel.loadMoreReviews(userId) },
            )
        }
    }
}

// ---- Header ----

@Composable
private fun ProfileHeader(
    userId: Int,
    userName: String?,
    verificationLevel: String?,
    initialRating: Double?,
    initialTotalRatings: Int?,
    userRole: UserRole?,
    memberSince: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        AvatarWithVerificationBadge(
            name = userName,
            userId = userId,
            verificationLevel = verificationLevel,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = userName ?: stringResource(R.string.profile_user_number, userId),
                style = PasabayanTextStyles.Heading.h3,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            verificationLevel?.let { VerificationPill(level = it) }
            memberSince?.let {
                val label = if (userRole == UserRole.CARRIER) {
                    stringResource(R.string.profile_carrier_since, it)
                } else {
                    stringResource(R.string.profile_member_since, it)
                }
                Text(
                    text = label,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (userRole == UserRole.CARRIER) {
                HeaderRatingRow(rating = initialRating, totalRatings = initialTotalRatings)
            } else {
                RoleTag(userRole = userRole)
            }
        }
    }
}

@Composable
private fun AvatarWithVerificationBadge(
    name: String?,
    userId: Int,
    verificationLevel: String?,
) {
    Box(modifier = Modifier.size(AVATAR_SIZE)) {
        Box(
            modifier = Modifier
                .size(AVATAR_SIZE)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .border(2.dp, PasabayanColors.Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initialsFor(name) ?: "#$userId",
                style = PasabayanTextStyles.Heading.h3,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (verificationLevel?.lowercase() != null && verificationLevel.lowercase() != "basic") {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PasabayanColors.Success,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

private fun initialsFor(name: String?): String? {
    if (name.isNullOrBlank()) return null
    val parts = name.trim().split(' ').filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        else -> parts[0].first().uppercase()
    }
}

@Composable
private fun VerificationPill(level: String) {
    val normalized = level.lowercase()
    val (bg, fg, label) = when (normalized) {
        "verified" -> Triple(
            PasabayanColors.Success.copy(alpha = 0.15f),
            PasabayanColors.Success,
            stringResource(R.string.profile_verification_verified),
        )
        "premium" -> Triple(
            PasabayanColors.BadgeGold.copy(alpha = 0.2f),
            PasabayanColors.BadgeGold,
            stringResource(R.string.profile_verification_premium),
        )
        else -> Triple(
            PasabayanColors.Border.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.profile_verification_basic),
        )
    }
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(PasabayanRadius.sm))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular,
            color = fg,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun HeaderRatingRow(rating: Double?, totalRatings: Int?) {
    if (rating == null || totalRatings == null || totalRatings <= 0) {
        Text(
            text = stringResource(R.string.profile_not_yet_rated),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StarRow(rating = rating.toInt().coerceIn(0, 5))
        Text(
            text = String.format("%.1f", rating),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.profile_trip_count, totalRatings),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StarRow(rating: Int, size: androidx.compose.ui.unit.Dp = 14.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = PasabayanColors.BadgeGold,
                modifier = Modifier.size(size),
            )
        }
    }
}

@Composable
private fun RoleTag(userRole: UserRole?) {
    val label = when (userRole) {
        UserRole.CARRIER -> stringResource(R.string.common_role_carrier)
        UserRole.SHIPPER, null -> stringResource(R.string.common_role_sender)
    }
    Text(
        text = label,
        style = PasabayanTextStyles.Body.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(PasabayanColors.Border.copy(alpha = 0.3f), RoundedCornerShape(PasabayanRadius.sm))
            .padding(horizontal = PasabayanSpacing.md, vertical = 6.dp),
    )
}

// ---- Performance stats ----

@Composable
private fun PerformanceStatsSection(
    isLoading: Boolean,
    error: String?,
    stats: CarrierStatsJson?,
    rating: Double?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Text(
            text = stringResource(R.string.profile_performance),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            }
            error != null -> {
                Text(
                    text = stringResource(R.string.profile_unable_load_stats),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
                ) {
                    PerformanceStatItem(
                        value = (stats?.deliveries?.completedTrips ?: 0).toString(),
                        label = stringResource(R.string.profile_stats_deliveries),
                        modifier = Modifier.weight(1f),
                    )
                    PerformanceStatItem(
                        value = formatEarnings(stats?.earnings?.totalEarnings),
                        label = stringResource(R.string.profile_stats_earned),
                        modifier = Modifier.weight(1f),
                    )
                    PerformanceStatItem(
                        value = String.format("%.1f", rating ?: 0.0),
                        label = stringResource(R.string.profile_stats_rating),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = PasabayanTextStyles.Heading.h2,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatEarnings(amount: Double?): String {
    val safe = amount ?: 0.0
    return "$${safe.toInt()}"
}

// ---- Favorite toggle ----

@Composable
private fun FavoriteToggleRow(
    isFavorite: Boolean,
    isToggling: Boolean,
    isChecking: Boolean,
    onToggle: () -> Unit,
) {
    val label = when {
        isToggling && isFavorite -> stringResource(R.string.profile_favorites_removing)
        isToggling && !isFavorite -> stringResource(R.string.profile_favorites_adding)
        isChecking -> stringResource(R.string.profile_favorites_checking)
        isFavorite -> stringResource(R.string.profile_favorites_remove)
        else -> stringResource(R.string.profile_favorites_add)
    }
    PButton(
        text = label,
        onClick = onToggle,
        style = if (isFavorite) PButtonStyle.Destructive else PButtonStyle.Secondary,
        size = PButtonSize.Small,
        isLoading = isToggling,
        enabled = !isChecking,
        icon = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
    )
}

// ---- Reviews ----

@Composable
private fun RatingsAndReviewsSection(
    totalRatings: Int?,
    reviews: List<RatingWithRaterJson>,
    ratingDistribution: Map<Int, Int>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_ratings_and_reviews),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (totalRatings != null && totalRatings > 0) {
                Text(
                    text = totalRatings.toString(),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (reviews.isNotEmpty()) {
            RatingDistributionView(distribution = ratingDistribution)
        }
        when {
            isLoading && reviews.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            }
            reviews.isEmpty() -> {
                Text(
                    text = stringResource(R.string.profile_no_reviews_yet),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.profile_no_reviews_description),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                for (review in reviews) {
                    UserReviewCard(review = review)
                }
                if (hasMore) {
                    PButton(
                        text = stringResource(R.string.profile_load_more_reviews),
                        onClick = onLoadMore,
                        style = PButtonStyle.Secondary,
                        isLoading = isLoadingMore,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserReviewCard(review: RatingWithRaterJson) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                val raterName = review.rater?.name.orEmpty()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initialsFor(raterName) ?: "?",
                        style = PasabayanTextStyles.Body.medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = raterName.ifBlank { stringResource(R.string.common_role_sender) },
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    StarRow(rating = review.rating.coerceIn(0, 5), size = 12.dp)
                }
            }
            review.reviewText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RatingDistributionView(distribution: Map<Int, Int>) {
    val total = distribution.values.sum().coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        for (stars in 5 downTo 1) {
            val count = distribution[stars] ?: 0
            val fraction = count.toFloat() / total.toFloat()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                Text(
                    text = stars.toString(),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(width = 20.dp, height = 16.dp),
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = PasabayanColors.BadgeGold,
                    modifier = Modifier.size(12.dp),
                )
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                PasabayanColors.Border.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp),
                            ),
                    )
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .size(width = maxWidth * fraction, height = 8.dp)
                                .background(PasabayanColors.BadgeGold, RoundedCornerShape(4.dp)),
                        )
                    }
                }
                Text(
                    text = count.toString(),
                    style = PasabayanTextStyles.Caption.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(width = 30.dp, height = 16.dp),
                )
            }
        }
    }
}

private val AVATAR_SIZE = 80.dp

internal const val POPOVER_TEST_TAG = "Profile.UserProfilePopover"

// ---- Preview ----

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "UserProfilePopover — light", heightDp = 900)
@Preview(showBackground = true, name = "UserProfilePopover — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserProfilePopoverPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            // Render the inner content directly; PModalBottomSheet is finicky in IDE previews.
            ProfileHeader(
                userId = 123,
                userName = "Jose Esplana",
                verificationLevel = "verified",
                initialRating = 4.3,
                initialTotalRatings = 20,
                userRole = UserRole.CARRIER,
                memberSince = "May 2023",
            )
            Spacer(Modifier.height(PasabayanSpacing.md))
            RatingDistributionView(
                distribution = mapOf(5 to 14, 4 to 4, 3 to 1, 2 to 0, 1 to 1),
            )
        }
    }
}
