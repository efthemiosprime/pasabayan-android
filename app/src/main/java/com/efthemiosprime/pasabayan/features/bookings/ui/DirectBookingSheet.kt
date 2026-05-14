package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.DirectBookingFormState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.DirectBookingSubmissionState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.DirectBookingViewModel

/**
 * Shipper "Book this trip" sheet — direct booking, no negotiation. iOS parity:
 * `Features/Bookings/Views/Carrier/DirectBookingSheet.swift`.
 *
 * Owns form input + submission via [DirectBookingViewModel]. The host watches
 * for [DirectBookingSubmissionState.Success] and dismisses the sheet; this
 * composable surfaces the error inline so the form's values stay editable.
 */
@Composable
fun DirectBookingSheet(
    trip: CarrierTripInfo,
    onClose: () -> Unit,
    onBooked: (com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectBookingViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsState()
    val submission by viewModel.submissionState.collectAsState()

    LaunchedEffect(submission) {
        (submission as? DirectBookingSubmissionState.Success)?.let { onBooked(it.booking) }
    }

    DirectBookingSheetContent(
        trip = trip,
        form = form,
        submission = submission,
        onClose = onClose,
        onSpaceNeededChange = viewModel::updateSpaceNeeded,
        onWeightNeededChange = viewModel::updateWeightNeeded,
        onPickupLocationChange = viewModel::updatePickupLocation,
        onDeliveryLocationChange = viewModel::updateDeliveryLocation,
        onPriceAgreedChange = viewModel::updatePriceAgreed,
        onServiceFeeChange = viewModel::updateServiceFee,
        onSpecialRequirementsChange = viewModel::updateSpecialRequirements,
        onSubmit = { viewModel.submit(trip.id) },
        onDismissError = viewModel::clearError,
        modifier = modifier,
    )
}

@Composable
internal fun DirectBookingSheetContent(
    trip: CarrierTripInfo,
    form: DirectBookingFormState,
    submission: DirectBookingSubmissionState,
    onClose: () -> Unit,
    onSpaceNeededChange: (String) -> Unit,
    onWeightNeededChange: (String) -> Unit,
    onPickupLocationChange: (String) -> Unit,
    onDeliveryLocationChange: (String) -> Unit,
    onPriceAgreedChange: (String) -> Unit,
    onServiceFeeChange: (String) -> Unit,
    onSpecialRequirementsChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSubmitting = submission is DirectBookingSubmissionState.Submitting
    val errorMessage = (submission as? DirectBookingSubmissionState.Error)?.message

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        HeaderRow(onClose = onClose)
        TripSummaryCard(trip = trip)

        errorMessage?.let { ErrorBanner(message = it, onDismiss = onDismissError) }

        SectionCard(title = stringResource(R.string.bookings_direct_section_requirements)) {
            NumericField(
                value = form.spaceNeeded,
                onValueChange = onSpaceNeededChange,
                label = stringResource(R.string.bookings_direct_space_label),
                placeholder = stringResource(R.string.bookings_direct_optional),
            )
            NumericField(
                value = form.weightNeeded,
                onValueChange = onWeightNeededChange,
                label = stringResource(R.string.bookings_direct_weight_label),
                placeholder = stringResource(R.string.bookings_direct_optional),
            )
        }

        SectionCard(title = stringResource(R.string.bookings_direct_section_locations)) {
            POutlinedTextField(
                value = form.pickupLocation,
                onValueChange = onPickupLocationChange,
                label = { Text(stringResource(R.string.bookings_direct_pickup_label)) },
                placeholder = { Text(stringResource(R.string.bookings_direct_pickup_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = form.deliveryLocation,
                onValueChange = onDeliveryLocationChange,
                label = { Text(stringResource(R.string.bookings_direct_delivery_label)) },
                placeholder = { Text(stringResource(R.string.bookings_direct_delivery_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SectionCard(title = stringResource(R.string.bookings_direct_section_pricing)) {
            NumericField(
                value = form.priceAgreed,
                onValueChange = onPriceAgreedChange,
                label = stringResource(R.string.bookings_direct_price_label),
            )
            NumericField(
                value = form.serviceFee,
                onValueChange = onServiceFeeChange,
                label = stringResource(R.string.bookings_direct_service_fee_label),
                placeholder = stringResource(R.string.bookings_direct_optional),
            )
            if (form.calculatedTotal > 0.0) {
                PDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.bookings_direct_total_label),
                        style = PasabayanTextStyles.Body.medium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.bookings_direct_total_value, form.calculatedTotal),
                        style = PasabayanTextStyles.Heading.h5,
                        fontWeight = FontWeight.Bold,
                        color = PasabayanColors.Success,
                    )
                }
            }
        }

        SectionCard(title = stringResource(R.string.bookings_direct_section_special)) {
            POutlinedTextField(
                value = form.specialRequirements,
                onValueChange = onSpecialRequirementsChange,
                label = { Text(stringResource(R.string.bookings_direct_section_special)) },
                placeholder = { Text(stringResource(R.string.bookings_direct_special_placeholder)) },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PButton(
            text = if (isSubmitting) {
                stringResource(R.string.bookings_direct_booking_in_progress)
            } else {
                stringResource(R.string.bookings_direct_book_now)
            },
            onClick = onSubmit,
            enabled = form.isValid && !isSubmitting,
            isLoading = isSubmitting,
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
            text = stringResource(R.string.bookings_direct_title),
            style = PasabayanTextStyles.Heading.h4,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        PButton(
            text = stringResource(R.string.bookings_direct_close),
            onClick = onClose,
            style = PButtonStyle.Tertiary,
            size = PButtonSize.Small,
        )
    }
}

@Composable
private fun TripSummaryCard(trip: CarrierTripInfo) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.bookings_direct_section_trip),
                style = PasabayanTextStyles.Heading.h6,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PRouteSection(
                origin = trip.originCity,
                destination = trip.destinationCity,
            )
            trip.availableWeightKg?.let { weight ->
                PDetailRow(
                    label = stringResource(R.string.bookings_direct_weight_available, weight),
                    value = trip.pricePerKg?.let {
                        stringResource(R.string.bookings_direct_per_kg_rate, it)
                    } ?: "",
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h6,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
) {
    POutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { p -> { Text(p) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.bookings_direct_error_title),
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
                text = stringResource(R.string.bookings_direct_try_again),
                onClick = onDismiss,
                style = PButtonStyle.Tertiary,
                size = PButtonSize.Small,
            )
        }
    }
}

// -- Previews -----------------------------------------------------------------

private fun previewTrip(): CarrierTripInfo = CarrierTripInfo(
    id = 42,
    originCity = "Manila",
    destinationCity = "Cebu",
    departureDate = "2026-05-25",
    arrivalDate = "2026-05-26",
    transportationMethod = "flight",
    availableWeightKg = 20.0,
    pricePerKg = 7.50,
    flatTripPrice = null,
    pricingType = "per_kg",
)

@Preview(showBackground = true, name = "DirectBooking — empty", heightDp = 1200)
@Preview(showBackground = true, name = "DirectBooking — empty dark", heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DirectBookingSheetEmptyPreview() {
    PasabayanTheme {
        DirectBookingSheetContent(
            trip = previewTrip(),
            form = DirectBookingFormState(),
            submission = DirectBookingSubmissionState.Idle,
            onClose = {}, onSpaceNeededChange = {}, onWeightNeededChange = {},
            onPickupLocationChange = {}, onDeliveryLocationChange = {},
            onPriceAgreedChange = {}, onServiceFeeChange = {},
            onSpecialRequirementsChange = {}, onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "DirectBooking — filled", heightDp = 1200)
@Composable
private fun DirectBookingSheetFilledPreview() {
    PasabayanTheme {
        DirectBookingSheetContent(
            trip = previewTrip(),
            form = DirectBookingFormState(
                spaceNeeded = "10",
                weightNeeded = "5",
                pickupLocation = "1234 Mock St",
                deliveryLocation = "4321 Test Ave",
                priceAgreed = "120",
                serviceFee = "15",
                specialRequirements = "Handle with care — fragile contents.",
            ),
            submission = DirectBookingSubmissionState.Idle,
            onClose = {}, onSpaceNeededChange = {}, onWeightNeededChange = {},
            onPickupLocationChange = {}, onDeliveryLocationChange = {},
            onPriceAgreedChange = {}, onServiceFeeChange = {},
            onSpecialRequirementsChange = {}, onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "DirectBooking — submitting", heightDp = 1200)
@Composable
private fun DirectBookingSheetSubmittingPreview() {
    PasabayanTheme {
        DirectBookingSheetContent(
            trip = previewTrip(),
            form = DirectBookingFormState(priceAgreed = "120"),
            submission = DirectBookingSubmissionState.Submitting,
            onClose = {}, onSpaceNeededChange = {}, onWeightNeededChange = {},
            onPickupLocationChange = {}, onDeliveryLocationChange = {},
            onPriceAgreedChange = {}, onServiceFeeChange = {},
            onSpecialRequirementsChange = {}, onSubmit = {}, onDismissError = {},
        )
    }
}

@Preview(showBackground = true, name = "DirectBooking — error", heightDp = 1200)
@Composable
private fun DirectBookingSheetErrorPreview() {
    PasabayanTheme {
        DirectBookingSheetContent(
            trip = previewTrip(),
            form = DirectBookingFormState(priceAgreed = "120"),
            submission = DirectBookingSubmissionState.Error("Trip is already at capacity."),
            onClose = {}, onSpaceNeededChange = {}, onWeightNeededChange = {},
            onPickupLocationChange = {}, onDeliveryLocationChange = {},
            onPriceAgreedChange = {}, onServiceFeeChange = {},
            onSpecialRequirementsChange = {}, onSubmit = {}, onDismissError = {},
        )
    }
}
