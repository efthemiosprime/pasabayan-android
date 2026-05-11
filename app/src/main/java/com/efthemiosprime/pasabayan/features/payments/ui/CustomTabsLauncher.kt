package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Launches [url] in a Chrome Custom Tab. iOS parity: `SafariView` in `PayoutSetupView.swift`.
 *
 * Returns `true` if the intent was dispatched. The caller is responsible for treating an `Intent`
 * launch as "user has begun the external flow" (we cannot detect actual completion); the iOS
 * pattern is to refresh status when the sheet is dismissed via `handleOnboardingReturn()`.
 */
fun launchCustomTab(context: Context, url: String): Boolean {
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
    return try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .build()
            .launchUrl(context, uri)
        true
    } catch (e: Exception) {
        false
    }
}
