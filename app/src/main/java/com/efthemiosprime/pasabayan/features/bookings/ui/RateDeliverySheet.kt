package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.RateDeliveryUiState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.RateDeliveryViewModel
import com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader

/**
 * Post-delivery rating sheet. Two-way: shipper rates carrier, carrier rates
 * shipper. iOS parity: `Features/Bookings/Views/Shipper/RateDeliverySheet.swift`.
 *
 * Stateless content layer accepts the [otherParty] + role and the VM state +
 * callbacks. Submission goes through [BookingsRepository.submitRating]; the
 * host dismisses the sheet when [onSubmitted] returns true.
 */
@Composable
fun RateDeliverySheet(
    matchId: Int,
    otherParty: UserSummary,
    onClose: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RateDeliveryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) onSubmitted()
    }

    RateDeliverySheetContent(
        otherParty = otherParty,
        state = state,
        onClose = onClose,
        onRatingChange = viewModel::setRating,
        onReviewChange = viewModel::setReviewText,
        onSubmit = { viewModel.submitRating(matchId) },
        onDismissError = viewModel::clearError,
        modifier = modifier,
    )
}

@Composable
internal fun RateDeliverySheetContent(
    otherParty: UserSummary,
    state: RateDeliveryUiState,
    onClose: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        HeaderRow(onClose = onClose)
        PersonHeader(otherParty = otherParty)
        RatingSection(rating = state.rating, onRatingChange = onRatingChange)
        ReviewSection(
            reviewText = state.reviewText,
            characterCount = state.characterCount,
            isOverLimit = state.isOverCharacterLimit,
            onReviewChange = onReviewChange,
        )

        state.errorMessage?.let { ErrorBanner(message = it, onDismiss = onDismissError) }

        PButton(
            text = if (state.isSubmitting) {
                stringResource(R.string.bookings_rate_delivery_submitting)
            } else {
                stringResource(R.string.bookings_rate_delivery_submit)
            },
            onClick = onSubmit,
            enabled = state.canSubmit,
            isLoading = state.isSubmitting,
            size = PButtonSize.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HeaderRow(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.bookings_rate_delivery_title),
            style = PasabayanTextStyles.Heading.h4,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        PButton(
            text = stringResource(R.string.bookings_rate_delivery_close),
            onClick = onClose,
            style = PButtonStyle.Tertiary,
            size = PButtonSize.Small,
        )
    }
}

@Composable
private fun PersonHeader(otherParty: UserSummary) {
    UserCardHeader(user = otherParty)
}

@Composable
private fun RatingSection(rating: Int, onRatingChange: (Int) -> Unit) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.bookings_rate_delivery_experience_question),
                style = PasabayanTextStyles.Body.medium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            StarRow(rating = rating, onRatingChange = onRatingChange)
            if (rating > 0) {
                Text(
                    text = ratingDescription(rating),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Reserve space so the card doesn't jump when the first tap lands.
                Text(
                    text = "",
                    style = PasabayanTextStyles.Body.small,
                    modifier = Modifier.height(20.dp),
                )
            }
        }
    }
}

@Composable
private fun StarRow(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..5).forEach { star ->
            val filled = star <= rating
            val cd = stringResource(R.string.bookings_rate_delivery_star_cd, star)
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (filled) PasabayanColors.BadgeAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClickLabel = cd) { onRatingChange(star) }
                    .semantics { contentDescription = cd },
            )
        }
    }
}

@Composable
private fun ratingDescription(rating: Int): String = when (rating) {
    1 -> stringResource(R.string.bookings_rate_delivery_rating_terrible)
    2 -> stringResource(R.string.bookings_rate_delivery_rating_poor)
    3 -> stringResource(R.string.bookings_rate_delivery_rating_okay)
    4 -> stringResource(R.string.bookings_rate_delivery_rating_good)
    5 -> stringResource(R.string.bookings_rate_delivery_rating_excellent)
    else -> ""
}

@Composable
private fun ReviewSection(
    reviewText: String,
    characterCount: Int,
    isOverLimit: Boolean,
    onReviewChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = stringResource(R.string.bookings_rate_delivery_write_review),
            style = PasabayanTextStyles.Body.medium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        POutlinedTextField(
            value = reviewText,
            onValueChange = onReviewChange,
            label = { Text(stringResource(R.string.bookings_rate_delivery_write_review)) },
            placeholder = { Text(stringResource(R.string.bookings_rate_delivery_placeholder)) },
            singleLine = false,
            maxLines = 6,
            isError = isOverLimit,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = stringResource(
                    R.string.bookings_rate_delivery_character_counter,
                    characterCount,
                    RateDeliveryUiState.REVIEW_CHARACTER_LIMIT,
                ),
                style = PasabayanTextStyles.Caption.regular,
                color = if (isOverLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.bookings_rate_delivery_error_title),
                    style = PasabayanTextStyles.Body.medium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = message,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PButton(
                text = stringResource(R.string.bookings_rate_delivery_close),
                onClick = onDismiss,
                style = PButtonStyle.Tertiary,
                size = PButtonSize.Small,
            )
        }
    }
}

// -- Previews -----------------------------------------------------------------

private fun previewParty(): UserSummary = UserSummary(
    id = 7,
    name = "Carla Reyes",
    rating = "4.8",
    totalRatings = 24,
    avatar = null,
)

@Preview(showBackground = true, name = "RateDelivery — idle")
@Preview(showBackground = true, name = "RateDelivery — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RateDeliverySheetIdlePreview() {
    PasabayanTheme {
        RateDeliverySheetContent(
            otherParty = previewParty(),
            state = RateDeliveryUiState(),
            onClose = {}, onRatingChange = {}, onReviewChange = {},
            onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "RateDelivery — 5 stars + review")
@Composable
private fun RateDeliverySheetFilledPreview() {
    PasabayanTheme {
        RateDeliverySheetContent(
            otherParty = previewParty(),
            state = RateDeliveryUiState(
                rating = 5,
                reviewText = "Carla was excellent — fast pickup, careful handling, friendly comms.",
            ),
            onClose = {}, onRatingChange = {}, onReviewChange = {},
            onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "RateDelivery — submitting")
@Composable
private fun RateDeliverySheetSubmittingPreview() {
    PasabayanTheme {
        RateDeliverySheetContent(
            otherParty = previewParty(),
            state = RateDeliveryUiState(rating = 4, isSubmitting = true),
            onClose = {}, onRatingChange = {}, onReviewChange = {},
            onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "RateDelivery — error")
@Composable
private fun RateDeliverySheetErrorPreview() {
    PasabayanTheme {
        RateDeliverySheetContent(
            otherParty = previewParty(),
            state = RateDeliveryUiState(
                rating = 3,
                errorMessage = "You've already rated this delivery.",
            ),
            onClose = {}, onRatingChange = {}, onReviewChange = {},
            onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "RateDelivery — over limit")
@Composable
private fun RateDeliverySheetOverLimitPreview() {
    PasabayanTheme {
        RateDeliverySheetContent(
            otherParty = previewParty(),
            state = RateDeliveryUiState(
                rating = 5,
                reviewText = "a".repeat(RateDeliveryUiState.REVIEW_CHARACTER_LIMIT + 5),
            ),
            onClose = {}, onRatingChange = {}, onReviewChange = {},
            onSubmit = {}, onDismissError = {},
        )
    }
}
