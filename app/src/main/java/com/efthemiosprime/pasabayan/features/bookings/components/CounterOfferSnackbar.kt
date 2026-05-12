package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.features.bookings.model.CounterOfferContext

/**
 * Compact transient snackbar that confirms a counter-offer was sent.
 * Parity with iOS `CounterOfferSnackbar.swift`. Use with auto-dismiss in
 * the host screen (e.g. `delay(6_000)`).
 */
@Composable
fun CounterOfferSnackbar(
    context: CounterOfferContext,
    currentUserId: Int?,
    onViewOffer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(SNACKBAR_TEST_TAG),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = null,
                tint = PasabayanColors.BadgePurple,
                modifier = Modifier.size(PasabayanSpacing.xxl),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.formattedNewPrice,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = signedDiffText(context),
                        style = PasabayanTextStyles.Caption.regular,
                        color = priceChangeColor(context),
                    )
                    Text(
                        text = stringResource(
                            R.string.bookings_counter_offer_banner_from,
                            counterOffererDisplayName(currentUserId, context),
                        ),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            PButton(
                text = stringResource(R.string.bookings_counter_offer_view_offer),
                onClick = onViewOffer,
                size = PButtonSize.Small,
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(SNACKBAR_DISMISS_TEST_TAG),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.bookings_counter_offer_banner_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun signedDiffText(context: CounterOfferContext): String {
    val prefix = if (context.isLowerOffer) "-" else "+"
    return "$prefix${context.formattedDifference}"
}

private fun priceChangeColor(context: CounterOfferContext) =
    if (context.isLowerOffer) PasabayanColors.Success else PasabayanColors.Warning

@Composable
private fun counterOffererDisplayName(
    currentUserId: Int?,
    context: CounterOfferContext,
): String {
    val name = context.counterOffererName.ifBlank { stringResource(R.string.common_role_sender) }
    val isYou = currentUserId != null && currentUserId == context.counterOffererId
    return if (isYou) stringResource(R.string.bookings_counter_offer_you) else name
}

internal const val SNACKBAR_TEST_TAG = "CounterOffer.Snackbar"
internal const val SNACKBAR_DISMISS_TEST_TAG = "CounterOffer.Snackbar.Dismiss"

@Preview(showBackground = true, name = "Snackbar — Lower — light")
@Preview(showBackground = true, name = "Snackbar — Lower — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferSnackbarLowerPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            CounterOfferSnackbar(
                context = CounterOfferContext(
                    newPrice = 70.0,
                    originalPrice = 100.0,
                    counterOffererName = "Jey-Em",
                    counterOffererId = 123,
                    initiatedBy = InitiatedBy.CARRIER,
                    isCounterOffer = true,
                ),
                currentUserId = 1,
                onViewOffer = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Snackbar — Higher — light")
@Preview(showBackground = true, name = "Snackbar — Higher — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferSnackbarHigherPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            CounterOfferSnackbar(
                context = CounterOfferContext(
                    newPrice = 130.0,
                    originalPrice = 100.0,
                    counterOffererName = "Sarah",
                    counterOffererId = 456,
                    initiatedBy = InitiatedBy.SHIPPER,
                    isCounterOffer = true,
                ),
                currentUserId = 1,
                onViewOffer = {},
                onDismiss = {},
            )
        }
    }
}
