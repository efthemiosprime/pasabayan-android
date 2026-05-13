package com.efthemiosprime.pasabayan.features.legal.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip

/**
 * Read-only legal viewer — iOS parity with `TermsAndPrivacyView`. Tabs across the four
 * canonical documents (Terms / Privacy / Service / Liability); body is the bundled HTML
 * rendered via [AssetArticleWebView] (no JavaScript, external links blocked). Distinct
 * from [LegalAgreementSheet] (the non-dismissible consent flow used during onboarding).
 *
 * Host this inside a [com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet]
 * or full-screen scaffold; the sheet itself only renders the title + tabs + WebView body.
 */
@Composable
fun LegalViewerSheet(
    modifier: Modifier = Modifier,
    initialDocument: LegalViewerDocument = LegalViewerDocument.TermsOfService,
) {
    var selected by rememberSaveable { mutableStateOf(initialDocument) }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.legal_viewer_title),
            style = PasabayanTextStyles.Heading.h4,
            modifier = Modifier.padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.sm,
            ),
        )
        DocumentTabs(
            selected = selected,
            onSelect = { selected = it },
        )
        AssetArticleWebView(
            filename = selected.htmlFilename,
            // weight(1f) is load-bearing: without it the WebView wraps to 0 height
            // inside the Column and the sheet renders blank under the tabs.
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = PasabayanSpacing.screenPadding),
        )
    }
}

/** The four legal documents bundled under `assets/articles/` — iOS `LegalDocument` parity. */
enum class LegalViewerDocument(val htmlFilename: String) {
    TermsOfService("terms-of-service.html"),
    PrivacyPolicy("privacy-policy.html"),
    ServiceAgreement("service-agreement.html"),
    LiabilityWaiver("liability-waiver.html"),
}

@Composable
private fun DocumentTabs(
    selected: LegalViewerDocument,
    onSelect: (LegalViewerDocument) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = PasabayanSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        LegalViewerDocument.values().forEach { document ->
            PFilterChip(
                label = stringResource(documentLabelRes(document)),
                selected = selected == document,
                onClick = { onSelect(document) },
            )
        }
    }
}

@StringRes
private fun documentLabelRes(document: LegalViewerDocument): Int = when (document) {
    LegalViewerDocument.TermsOfService -> R.string.legal_viewer_tab_terms
    LegalViewerDocument.PrivacyPolicy -> R.string.legal_viewer_tab_privacy
    LegalViewerDocument.ServiceAgreement -> R.string.legal_viewer_tab_service
    LegalViewerDocument.LiabilityWaiver -> R.string.legal_viewer_tab_liability
}

@Preview(showBackground = true, name = "LegalViewer — light", heightDp = 720)
@Preview(
    showBackground = true,
    name = "LegalViewer — dark",
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LegalViewerSheetPreview() {
    PasabayanTheme {
        // Preview renders the chrome (title + tabs); the WebView body is empty in previews.
        LegalViewerSheet()
    }
}
