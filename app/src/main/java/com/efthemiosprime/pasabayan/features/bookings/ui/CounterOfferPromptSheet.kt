package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.bookings.services.CounterOfferPromptValidation
import com.efthemiosprime.pasabayan.features.bookings.services.CounterOfferPromptValidator
import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig

/**
 * Sheet for proposing a counter-offer. Parity with iOS
 * `CounterOfferPromptView.swift`.
 *
 * @param currentPrice the price the user just declined (rendered with
 *   strikethrough in the declined-price card).
 * @param remainingOffers number of counter-offers the user has left in
 *   this exchange. Rendered as caption text; Android-only context
 *   (iOS surfaces this only in the banner).
 * @param referencePrice optional own-side reference price — the carrier's
 *   rate when used carrier-side, the shipper's budget when used
 *   shipper-side. Rendered only when greater than zero.
 * @param minDeliveryPrice the floor accepted by Stripe — used to gate
 *   submission and shown as a hint when the input is empty.
 * @param isSubmitting drives the submit button's loading state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterOfferPromptSheet(
    currentPrice: Double,
    remainingOffers: Int,
    onSubmit: (newPrice: Double, message: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    referencePrice: Double? = null,
    referencePriceLabel: String? = null,
    minDeliveryPrice: Double = StripeConfig.DEFAULT_MIN_DELIVERY_PRICE,
    isSubmitting: Boolean = false,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        var rawPrice by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        val validation = CounterOfferPromptValidator.validate(
            rawInput = rawPrice,
            originalPrice = currentPrice,
            minDeliveryPrice = minDeliveryPrice,
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            // Title
            Text(
                text = stringResource(R.string.bookings_counter_offer_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.bookings_counter_offer_header),
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Declined price card — strikethrough so the user sees what they're moving away from.
            PCard(variant = PCardVariant.Secondary, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.bookings_counter_offer_declined_price),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = String.format(Locale.ROOT, "$%.2f", currentPrice),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
            }

            // Reference price card (carrier rate / shipper budget) — only when meaningful.
            if (referencePrice != null && referencePrice > 0.0 && referencePriceLabel != null) {
                PCard(variant = PCardVariant.Secondary, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = referencePriceLabel,
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = String.format(Locale.ROOT, "$%.2f", referencePrice),
                        style = PasabayanTextStyles.Body.medium,
                        color = PasabayanColors.Info,
                    )
                }
            }

            // Remaining offers caption (Android-only — iOS surfaces this in the banner).
            if (remainingOffers > 0) {
                Text(
                    text = stringResource(R.string.bookings_detail_remaining_offers, remainingOffers),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Counter-offer price input.
            val errorText = errorText(validation)
            val hintText = stringResource(
                R.string.bookings_counter_offer_minimum_hint,
                String.format(Locale.ROOT, "$%.2f", minDeliveryPrice),
            )
            POutlinedTextField(
                value = rawPrice,
                onValueChange = { rawPrice = it },
                label = { Text(stringResource(R.string.bookings_counter_offer_your_price)) },
                leadingIcon = {
                    Text(
                        text = "$",
                        style = PasabayanTextStyles.Body.medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                isError = errorText != null,
                supportingText = {
                    Text(
                        text = errorText ?: hintText,
                        style = PasabayanTextStyles.Caption.regular,
                        color = if (errorText != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )

            // Optional message.
            POutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.bookings_counter_offer_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                enabled = !isSubmitting,
            )

            // Submit — only enabled on Valid; shows loading spinner when in flight.
            PButton(
                text = stringResource(R.string.bookings_counter_offer_submit),
                onClick = {
                    (validation as? CounterOfferPromptValidation.Valid)?.let { v ->
                        onSubmit(v.price, message.ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = validation is CounterOfferPromptValidation.Valid,
                isLoading = isSubmitting,
            )

            // Tertiary cancel — explicit out without closing the sheet by gesture.
            PButton(
                text = stringResource(R.string.bookings_counter_offer_no_thanks),
                onClick = onDismiss,
                style = PButtonStyle.Tertiary,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun errorText(validation: CounterOfferPromptValidation): String? = when (validation) {
    CounterOfferPromptValidation.Empty,
    CounterOfferPromptValidation.NotNumeric,
    is CounterOfferPromptValidation.Valid -> null
    CounterOfferPromptValidation.NotPositive ->
        stringResource(R.string.bookings_counter_offer_error_positive)
    is CounterOfferPromptValidation.BelowMinimum ->
        stringResource(
            R.string.bookings_counter_offer_error_minimum,
            String.format(Locale.ROOT, "$%.2f", validation.minimum),
        )
    CounterOfferPromptValidation.SameAsOriginal ->
        stringResource(R.string.bookings_counter_offer_error_different)
}

@Preview(showBackground = true, name = "Prompt — light")
@Preview(showBackground = true, name = "Prompt — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferPromptSheetPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            // PModalBottomSheet is non-trivial to preview directly; render the inner
            // content area against the theme so designers can iterate on layout.
            CounterOfferPromptSheet(
                currentPrice = 100.0,
                remainingOffers = 2,
                onSubmit = { _, _ -> },
                onDismiss = {},
                referencePrice = 120.0,
                referencePriceLabel = "Your rate",
                minDeliveryPrice = 5.0,
            )
        }
    }
}
