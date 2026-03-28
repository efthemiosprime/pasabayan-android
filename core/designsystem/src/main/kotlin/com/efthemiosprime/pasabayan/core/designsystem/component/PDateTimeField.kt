package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Read-only text field that triggers a date/time picker on click.
 * The actual DatePickerDialog/TimePickerDialog is provided by the caller
 * since picker configuration is feature-specific.
 */
@Composable
fun PDateTimeField(
    value: String,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    trailingIcon: ImageVector = Icons.Outlined.DateRange,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    POutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.clickable(onClick = onClick),
        readOnly = true,
        enabled = false,
        isError = isError,
        supportingText = supportingText,
        placeholder = placeholder?.let { { Text(it) } },
        trailingIcon = {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
            )
        },
    )
}
