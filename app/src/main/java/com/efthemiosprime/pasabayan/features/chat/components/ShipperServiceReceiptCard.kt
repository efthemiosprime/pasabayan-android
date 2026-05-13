package com.efthemiosprime.pasabayan.features.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.bookings.model.MatchReceipt

/**
 * Inline receipt card rendered in the shipper's chat thread once the
 * carrier has uploaded a receipt. Mirrors iOS `ShipperServiceReceiptCard`
 * (ConversationDetailView.swift). Tap → host can launch a full-screen
 * viewer; until that surface lands [onExpand] can be a no-op.
 */
@Composable
fun ShipperServiceReceiptCard(
    receipt: MatchReceipt,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(PasabayanSpacing.xxl),
            )
            Spacer(Modifier.width(PasabayanSpacing.sm))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.chat_receipt_card_title),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                val timestampLabel = receipt.uploadedAt?.takeIf { it.isNotBlank() }
                    ?.let { stringResource(R.string.chat_receipt_card_uploaded_at, it) }
                    ?: stringResource(R.string.chat_receipt_card_uploaded_unknown)
                Text(
                    text = timestampLabel,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(PasabayanSpacing.sm))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(receipt.receiptUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.chat_receipt_card_image_description),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(RECEIPT_IMAGE_HEIGHT)
                .clip(RoundedCornerShape(PasabayanRadius.sm)),
        )
    }
}

private val RECEIPT_IMAGE_HEIGHT = 200.dp

@Preview(showBackground = true, name = "Receipt card — light")
@Preview(showBackground = true, name = "Receipt card — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShipperServiceReceiptCardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            ShipperServiceReceiptCard(
                receipt = MatchReceipt(
                    receiptPhoto = "receipts/match-100.jpg",
                    receiptUrl = "https://placehold.co/600x400",
                    uploadedAt = "2026-04-01T12:00:00Z",
                ),
                onExpand = {},
            )
            ShipperServiceReceiptCard(
                receipt = MatchReceipt(
                    receiptPhoto = "receipts/match-100.jpg",
                    receiptUrl = "https://placehold.co/600x400",
                    uploadedAt = null,
                ),
                onExpand = {},
            )
        }
    }
}
