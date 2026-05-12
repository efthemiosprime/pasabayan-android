package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.profile.model.AccountManagementUiState
import com.efthemiosprime.pasabayan.features.profile.viewmodel.AccountManagementViewModel
import com.efthemiosprime.pasabayan.features.profile.viewmodel.toContentUri

object AccountManagementTestTags {
    const val Root = "account_management"
    const val Export = "account_management_export"
    const val Delete = "account_management_delete"
    const val DeleteConfirm = "account_management_delete_confirm"
}

@Composable
fun AccountManagementSheet(
    onClose: () -> Unit,
    onAccountDeletionRequested: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.profile_account_export_share_title)
    LaunchedEffect(Unit) {
        viewModel.shareEvents.collect { request ->
            val uri = request.toContentUri(context)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ContextCompat.startActivity(
                context,
                Intent.createChooser(send, shareTitle),
                null,
            )
        }
    }
    LaunchedEffect(Unit) {
        viewModel.signOutEvents.collect {
            onAccountDeletionRequested()
        }
    }
    AccountManagementContent(
        state = state,
        onExport = viewModel::exportData,
        onReasonChange = viewModel::onDeletionReasonChange,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
        onClose = onClose,
        modifier = modifier,
    )
    if (state.showDeleteConfirm) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog(
            title = stringResource(R.string.profile_account_delete_confirm_title),
            message = stringResource(R.string.profile_account_delete_confirm_message),
            confirmText = stringResource(R.string.profile_account_delete_confirm_yes),
            onConfirm = viewModel::confirmDelete,
            dismissText = stringResource(R.string.profile_account_delete_confirm_cancel),
            onDismiss = viewModel::cancelDelete,
            isDestructive = true,
            confirmModifier = Modifier.testTag(AccountManagementTestTags.DeleteConfirm),
        )
    }
}

@Composable
fun AccountManagementContent(
    state: AccountManagementUiState,
    onExport: () -> Unit,
    onReasonChange: (String) -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.md,
            )
            .testTag(AccountManagementTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.profile_account_title),
            style = PasabayanTextStyles.Heading.h3,
        )
        ExportSection(state = state, onExport = onExport)
        DeleteSection(
            state = state,
            onReasonChange = onReasonChange,
            onRequestDelete = onRequestDelete,
        )
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        state.infoMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = msg, style = PasabayanTextStyles.Body.small)
            }
        }
        PButton(
            text = stringResource(R.string.profile_account_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    // Confirm/cancel are wired through the parent sheet's AlertDialog.
    @Suppress("UnusedExpression")
    run {
        onConfirmDelete
        onCancelDelete
    }
}

@Composable
private fun ExportSection(
    state: AccountManagementUiState,
    onExport: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_account_section_export),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.profile_account_export_helper),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.isExporting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.profile_account_export_action),
                    onClick = onExport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AccountManagementTestTags.Export),
                )
            }
        }
    }
}

@Composable
private fun DeleteSection(
    state: AccountManagementUiState,
    onReasonChange: (String) -> Unit,
    onRequestDelete: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_account_section_delete),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.profile_account_delete_helper),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            POutlinedTextField(
                value = state.deletionReason,
                onValueChange = onReasonChange,
                label = { Text(stringResource(R.string.profile_account_delete_reason_label)) },
                placeholder = {
                    Text(stringResource(R.string.profile_account_delete_reason_placeholder))
                },
                singleLine = false,
                maxLines = 4,
                enabled = !state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isDeleting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.profile_account_delete_action),
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AccountManagementTestTags.Delete),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "AccountMgmt — light")
@Preview(showBackground = true, name = "AccountMgmt — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AccountManagementPreview() {
    PasabayanTheme {
        AccountManagementContent(
            state = AccountManagementUiState(deletionReason = "Moving to a new app"),
            onExport = {},
            onReasonChange = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onClose = {},
        )
    }
}
