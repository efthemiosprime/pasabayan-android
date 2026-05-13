package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.payments.model.OtherParty
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptAmount
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptDelivery

/**
 * Tappable list row for a receipt. Role icon (up/blue for shipper, down/green for carrier),
 * receipt number, route description, trailing amount + optional [TipBadge].
 */
@Composable
fun ReceiptRow(
    receipt: PaymentReceipt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            RoleIcon(isShipper = receipt.isShipper)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.receiptNumber,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = receipt.routeDescription,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                receipt.dateFormatted?.takeIf { it.isNotBlank() }?.let { date ->
                    Text(
                        text = date,
                        style = PasabayanTextStyles.Caption.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = receipt.displayAmount,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                receipt.amount.tip?.takeIf { it > 0.0 }?.let { tip ->
                    TipBadge(amount = tip, currency = receipt.amount.currency)
                }
            }
        }
    }
}

@Composable
private fun RoleIcon(isShipper: Boolean) {
    val color: Color = if (isShipper) PasabayanColors.BadgeBlue else PasabayanColors.BadgeGreen
    val icon = if (isShipper) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
    Box(
        modifier = Modifier
            .size(RoleIconSize)
            .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(RoleIconInnerSize),
            tint = color,
        )
    }
}

private val RoleIconSize = 36.dp
private val RoleIconInnerSize = 18.dp

private val shipperRoute = PaymentReceipt(
    id = 1, receiptNumber = "RCP-1001",
    date = "2026-05-12T10:00:00Z",
    dateFormatted = "May 12, 2026",
    role = "shipper",
    otherParty = OtherParty(id = 5, name = "John Carrier"),
    amount = PaymentReceiptAmount(total = 165.0, tip = 0.0, currency = "cad"),
    delivery = PaymentReceiptDelivery(pickupCity = "Toronto", deliveryCity = "Montreal"),
    status = "completed",
)

private val carrierWithTip = PaymentReceipt(
    id = 2, receiptNumber = "RCP-1002",
    date = "2026-05-11T16:00:00Z",
    dateFormatted = "May 11, 2026",
    role = "carrier",
    otherParty = OtherParty(id = 9, name = "Alice Shipper"),
    amount = PaymentReceiptAmount(total = 165.0, carrierAmount = 142.5, tip = 5.0, currency = "cad"),
    delivery = PaymentReceiptDelivery(pickupCity = "Ottawa", deliveryCity = "Kingston"),
    status = "completed",
)

private val partyFallback = PaymentReceipt(
    id = 3, receiptNumber = "RCP-1003",
    date = "2026-05-10T09:00:00Z",
    dateFormatted = null,
    role = "shipper",
    otherParty = OtherParty(id = 12, name = "Maria Carrier"),
    amount = PaymentReceiptAmount(total = 88.0, currency = "cad"),
    delivery = null, // route falls back to party name
    status = "completed",
)

@Preview(showBackground = true, name = "ReceiptRow light")
@Preview(showBackground = true, name = "ReceiptRow dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptRowPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            ReceiptRow(receipt = shipperRoute, onClick = {})
            ReceiptRow(receipt = carrierWithTip, onClick = {})
            ReceiptRow(receipt = partyFallback, onClick = {})
        }
    }
}

