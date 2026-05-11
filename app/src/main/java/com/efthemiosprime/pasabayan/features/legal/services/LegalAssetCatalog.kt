package com.efthemiosprime.pasabayan.features.legal.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Snapshot of `*-fr.html` files in `assets/articles/`. Used by
 * [com.efthemiosprime.pasabayan.features.legal.model.LegalDocument.localFilename] so we can
 * fall back to English when a French translation is missing.
 */
interface LegalAssetCatalog {
    val frenchArticleFiles: Set<String>
}

@Singleton
class AndroidLegalAssetCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
) : LegalAssetCatalog {

    override val frenchArticleFiles: Set<String> by lazy {
        runCatching {
            context.assets.list("articles")
                ?.filter { it.endsWith("-fr.html") }
                ?.toSet()
                ?: emptySet()
        }.getOrDefault(emptySet())
    }
}
