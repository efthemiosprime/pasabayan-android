package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold

@Composable
fun ChatReceiptUploadSheet(
    onUploadFromCamera: () -> Unit,
    onUploadFromGallery: () -> Unit,
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
            Text(text = stringResource(R.string.chat_receipt_upload_description))
            PButton(
                text = stringResource(R.string.chat_receipt_upload_camera),
                onClick = onUploadFromCamera,
            )
            PButton(
                text = stringResource(R.string.chat_receipt_upload_gallery),
                onClick = onUploadFromGallery,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Preview(showBackground = true, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatReceiptUploadSheetPreview() {
    PasabayanTheme {
        ChatReceiptUploadSheet(
            onUploadFromCamera = {},
            onUploadFromGallery = {},
            onClose = {},
        )
    }
}

