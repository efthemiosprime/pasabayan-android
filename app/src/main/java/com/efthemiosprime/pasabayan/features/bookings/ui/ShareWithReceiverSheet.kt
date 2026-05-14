package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.ShareWithReceiverState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.ShareWithReceiverViewModel

/**
 * Shipper-side sheet that issues a fresh receiver tracking link and PIN, and
 * exposes an Android share-intent for delivery to the receiver. iOS parity:
 * `Features/Bookings/Views/Shipper/ShareWithReceiverSheet.swift`.
 *
 * The VM runs `getReceiverAccess → revoke active → createReceiverAccess` on
 * appear; the share button launches `Intent.ACTION_SEND`.
 */
@Composable
fun ShareWithReceiverSheet(
    matchId: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareWithReceiverViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(matchId) { viewModel.start(matchId) }
    DisposableEffect(Unit) { onDispose { viewModel.cancel() } }

    ShareWithReceiverSheetContent(
        state = state,
        onClose = onClose,
        onRetry = { viewModel.start(matchId) },
        onShare = { token -> launchShareIntent(context, token) },
        modifier = modifier,
    )
}

@Composable
internal fun ShareWithReceiverSheetContent(
    state: ShareWithReceiverState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onShare: (ReceiverAccessToken) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.bookings_share_with_receiver_title),
                style = PasabayanTextStyles.Heading.h4,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            PButton(
                text = stringResource(R.string.bookings_share_with_receiver_close),
                onClick = onClose,
                style = PButtonStyle.Tertiary,
                size = PButtonSize.Small,
            )
        }

        when (state) {
            is ShareWithReceiverState.Loading -> LoadingContent()
            is ShareWithReceiverState.Success -> SuccessContent(token = state.token, onShare = { onShare(state.token) })
            is ShareWithReceiverState.Error -> ErrorContent(message = state.message, onRetry = onRetry)
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PasabayanSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        PCircularProgress()
        Text(
            text = stringResource(R.string.bookings_share_with_receiver_loading),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SuccessContent(token: ReceiverAccessToken, onShare: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PasabayanColors.Success,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = stringResource(R.string.bookings_share_with_receiver_success_title),
            style = PasabayanTextStyles.Heading.h4,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
            shape = RoundedCornerShape(PasabayanRadius.md),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(PasabayanSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                LinkRow(
                    label = stringResource(R.string.bookings_share_with_receiver_link_label),
                    value = token.shortUrl,
                )
                if (token.hasPin && token.pin != null) {
                    LinkRow(
                        label = stringResource(R.string.bookings_share_with_receiver_pin_label),
                        value = token.pin,
                    )
                }
            }
        }

        PButton(
            text = stringResource(R.string.bookings_share_with_receiver_share_action),
            onClick = onShare,
            icon = Icons.Default.Share,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.bookings_share_with_receiver_helper),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LinkRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.size(PasabayanSpacing.md))
        Text(
            text = value,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.bookings_share_with_receiver_error_title),
            style = PasabayanTextStyles.Heading.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PButton(
            text = stringResource(R.string.bookings_share_with_receiver_try_again),
            onClick = onRetry,
        )
    }
}

/**
 * Build the share message body and launch Android's chooser. Mirrors iOS
 * `formatShareMessage` (line 117 of ShareWithReceiverSheet.swift).
 */
private fun launchShareIntent(context: Context, token: ReceiverAccessToken) {
    val body = context.getString(R.string.bookings_share_with_receiver_message_body, token.shortUrl)
    val pinSuffix = if (token.hasPin && token.pin != null) {
        context.getString(R.string.bookings_share_with_receiver_message_pin_suffix, token.pin)
    } else {
        ""
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body + pinSuffix)
    }
    val chooser = Intent.createChooser(
        intent,
        context.getString(R.string.bookings_share_with_receiver_share_chooser),
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(chooser)
}

// -- Previews -----------------------------------------------------------------

private fun previewToken(hasPin: Boolean = true): ReceiverAccessToken = ReceiverAccessToken(
    id = 1,
    shortCode = "ABC123",
    shortUrl = "https://psb.to/ABC123",
    hasPin = hasPin,
    pin = if (hasPin) "4829" else null,
    pinNotice = null,
    isActive = true,
    accessCount = 0,
    trackingUrl = "https://app.pasabayan.com/track/ABC123",
    firstAccessedAt = null,
    lastAccessedAt = null,
    createdAt = "2026-05-14T09:00:00Z",
)

@Preview(showBackground = true, name = "Share — success with PIN")
@Preview(showBackground = true, name = "Share — success dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareSheetSuccessPreview() {
    PasabayanTheme {
        ShareWithReceiverSheetContent(
            state = ShareWithReceiverState.Success(previewToken()),
            onClose = {}, onRetry = {}, onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Share — success no PIN")
@Composable
private fun ShareSheetSuccessNoPinPreview() {
    PasabayanTheme {
        ShareWithReceiverSheetContent(
            state = ShareWithReceiverState.Success(previewToken(hasPin = false)),
            onClose = {}, onRetry = {}, onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Share — loading")
@Preview(showBackground = true, name = "Share — loading dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareSheetLoadingPreview() {
    PasabayanTheme {
        ShareWithReceiverSheetContent(
            state = ShareWithReceiverState.Loading,
            onClose = {}, onRetry = {}, onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Share — error")
@Composable
private fun ShareSheetErrorPreview() {
    PasabayanTheme {
        ShareWithReceiverSheetContent(
            state = ShareWithReceiverState.Error("Network unavailable"),
            onClose = {}, onRetry = {}, onShare = {},
        )
    }
}
