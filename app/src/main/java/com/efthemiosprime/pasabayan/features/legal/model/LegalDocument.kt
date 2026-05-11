package com.efthemiosprime.pasabayan.features.legal.model

/**
 * A pending legal document the user must agree to before continuing. iOS parity:
 * `PendingDocument` in `LegalModels.swift`.
 */
data class LegalDocument(
    val id: Int,
    val type: String,
    val title: String,
    val version: String,
) {
    /** Lower-cased, underscore-normalized type for matching against [DocumentKind]. */
    val normalizedType: String get() = type.lowercase().replace('-', '_')

    /**
     * Resolved local HTML filename in `assets/articles/`. Falls back to English when no
     * `-fr` file is present. Mirrors iOS `resolvedLocalFilename(for:)`.
     *
     * @param languageCode current locale code (e.g. `"en"`, `"fr"`)
     * @param frenchAssets the set of `*-fr.html` asset names that exist (or `null` if unknown,
     *   in which case we optimistically return the FR filename for FR locales)
     */
    fun localFilename(
        languageCode: String,
        frenchAssets: Set<String>? = null,
    ): String {
        val base = DocumentKind.baseFilename(normalizedType, fallbackType = type)
        val englishFile = "$base.html"
        val isFrench = languageCode.lowercase().let { it == "fr" || it.startsWith("fr-") || it.startsWith("fr_") }
        if (!isFrench) return englishFile
        val frenchFile = "$base-fr.html"
        return when {
            frenchAssets == null -> frenchFile
            frenchAssets.contains(frenchFile) -> frenchFile
            else -> englishFile
        }
    }
}

/** Canonical document types the iOS app recognizes plus an unknown fallback. */
internal object DocumentKind {
    fun baseFilename(normalizedType: String, fallbackType: String): String = when (normalizedType) {
        "terms_of_service", "terms", "terms_of_use" -> "terms-of-service"
        "privacy_policy", "privacy" -> "privacy-policy"
        "service_agreement", "service" -> "service-agreement"
        "liability_waiver", "liability" -> "liability-waiver"
        else -> fallbackType.replace('_', '-')
    }
}
