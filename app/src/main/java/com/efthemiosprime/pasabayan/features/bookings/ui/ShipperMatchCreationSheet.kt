package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ShipperMatchCreationSheet(
    trip: Trip,
    pendingPackages: List<PackageRequest>,
    isSubmitting: Boolean,
    requestErrorMessage: String?,
    requestSuccessMessage: String?,
    onSubmit: (packageId: Int, offeredPrice: Double, message: String?) -> Unit,
    onSuccessDone: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPackageId by remember { mutableStateOf<Int?>(null) }
    var offeredPrice by remember { mutableStateOf(String.format("%.2f", trip.effectivePrice)) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(pendingPackages) {
        if (selectedPackageId == null && pendingPackages.isNotEmpty()) {
            selectedPackageId = pendingPackages.first().id
        }
    }

    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            if (!requestSuccessMessage.isNullOrBlank()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.bookings_success_title),
                    style = PasabayanTextStyles.Heading.h4,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = requestSuccessMessage,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PButton(
                    text = androidx.compose.ui.res.stringResource(R.string.bookings_success_done),
                    onClick = onSuccessDone,
                    modifier = Modifier.fillMaxWidth(),
                )
                return@Column
            }

            Text(
                text = androidx.compose.ui.res.stringResource(R.string.bookings_match_creation_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = androidx.compose.ui.res.stringResource(R.string.bookings_match_creation_choose_package),
                style = PasabayanTextStyles.Caption.large,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (pendingPackages.isEmpty()) {
                PCard {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.bookings_match_creation_no_packages),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                pendingPackages.forEach { pkg ->
                    val isSelected = selectedPackageId == pkg.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { selectedPackageId = pkg.id }
                            .padding(PasabayanSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                            Text(
                                text = pkg.title,
                                style = PasabayanTextStyles.Body.medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${pkg.pickupCity.orEmpty()} → ${pkg.deliveryCity.orEmpty()}",
                                style = PasabayanTextStyles.Caption.regular,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = if (isSelected) "✓" else "",
                            style = PasabayanTextStyles.Body.medium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            POutlinedTextField(
                value = offeredPrice,
                onValueChange = { offeredPrice = it },
                label = { Text(androidx.compose.ui.res.stringResource(R.string.bookings_request_to_carry_price)) },
                modifier = Modifier.fillMaxWidth(),
            )

            POutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(androidx.compose.ui.res.stringResource(R.string.bookings_request_to_carry_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
            )

            if (!requestErrorMessage.isNullOrBlank()) {
                Text(
                    text = requestErrorMessage,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            val parsedPrice = offeredPrice.toDoubleOrNull()
            PButton(
                text = androidx.compose.ui.res.stringResource(R.string.bookings_request_to_carry_submit),
                onClick = {
                    val packageId = selectedPackageId ?: return@PButton
                    val price = parsedPrice ?: return@PButton
                    onSubmit(packageId, price, message.takeIf { it.isNotBlank() })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && selectedPackageId != null && parsedPrice != null && parsedPrice > 0,
            )
        }
    }
}

@Preview(showBackground = true, name = "ShipperMatchCreationSheet - light")
@Preview(showBackground = true, name = "ShipperMatchCreationSheet - dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShipperMatchCreationSheetPreview() {
    PasabayanTheme {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.bookings_match_creation_title),
            modifier = Modifier.padding(16.dp),
        )
    }
}
