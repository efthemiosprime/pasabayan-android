package com.efthemiosprime.pasabayan.features.legal.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.features.legal.model.LegalDocument
import com.efthemiosprime.pasabayan.features.legal.model.LegalStatus
import com.efthemiosprime.pasabayan.features.legal.viewmodel.LegalAgreementUiState
import com.efthemiosprime.pasabayan.features.legal.viewmodel.LegalAgreementViewModel

/**
 * Non-dismissible sheet that walks the user through each pending legal document and records
 * agreement. Mirrors iOS `LegalAgreementSheet`: linear progress header, horizontal document
 * tabs (when multiple), bundled HTML WebView, and an agree footer.
 *
 * The sheet drives [LegalAgreementViewModel]; the host is responsible for presenting it inside
 * a modal bottom sheet / full-screen container that ignores swipe-to-dismiss.
 */
@Composable
fun LegalAgreementSheet(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LegalAgreementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (state.status == null) viewModel.loadStatus()
    }

    // Track agreed-this-session set + current tab in composition state (UI-only concern).
    var currentIndex by remember { mutableStateOf(0) }
    val agreedIds = remember { mutableStateOf(emptySet<Int>()) }

    LaunchedEffect(state.justAgreed) {
        if (state.justAgreed && state.status?.pendingDocuments.isNullOrEmpty()) {
            // Reset session set and notify host.
            agreedIds.value = emptySet()
            viewModel.consumeJustAgreed()
            onFinished()
        }
    }

    val pendingDocs = state.status?.pendingDocuments.orEmpty()
    val safeIndex = currentIndex.coerceIn(0, (pendingDocs.size - 1).coerceAtLeast(0))
    val currentDoc = pendingDocs.getOrNull(safeIndex)
    val languageCode = LocalConfiguration.current.locales[0].language

    LegalAgreementContent(
        state = state,
        currentDoc = currentDoc,
        currentIndex = safeIndex,
        agreedIds = agreedIds.value,
        filenameOf = { doc -> viewModel.localFilename(doc, languageCode) },
        onSelectIndex = { currentIndex = it },
        onAgreeCurrent = {
            val doc = currentDoc ?: return@LegalAgreementContent
            if (doc.id in agreedIds.value) {
                // Already agreed this session — advance to next un-agreed doc.
                val next = pendingDocs
                    .indexOfFirst { it.id !in agreedIds.value && it.id != doc.id }
                if (next >= 0) currentIndex = next
                return@LegalAgreementContent
            }
            agreedIds.value = agreedIds.value + doc.id
            viewModel.agree(listOf(doc.id))
            val next = pendingDocs.indexOfFirst { it.id != doc.id && it.id !in agreedIds.value }
            if (next >= 0) currentIndex = next
        },
        onRetryLoad = { viewModel.loadStatus() },
        onClearError = { viewModel.clearError() },
        modifier = modifier,
    )
}

@Composable
internal fun LegalAgreementContent(
    state: LegalAgreementUiState,
    currentDoc: LegalDocument?,
    currentIndex: Int,
    agreedIds: Set<Int>,
    filenameOf: (LegalDocument) -> String,
    onSelectIndex: (Int) -> Unit,
    onAgreeCurrent: () -> Unit,
    onRetryLoad: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val docs = state.status?.pendingDocuments.orEmpty()
    val total = docs.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header: title + progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.legal_title),
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (total > 0) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / total.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.legal_document_progress, currentIndex + 1, total),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val remaining = total - agreedIds.size
                    if (remaining > 0) {
                        Text(
                            text = stringResource(R.string.legal_remaining, remaining),
                            style = PasabayanTextStyles.Caption.regular,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (total > 1) {
                    DocumentTabs(
                        docs = docs,
                        currentIndex = currentIndex,
                        agreedIds = agreedIds,
                        onSelectIndex = onSelectIndex,
                    )
                }
            }
        }

        // Body
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading && state.status == null -> CenteredLoader(stringResource(R.string.legal_loading_status))
                state.errorMessage != null && state.status == null -> ErrorBlock(
                    message = state.errorMessage ?: stringResource(R.string.legal_error_load),
                    onRetry = onRetryLoad,
                )
                state.status != null && docs.isEmpty() -> AllAgreedBlock()
                currentDoc != null -> AssetArticleWebView(
                    filename = filenameOf(currentDoc),
                    modifier = Modifier.fillMaxSize(),
                )
                else -> CenteredLoader(stringResource(R.string.legal_loading_document))
            }
        }

        // Footer
        if (currentDoc != null) {
            FooterAgreeBar(
                isSubmitting = state.isSubmitting,
                errorMessage = state.errorMessage,
                onAgree = onAgreeCurrent,
                onClearError = onClearError,
            )
        }
    }
}

@Composable
private fun DocumentTabs(
    docs: List<LegalDocument>,
    currentIndex: Int,
    agreedIds: Set<Int>,
    onSelectIndex: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        docs.forEachIndexed { index, doc ->
            val selected = index == currentIndex
            val agreed = doc.id in agreedIds
            Row(
                modifier = Modifier
                    .clickable { onSelectIndex(index) }
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(50),
                    )
                    .padding(horizontal = PasabayanSpacing.md, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                if (agreed) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else PasabayanColors.Success,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = doc.title.ifBlank { doc.type.replace('_', ' ').replaceFirstChar { it.uppercase() } },
                    style = PasabayanTextStyles.Caption.regular.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CenteredLoader(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(PasabayanSpacing.md))
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = PasabayanColors.Error,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.height(PasabayanSpacing.md))
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(PasabayanSpacing.md))
        PButton(
            text = stringResource(R.string.legal_retry),
            onClick = onRetry,
            style = PButtonStyle.Secondary,
        )
    }
}

@Composable
private fun AllAgreedBlock() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(PasabayanColors.Success.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = PasabayanColors.Success,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.legal_all_agreed_title),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.legal_all_agreed_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FooterAgreeBar(
    isSubmitting: Boolean,
    errorMessage: String?,
    onAgree: () -> Unit,
    onClearError: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(PasabayanSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        errorMessage?.let { msg ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                modifier = Modifier.clickable(onClick = onClearError),
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = PasabayanColors.Error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = msg,
                    style = PasabayanTextStyles.Caption.regular,
                    color = PasabayanColors.Error,
                )
            }
        }
        PButton(
            text = stringResource(R.string.legal_agree_to_this),
            onClick = onAgree,
            style = PButtonStyle.Submit,
            isLoading = isSubmitting,
            icon = Icons.Filled.Check,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// -- Previews --

@Preview(name = "Legal sheet — populated", showBackground = true)
@Preview(
    name = "Legal sheet — populated dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LegalAgreementPopulatedPreview() {
    PasabayanTheme {
        val docs = listOf(
            LegalDocument(id = 1, type = "terms_of_service", title = "Terms of Service", version = "1.0"),
            LegalDocument(id = 2, type = "privacy_policy", title = "Privacy Policy", version = "2.1"),
        )
        LegalAgreementContent(
            state = LegalAgreementUiState(
                status = LegalStatus(allAgreed = false, pendingDocuments = docs, pendingCount = 2),
            ),
            currentDoc = docs.first(),
            currentIndex = 0,
            agreedIds = emptySet(),
            filenameOf = { "terms-of-service.html" },
            onSelectIndex = {},
            onAgreeCurrent = {},
            onRetryLoad = {},
            onClearError = {},
        )
    }
}

@Preview(name = "Legal sheet — all agreed", showBackground = true)
@Composable
private fun LegalAgreementAllAgreedPreview() {
    PasabayanTheme {
        LegalAgreementContent(
            state = LegalAgreementUiState(
                status = LegalStatus(allAgreed = true, pendingDocuments = emptyList(), pendingCount = 0),
            ),
            currentDoc = null,
            currentIndex = 0,
            agreedIds = emptySet(),
            filenameOf = { "" },
            onSelectIndex = {},
            onAgreeCurrent = {},
            onRetryLoad = {},
            onClearError = {},
        )
    }
}

@Preview(name = "Legal sheet — load error", showBackground = true)
@Composable
private fun LegalAgreementErrorPreview() {
    PasabayanTheme {
        LegalAgreementContent(
            state = LegalAgreementUiState(errorMessage = "Couldn't reach the server."),
            currentDoc = null,
            currentIndex = 0,
            agreedIds = emptySet(),
            filenameOf = { "" },
            onSelectIndex = {},
            onAgreeCurrent = {},
            onRetryLoad = {},
            onClearError = {},
        )
    }
}
