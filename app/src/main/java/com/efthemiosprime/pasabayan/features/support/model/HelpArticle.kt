package com.efthemiosprime.pasabayan.features.support.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A single Help Center article — iOS parity with `Article` in `HelpCenterView.swift`.
 * Title + reading time are localized via string resources. [htmlBaseFilename] is the
 * English filename (without `.html`); FR fallback is resolved at render time.
 */
data class HelpArticle(
    val id: String,
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val readingTimeRes: Int,
    val htmlBaseFilename: String,
) {
    /** Returns the localized HTML filename, falling back to English when the FR file is absent. */
    fun resolveHtmlFilename(
        languageCode: String,
        frenchAssets: Set<String>? = null,
    ): String {
        val englishFile = "$htmlBaseFilename.html"
        val isFrench = languageCode.lowercase().let {
            it == "fr" || it.startsWith("fr-") || it.startsWith("fr_")
        }
        if (!isFrench) return englishFile
        val frenchFile = "$htmlBaseFilename-fr.html"
        return when {
            frenchAssets == null -> frenchFile
            frenchAssets.contains(frenchFile) -> frenchFile
            else -> englishFile
        }
    }
}
