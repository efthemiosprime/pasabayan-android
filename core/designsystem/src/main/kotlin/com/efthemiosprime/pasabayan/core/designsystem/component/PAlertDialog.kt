package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius

/**
 * Design-system wrapper over Material 3 [AlertDialog]. Pins shape, sensible
 * defaults, and a single switch for destructive confirms (paints the confirm
 * button text in `colorScheme.error`).
 *
 * Use [message] for plain string body content (most cases). Use [content]
 * when the body needs a composable — e.g. embedding a `TimePicker` /
 * `DatePicker` / a custom layout.
 *
 * The underlying [AlertDialog] is already theme-aware (`colorScheme.surface`
 * + `onSurface`) so dark mode works without overrides.
 */
@Composable
fun PAlertDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    modifier: Modifier = Modifier,
    /** Plain-string body. Ignored if [content] is non-null. */
    message: String? = null,
    /** Composable body for richer layouts (pickers, lists, etc.). */
    content: @Composable (() -> Unit)? = null,
    /** When non-null renders a dismiss button alongside confirm. */
    dismissText: String? = null,
    /** Paints the confirm button text in `colorScheme.error`. */
    isDestructive: Boolean = false,
    /** Optional leading icon, rendered above the title. */
    icon: ImageVector? = null,
    /** Tint for [icon]; defaults to `colorScheme.primary`. */
    iconTint: Color? = null,
    /** Forwarded to the confirm button for ui-test targeting. */
    confirmModifier: Modifier = Modifier,
    /** Forwarded to the dismiss button for ui-test targeting. */
    dismissModifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanRadius.modal),
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTint ?: MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = { Text(title) },
        text = when {
            content != null -> content
            message != null -> {
                { Text(message) }
            }
            else -> null
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = confirmModifier) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else Color.Unspecified,
                )
            }
        },
        dismissButton = dismissText?.let {
            {
                TextButton(onClick = onDismiss, modifier = dismissModifier) {
                    Text(it)
                }
            }
        },
    )
}
