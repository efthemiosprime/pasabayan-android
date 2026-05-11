package com.efthemiosprime.pasabayan.features.legal.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renders an HTML article bundled under `assets/articles/`. JavaScript is intentionally
 * disabled — these documents are static legal/help content. External links are blocked so
 * the user can't navigate away from the agreement context.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AssetArticleWebView(
    filename: String,
    modifier: Modifier = Modifier,
) {
    var loading by remember(filename) { mutableStateOf(true) }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    settings.apply {
                        javaScriptEnabled = false
                        defaultTextEncodingName = "utf-8"
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = request?.url?.scheme?.startsWith("http") == true
                    }
                }
            },
            update = { webView ->
                loading = true
                webView.loadUrl("file:///android_asset/articles/$filename")
            },
        )
        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
