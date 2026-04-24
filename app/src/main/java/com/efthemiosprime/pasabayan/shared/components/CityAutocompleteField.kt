package com.efthemiosprime.pasabayan.shared.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

@Composable
fun CityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        POutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )

        if (suggestions.isNotEmpty()) {
            PCard(
                variant = PCardVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 192.dp),
                ) {
                    items(suggestions, key = { it }) { city ->
                        Text(
                            text = city,
                            style = PasabayanTextStyles.Body.medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(city) }
                                .padding(
                                    horizontal = PasabayanSpacing.md,
                                    vertical = PasabayanSpacing.sm,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "CityAutocompleteField - light")
@Preview(
    showBackground = true,
    name = "CityAutocompleteField - dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CityAutocompleteFieldPreview() {
    var value by remember { mutableStateOf("To") }
    PasabayanTheme {
        CityAutocompleteField(
            value = value,
            onValueChange = { value = it },
            label = { Text(stringResource(R.string.trips_create_origin)) },
            suggestions = listOf("Toronto", "Victoria"),
            onSuggestionSelected = { value = it },
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
