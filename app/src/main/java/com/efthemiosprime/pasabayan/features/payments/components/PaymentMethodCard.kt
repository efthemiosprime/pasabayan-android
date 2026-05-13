package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay

@Composable
fun PaymentMethodCard(
    method: PaymentMethodDisplay,
    onSetDefault: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var removeDialogOpen by remember { mutableStateOf(false) }

    PCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            BrandIcon(brand = method.brand)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = method.expiryDisplay,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (method.isDefault) DefaultBadge()
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.payments_methods_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (!method.isDefault) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.payments_methods_set_default)) },
                            onClick = {
                                menuOpen = false
                                onSetDefault()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.payments_methods_remove),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            removeDialogOpen = true
                        },
                    )
                }
            }
        }
    }

    if (removeDialogOpen) {
        PAlertDialog(
            title = stringResource(R.string.payments_methods_remove_confirm_title),
            message = stringResource(R.string.payments_methods_remove_confirm_body),
            confirmText = stringResource(R.string.payments_methods_remove),
            dismissText = stringResource(R.string.payments_methods_remove_confirm_cancel),
            isDestructive = true,
            onConfirm = {
                removeDialogOpen = false
                onRemove()
            },
            onDismiss = { removeDialogOpen = false },
        )
    }
}

@Composable
private fun BrandIcon(brand: String) {
    val color = brandColor(brand)
    Box(
        modifier = Modifier
            .size(BrandIconSize)
            .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(BrandIconInnerSize),
            tint = color,
        )
    }
}

@Composable
private fun DefaultBadge() {
    Text(
        text = stringResource(R.string.payments_methods_default_badge),
        style = PasabayanTextStyles.Caption.small,
        color = PasabayanColors.Success,
        modifier = Modifier
            .background(
                PasabayanColors.Success.copy(alpha = 0.15f),
                RoundedCornerShape(PasabayanRadius.badge),
            )
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    )
}

/** iOS-parity brand-color mapping. Pasabayan tokens — no hex literals. */
private fun brandColor(brand: String): Color = when (brand.lowercase()) {
    "visa" -> PasabayanColors.BadgeBlue
    "mastercard" -> PasabayanColors.BadgeOrange
    "amex", "american express" -> PasabayanColors.BadgeGreen
    "discover" -> PasabayanColors.BadgePurple
    else -> PasabayanColors.BadgeGray
}

private val BrandIconSize = 40.dp
private val BrandIconInnerSize = 22.dp

@Preview(showBackground = true, name = "PaymentMethodCard light")
@Preview(showBackground = true, name = "PaymentMethodCard dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentMethodCardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PaymentMethodCard(
                method = PaymentMethodDisplay("pm_v", "visa", "4242", 12, 2028, isDefault = true),
                onSetDefault = {}, onRemove = {},
            )
            PaymentMethodCard(
                method = PaymentMethodDisplay("pm_m", "mastercard", "5555", 6, 2027, isDefault = false),
                onSetDefault = {}, onRemove = {},
            )
            PaymentMethodCard(
                method = PaymentMethodDisplay("pm_a", "amex", "0005", 1, 2029, isDefault = false),
                onSetDefault = {}, onRemove = {},
            )
            PaymentMethodCard(
                method = PaymentMethodDisplay("pm_d", "discover", "6011", 9, 2030, isDefault = false),
                onSetDefault = {}, onRemove = {},
            )
            PaymentMethodCard(
                method = PaymentMethodDisplay("pm_u", "unknown", "0000", 3, 2026, isDefault = false),
                onSetDefault = {}, onRemove = {},
            )
        }
    }
}
