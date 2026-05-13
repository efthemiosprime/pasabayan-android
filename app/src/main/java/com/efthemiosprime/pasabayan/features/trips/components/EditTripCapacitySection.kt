package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

/**
 * Editable capacity section for `EditTripSheet`. Mirrors iOS `EditTripSheet.swift:824-892`
 * (`availableWeight` + `availableSpace`). When the trip has left planning, both inputs
 * render disabled — iOS `canEditDetails` is false outside planning.
 */
@Composable
fun EditTripCapacitySection(
    weightText: String,
    onWeightChange: (String) -> Unit,
    spaceText: String,
    onSpaceChange: (String) -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PDetailSectionTitle(text = stringResource(R.string.trips_edit_capacity_section))
        POutlinedTextField(
            value = weightText,
            onValueChange = { if (!locked) onWeightChange(it) },
            label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        POutlinedTextField(
            value = spaceText,
            onValueChange = { if (!locked) onSpaceChange(it) },
            label = { Text(stringResource(R.string.trips_create_space_capacity)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

@Preview(showBackground = true, name = "EditTripCapacitySection — planning, light")
@Preview(showBackground = true, name = "EditTripCapacitySection — planning, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripCapacityPlanningPreview() {
    var weight by remember { mutableStateOf("25") }
    var space by remember { mutableStateOf("50") }
    PasabayanTheme {
        EditTripCapacitySection(
            weightText = weight, onWeightChange = { weight = it },
            spaceText = space, onSpaceChange = { space = it },
            locked = false,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripCapacitySection — locked, light")
@Preview(showBackground = true, name = "EditTripCapacitySection — locked, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripCapacityLockedPreview() {
    PasabayanTheme {
        EditTripCapacitySection(
            weightText = "25", onWeightChange = {},
            spaceText = "50", onSpaceChange = {},
            locked = true,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
