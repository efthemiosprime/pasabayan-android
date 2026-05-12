package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RemoveCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.features.bookings.model.CounterOfferContext
import com.efthemiosprime.pasabayan.features.bookings.model.CounterOfferSummaryKind

/**
 * Rich top-of-screen banner used in match details when the match is a
 * counter-offer. Parity with iOS `Features/Bookings/Views/Components/
 * CounterOfferBanner.swift`.
 *
 * Render with [CounterOfferContext.fromMatch] or [CounterOfferContext
 * .fromNotificationData]. Pass `currentUserId` so the banner can render
 * "You" when the viewer is the counter-offerer and pick the right summary
 * line for their role.
 */
@Composable
fun CounterOfferBanner(
    context: CounterOfferContext,
    currentUserId: Int?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(true) }
    if (!visible) return

    PCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(BANNER_TEST_TAG),
    ) {
        HeaderRow(
            context = context,
            currentUserId = currentUserId,
            onDismiss = {
                visible = false
                onDismiss()
            },
        )
        Spacer(Modifier.height(PasabayanSpacing.md))
        PriceComparisonRow(context = context)

        val summary = roleSummary(currentUserId = currentUserId, context = context)
        Spacer(Modifier.height(PasabayanSpacing.md))
        SummaryRow(summary = summary)

        val metadata = metadataLines(context)
        if (metadata.isNotEmpty()) {
            Spacer(Modifier.height(PasabayanSpacing.sm))
            MetadataLines(lines = metadata)
        }
    }
}

@Composable
private fun HeaderRow(
    context: CounterOfferContext,
    currentUserId: Int?,
    onDismiss: () -> Unit,
) {
    val name = counterOffererDisplayName(currentUserId, context)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.MonetizationOn,
            contentDescription = null,
            tint = PasabayanColors.BadgePurple,
            modifier = Modifier.size(ICON_LG),
        )
        Spacer(Modifier.width(PasabayanSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.bookings_counter_offer_title),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.bookings_counter_offer_banner_from, name),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(BANNER_DISMISS_TEST_TAG),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.bookings_counter_offer_banner_dismiss),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PriceComparisonRow(context: CounterOfferContext) {
    val direction = priceChangeRole(context.summaryKind)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(PasabayanRadius.sm),
            )
            .padding(vertical = PasabayanSpacing.sm, horizontal = PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Was
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.bookings_counter_offer_banner_was),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PasabayanSpacing.xs))
            Text(
                text = context.formattedOriginalPrice,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        // Arrow + diff
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = direction.arrowIcon,
                contentDescription = null,
                tint = direction.color,
                modifier = Modifier.size(ICON_LG),
            )
            Spacer(Modifier.height(PasabayanSpacing.xs))
            Text(
                text = context.formattedDifference,
                style = PasabayanTextStyles.Caption.large,
                color = direction.color,
            )
        }
        // Now
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.bookings_counter_offer_banner_now),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PasabayanSpacing.xs))
            Text(
                text = context.formattedNewPrice,
                style = PasabayanTextStyles.Heading.h5,
                color = if (context.isLowerOffer) PasabayanColors.Success else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SummaryRow(summary: CounterOfferSummary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = summary.icon,
            contentDescription = null,
            tint = summary.color,
            modifier = Modifier.size(ICON_SM),
        )
        Text(
            text = summary.text,
            style = PasabayanTextStyles.Caption.regular,
            color = summary.color,
        )
    }
}

@Composable
private fun MetadataLines(lines: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        for (line in lines) {
            Text(
                text = line,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ----------------- pure helpers -----------------

private data class CounterOfferSummary(
    val text: String,
    val color: Color,
    val icon: ImageVector,
)

@Composable
private fun roleSummary(
    currentUserId: Int?,
    context: CounterOfferContext,
): CounterOfferSummary {
    val viewerIsShipper = viewerIsShipper(currentUserId, context)
    val diff = context.formattedDifference

    if (context.summaryKind == CounterOfferSummaryKind.UNCHANGED) {
        return CounterOfferSummary(
            text = stringResource(R.string.bookings_counter_offer_summary_unchanged),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Filled.RemoveCircle,
        )
    }

    return when (viewerIsShipper) {
        true -> if (context.isLowerOffer) {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_you_saved, diff),
                color = PasabayanColors.Success,
                icon = Icons.Filled.CheckCircle,
            )
        } else {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_increased_by, diff),
                color = PasabayanColors.Warning,
                icon = Icons.Filled.Error,
            )
        }

        false -> if (context.isHigherOffer) {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_earned_more, diff),
                color = PasabayanColors.Success,
                icon = Icons.Filled.CheckCircle,
            )
        } else {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_below_rate, diff),
                color = PasabayanColors.Warning,
                icon = Icons.Filled.Error,
            )
        }

        null -> if (context.isLowerOffer) {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_reduced_by, diff),
                color = PasabayanColors.Success,
                icon = Icons.Filled.CheckCircle,
            )
        } else {
            CounterOfferSummary(
                text = stringResource(R.string.bookings_counter_offer_summary_increased_by, diff),
                color = PasabayanColors.Warning,
                icon = Icons.Filled.Error,
            )
        }
    }
}

/**
 * Returns `true` if the viewer is the shipper side of the exchange,
 * `false` if the viewer is the carrier side, `null` when the role can't
 * be determined (no current user, or `initiatedBy == UNKNOWN`). Mirrors
 * iOS `viewerIsShipper`.
 */
internal fun viewerIsShipper(
    currentUserId: Int?,
    context: CounterOfferContext,
): Boolean? {
    if (currentUserId == null) return null
    return when (context.initiatedBy) {
        InitiatedBy.CARRIER -> currentUserId != context.counterOffererId
        InitiatedBy.SHIPPER -> currentUserId == context.counterOffererId
        InitiatedBy.UNKNOWN -> null
    }
}

@Composable
private fun counterOffererDisplayName(
    currentUserId: Int?,
    context: CounterOfferContext,
): String {
    val name = context.counterOffererName.ifBlank { stringResource(R.string.common_role_sender) }
    val isYou = currentUserId != null && currentUserId == context.counterOffererId
    return if (isYou) stringResource(R.string.bookings_counter_offer_you) else name
}

@Composable
private fun metadataLines(context: CounterOfferContext): List<String> {
    val out = mutableListOf<String>()
    context.counterOfferRound?.takeIf { it > 0 }?.let {
        out += stringResource(R.string.bookings_detail_round, it)
    }
    val initiatedKey = when (context.initiatedBy) {
        InitiatedBy.SHIPPER -> R.string.bookings_counter_offer_banner_initiated_by_shipper
        InitiatedBy.CARRIER -> R.string.bookings_counter_offer_banner_initiated_by_carrier
        InitiatedBy.UNKNOWN -> null
    }
    initiatedKey?.let { out += stringResource(it) }

    val remaining = context.remainingCounterOffers
    when {
        remaining != null && remaining > 0 ->
            out += stringResource(R.string.bookings_detail_remaining_offers, remaining)
        remaining != null && remaining <= 0 ->
            out += stringResource(R.string.bookings_counter_offer_error_limit_reached)
        context.canCounterOffer == false ->
            out += stringResource(R.string.bookings_counter_offer_error_limit_reached)
    }
    return out
}

private data class PriceChangeRole(
    val arrowIcon: ImageVector,
    val color: Color,
)

@Composable
private fun priceChangeRole(kind: CounterOfferSummaryKind): PriceChangeRole = when (kind) {
    CounterOfferSummaryKind.LOWER -> PriceChangeRole(
        arrowIcon = Icons.Filled.ArrowCircleDown,
        color = PasabayanColors.Success,
    )
    CounterOfferSummaryKind.HIGHER -> PriceChangeRole(
        arrowIcon = Icons.Filled.ArrowCircleUp,
        color = PasabayanColors.Warning,
    )
    CounterOfferSummaryKind.UNCHANGED -> PriceChangeRole(
        arrowIcon = Icons.Filled.RemoveCircle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// Icon sizes — 24dp/16dp are standard Material sizes; not part of the
// general spacing scale so kept local here.
private val ICON_LG = PasabayanSpacing.xxl  // 24.dp
private val ICON_SM = PasabayanSpacing.lg   // 16.dp

// Test tags
internal const val BANNER_TEST_TAG = "CounterOffer.Banner"
internal const val BANNER_DISMISS_TEST_TAG = "CounterOffer.Banner.Dismiss"

// ----------------- previews -----------------

@Preview(showBackground = true, name = "Banner — Lower — light")
@Preview(showBackground = true, name = "Banner — Lower — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferBannerLowerPreview() {
    PasabayanTheme {
        Box(Modifier.padding(PasabayanSpacing.lg)) {
            CounterOfferBanner(
                context = CounterOfferContext(
                    newPrice = 70.0,
                    originalPrice = 100.0,
                    counterOffererName = "John Smith",
                    counterOffererId = 123,
                    initiatedBy = InitiatedBy.CARRIER,
                    isCounterOffer = true,
                    counterOfferRound = 1,
                    remainingCounterOffers = 2,
                    canCounterOffer = true,
                ),
                currentUserId = 1, // viewer is shipper (not the counter-offerer)
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Banner — Higher — light")
@Preview(showBackground = true, name = "Banner — Higher — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferBannerHigherPreview() {
    PasabayanTheme {
        Box(Modifier.padding(PasabayanSpacing.lg)) {
            CounterOfferBanner(
                context = CounterOfferContext(
                    newPrice = 130.0,
                    originalPrice = 100.0,
                    counterOffererName = "Jane Doe",
                    counterOffererId = 456,
                    initiatedBy = InitiatedBy.SHIPPER,
                    isCounterOffer = true,
                    counterOfferRound = 2,
                    remainingCounterOffers = 0,
                    canCounterOffer = false,
                ),
                currentUserId = 1, // viewer is carrier (not the counter-offerer)
                onDismiss = {},
            )
        }
    }
}
