package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ChatReceiptUploadState

/**
 * Stateless receipt-upload sheet. The host launches the Android Photo
 * Picker on [onPickPhoto] and feeds the resulting bytes to the VM, which
 * drives [state].
 *
 * Idle → user taps "Choose photo" → host launches picker.
 * Uploading → spinner + label.
 * Success → checkmark; host auto-dismisses on this state.
 * Error → banner + "Try again" routes back to [onPickPhoto].
 */
@Composable
fun ChatReceiptUploadSheet(
    state: ChatReceiptUploadState,
    onPickPhoto: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PDetailSheetScaffold(
        modifier = modifier,
        title = stringResource(R.string.chat_receipt_upload_title),
        closeContentDescription = stringResource(R.string.chat_receipt_upload_close),
        onClose = onClose,
    ) {
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            when (state) {
                ChatReceiptUploadState.Idle -> IdleBody(onPickPhoto = onPickPhoto)
                ChatReceiptUploadState.Uploading -> UploadingBody()
                ChatReceiptUploadState.Success -> SuccessBody()
                is ChatReceiptUploadState.Error -> ErrorBody(message = state.message, onRetry = onPickPhoto)
            }
        }
    }
}

@Composable
private fun IdleBody(onPickPhoto: () -> Unit) {
    Text(
        text = stringResource(R.string.chat_receipt_upload_description),
        style = PasabayanTextStyles.Body.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.size(PasabayanSpacing.sm))
    PButton(
        text = stringResource(R.string.chat_receipt_upload_choose),
        onClick = onPickPhoto,
        icon = Icons.Filled.PhotoLibrary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun UploadingBody() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PCircularProgress(modifier = Modifier.size(PasabayanSpacing.xxl))
        Spacer(Modifier.width(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.chat_receipt_upload_uploading),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SuccessBody() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = PasabayanColors.Success,
            modifier = Modifier.size(PasabayanSpacing.xxxl),
        )
        Spacer(Modifier.width(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.chat_receipt_upload_success),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ErrorBody(message: String, onRetry: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = PasabayanColors.Error,
            modifier = Modifier.size(PasabayanSpacing.xxxl),
        )
        Spacer(Modifier.width(PasabayanSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.chat_receipt_upload_error_generic),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.size(PasabayanSpacing.sm))
    PButton(
        text = stringResource(R.string.chat_receipt_upload_retry),
        onClick = onRetry,
        style = PButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true, name = "Receipt — Idle — light")
@Preview(showBackground = true, name = "Receipt — Idle — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptIdlePreview() {
    PasabayanTheme {
        ChatReceiptUploadSheet(state = ChatReceiptUploadState.Idle, onPickPhoto = {}, onClose = {})
    }
}

@Preview(showBackground = true, name = "Receipt — Uploading — light")
@Preview(showBackground = true, name = "Receipt — Uploading — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptUploadingPreview() {
    PasabayanTheme {
        ChatReceiptUploadSheet(state = ChatReceiptUploadState.Uploading, onPickPhoto = {}, onClose = {})
    }
}

@Preview(showBackground = true, name = "Receipt — Success — light")
@Preview(showBackground = true, name = "Receipt — Success — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptSuccessPreview() {
    PasabayanTheme {
        ChatReceiptUploadSheet(state = ChatReceiptUploadState.Success, onPickPhoto = {}, onClose = {})
    }
}

@Preview(showBackground = true, name = "Receipt — Error — light")
@Preview(showBackground = true, name = "Receipt — Error — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptErrorPreview() {
    PasabayanTheme {
        ChatReceiptUploadSheet(
            state = ChatReceiptUploadState.Error("Network error"),
            onPickPhoto = {},
            onClose = {},
        )
    }
}
