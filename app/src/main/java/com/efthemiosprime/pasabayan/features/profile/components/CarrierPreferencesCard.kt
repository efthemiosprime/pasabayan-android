package com.efthemiosprime.pasabayan.features.profile.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.PreferredPickupCityJson

/**
 * Read-only summary of the carrier's saved trip defaults — iOS parity with
 * `CarrierPreferencesSection.swift` (`.standalone` style). Carrier-only.
 *
 * Tapping the "Edit" affordance opens the existing carrier-profile edit sheet
 * (same destination as the Vehicle info menu row).
 *
 * NOTE: iOS also shows "Usual transport" sourced from a per-user preference
 * store. That store has no Android equivalent yet; the row is intentionally
 * omitted here and tracked as a follow-up.
 */
@Composable
fun CarrierPreferencesCard(
    carrierProfile: CarrierProfileJson?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notSet = stringResource(R.string.profile_carrier_preferences_not_set)
    val pickupCity = carrierProfile?.preferredPickupCity?.displayName() ?: notSet
    val maxWeight = carrierProfile?.maxWeightCapacityKg
        ?.takeIf { it > 0 }
        ?.let { stringResource(R.string.profile_carrier_preferences_max_weight_value, it) }
        ?: notSet
    val maxSpace = carrierProfile?.maxSpaceCapacityLiters
        ?.takeIf { it > 0 }
        ?.let { stringResource(R.string.profile_carrier_preferences_max_space_value, it) }
        ?: notSet

    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_carrier_preferences_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PDetailRow(
                    label = stringResource(R.string.profile_carrier_preferences_pickup_city),
                    value = pickupCity,
                )
                PDetailRow(
                    label = stringResource(R.string.profile_carrier_preferences_max_weight),
                    value = maxWeight,
                )
                PDetailRow(
                    label = stringResource(R.string.profile_carrier_preferences_max_space),
                    value = maxSpace,
                )
            }
            PDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEdit),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_carrier_preferences_edit),
                    style = PasabayanTextStyles.Body.medium,
                    color = PasabayanColors.Info,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PasabayanColors.Info,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(18.dp),
                )
            }
        }
    }
}

private fun PreferredPickupCityJson.displayName(): String {
    if (name.isBlank()) return ""
    return if (stateCode.isNotBlank()) "$name, $stateCode" else name
}

@Preview(showBackground = true, name = "CarrierPreferences — light")
@Preview(showBackground = true, name = "CarrierPreferences — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierPreferencesCardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            CarrierPreferencesCard(
                carrierProfile = CarrierProfileJson(
                    preferredPickupCity = PreferredPickupCityJson(
                        id = 1,
                        name = "Montreal",
                        stateCode = "QC",
                    ),
                    maxWeightCapacityKg = 25.0,
                    maxSpaceCapacityLiters = 500.0,
                ),
                onEdit = {},
            )
            CarrierPreferencesCard(
                carrierProfile = null,
                onEdit = {},
            )
        }
    }
}
