package com.efthemiosprime.pasabayan.features.legal.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LegalDocumentTest {

    @Test
    fun `terms aliases resolve to terms-of-service base filename`() {
        for (type in listOf("terms_of_service", "terms", "terms_of_use", "Terms-Of-Service")) {
            val doc = LegalDocument(id = 1, type = type, title = "x", version = "1.0")
            assertEquals(
                "type '$type' should resolve to terms-of-service",
                "terms-of-service.html",
                doc.localFilename(languageCode = "en"),
            )
        }
    }

    @Test
    fun `privacy aliases resolve correctly`() {
        for (type in listOf("privacy_policy", "privacy", "Privacy-Policy")) {
            val doc = LegalDocument(id = 1, type = type, title = "x", version = "1.0")
            assertEquals("privacy-policy.html", doc.localFilename("en"))
        }
    }

    @Test
    fun `unknown type falls back to dash-separated lower form`() {
        val doc = LegalDocument(id = 1, type = "custom_doc_kind", title = "x", version = "1.0")
        assertEquals("custom-doc-kind.html", doc.localFilename("en"))
    }

    @Test
    fun `french locale prefers fr file when asset exists`() {
        val doc = LegalDocument(id = 1, type = "terms_of_service", title = "x", version = "1.0")
        val frenchAssets = setOf("terms-of-service-fr.html", "privacy-policy-fr.html")
        assertEquals("terms-of-service-fr.html", doc.localFilename("fr", frenchAssets))
    }

    @Test
    fun `french locale falls back to english when fr asset missing`() {
        val doc = LegalDocument(id = 1, type = "liability_waiver", title = "x", version = "1.0")
        val frenchAssets = setOf("terms-of-service-fr.html")
        assertEquals("liability-waiver.html", doc.localFilename("fr", frenchAssets))
    }

    @Test
    fun `english locale ignores french asset set`() {
        val doc = LegalDocument(id = 1, type = "terms_of_service", title = "x", version = "1.0")
        assertEquals(
            "terms-of-service.html",
            doc.localFilename("en", frenchAssets = setOf("terms-of-service-fr.html")),
        )
    }

    @Test
    fun `null french asset set optimistically returns fr filename for fr locale`() {
        val doc = LegalDocument(id = 1, type = "privacy_policy", title = "x", version = "1.0")
        assertEquals("privacy-policy-fr.html", doc.localFilename("fr-CA", frenchAssets = null))
    }
}
