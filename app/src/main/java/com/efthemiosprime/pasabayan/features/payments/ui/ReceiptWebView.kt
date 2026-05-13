package com.efthemiosprime.pasabayan.features.payments.ui

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress

/**
 * Loads [url] into an in-process [WebView]. Shows a centred progress while loading
 * and a localised error message when the page fails. The WebView is destroyed on
 * disposal — required to release native resources.
 *
 * JavaScript is enabled because Stripe receipt pages render via JS. The receipt URL
 * is server-issued from a trusted domain — third-party JS is not a concern here.
 */
@Composable
fun ReceiptWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorText: String? by remember { mutableStateOf(null) }
    var webViewRef: WebView? by remember { mutableStateOf(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    @Suppress("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            errorText = null
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true) {
                                errorText = error?.description?.toString()
                                isLoading = false
                            }
                        }
                    }
                    webViewRef = this
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) webView.loadUrl(url)
            },
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(PasabayanSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                PCircularProgress()
                Text(
                    text = stringResource(R.string.payments_receipt_webview_loading),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        errorText?.let { _ ->
            Text(
                text = stringResource(R.string.payments_receipt_webview_error),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(PasabayanSpacing.md),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    DisposableEffect(url) {
        onDispose { webViewRef?.destroy() }
    }
}
