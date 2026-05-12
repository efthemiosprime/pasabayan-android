package com.efthemiosprime.pasabayan.features.support.ui

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import com.efthemiosprime.pasabayan.features.support.model.SupportTicket
import com.efthemiosprime.pasabayan.features.support.model.SupportTicketDraft
import com.efthemiosprime.pasabayan.features.support.viewmodel.SupportTicketUiState
import com.efthemiosprime.pasabayan.features.support.viewmodel.SupportTicketViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun SupportTicketFormScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupportTicketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let(viewModel::addAttachment)
    }

    SupportTicketFormContent(
        state = state,
        onClose = onClose,
        onCategorySelect = viewModel::selectCategory,
        onSubjectChange = viewModel::setSubject,
        onEmailChange = viewModel::setEmail,
        onPrioritySelect = viewModel::selectPriority,
        onDescriptionChange = viewModel::setDescription,
        onPickAttachment = {
            attachmentLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
            )
        },
        onRemoveAttachment = viewModel::removeAttachment,
        onSubmit = viewModel::submit,
        onClearError = viewModel::clearError,
        onConsumeSuccess = {
            viewModel.consumeSubmittedTicket()
            onClose()
        },
        modifier = modifier,
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
internal fun SupportTicketFormContent(
    state: SupportTicketUiState,
    onClose: () -> Unit,
    onCategorySelect: (SupportCategory) -> Unit,
    onSubjectChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPrioritySelect: (SupportPriority) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPickAttachment: () -> Unit,
    onRemoveAttachment: (Uri) -> Unit,
    onSubmit: () -> Unit,
    onClearError: () -> Unit,
    onConsumeSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.support_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.support_intro),
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CategoryGrid(
                selected = state.draft.category,
                onSelect = onCategorySelect,
            )

            POutlinedTextField(
                value = state.draft.subject,
                onValueChange = onSubjectChange,
                label = { Text(stringResource(R.string.support_subject_label)) },
                placeholder = { Text(stringResource(R.string.support_subject_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )

            POutlinedTextField(
                value = state.draft.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.support_email_label)) },
                placeholder = { Text(stringResource(R.string.support_email_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
            )

            PrioritySelector(
                selected = state.draft.priority,
                onSelect = onPrioritySelect,
            )

            POutlinedTextField(
                value = state.draft.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.support_description_label)) },
                placeholder = { Text(stringResource(R.string.support_description_placeholder)) },
                singleLine = false,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("${state.draft.description.length} / ${SupportTicketDraft.MAX_DESCRIPTION}")
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )

            AttachmentsSection(
                attachments = state.draft.attachments,
                canAddMore = state.draft.attachments.size < SupportTicketDraft.MAX_ATTACHMENTS,
                onAdd = onPickAttachment,
                onRemove = onRemoveAttachment,
            )

            state.errorMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PasabayanColors.Error.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(PasabayanSpacing.md)
                        .clickable(onClick = onClearError),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
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
                text = if (state.isSubmitting) {
                    stringResource(R.string.support_submitting)
                } else {
                    stringResource(R.string.support_submit)
                },
                onClick = onSubmit,
                style = PButtonStyle.Submit,
                enabled = state.draft.isSubmittable && !state.isSubmitting,
                isLoading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(PasabayanSpacing.md))
        }

        val ticket = state.submittedTicket
        if (ticket != null) {
            SubmissionSuccessDialog(ticket = ticket, onDismiss = onConsumeSuccess)
        }
    }
}

@Composable
private fun CategoryGrid(
    selected: SupportCategory?,
    onSelect: (SupportCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = stringResource(R.string.support_category_label),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // Two-column flow without flow-layout APIs: chunk into pairs.
        SupportCategory.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                row.forEach { category ->
                    CategoryCard(
                        category = category,
                        selected = category == selected,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: SupportCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    PCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg, RoundedCornerShape(8.dp))
                .padding(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = stringResource(category.displayNameRes),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PrioritySelector(
    selected: SupportPriority,
    onSelect: (SupportPriority) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = stringResource(R.string.support_priority_label),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            SupportPriority.entries.forEach { priority ->
                PFilterChip(
                    label = stringResource(priority.displayNameRes),
                    selected = priority == selected,
                    onClick = { onSelect(priority) },
                )
            }
        }
    }
}

@Composable
private fun AttachmentsSection(
    attachments: List<Uri>,
    canAddMore: Boolean,
    onAdd: () -> Unit,
    onRemove: (Uri) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = stringResource(R.string.support_attachments_label),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.support_attachments_hint, SupportTicketDraft.MAX_ATTACHMENTS),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (attachments.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                attachments.forEach { uri ->
                    AttachmentChip(uri = uri, onRemove = { onRemove(uri) })
                }
            }
        }
        if (canAddMore) {
            PButton(
                text = stringResource(R.string.support_attachments_add),
                onClick = onAdd,
                style = PButtonStyle.Secondary,
                icon = Icons.Filled.Attachment,
            )
        }
    }
}

@Composable
private fun AttachmentChip(uri: Uri, onRemove: () -> Unit) {
    val label = uri.lastPathSegment ?: "attachment"
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Filled.Attachment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = PasabayanSpacing.xs),
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = stringResource(R.string.support_attachments_remove),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SubmissionSuccessDialog(ticket: SupportTicket, onDismiss: () -> Unit) {
    com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog(
        title = stringResource(R.string.support_success_title),
        message = stringResource(R.string.support_success_description, ticket.id, ticket.email),
        confirmText = stringResource(R.string.support_success_dismiss),
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        icon = Icons.Filled.CheckCircle,
        iconTint = PasabayanColors.Success,
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Support form — empty", showBackground = true)
@Preview(
    name = "Support form — empty dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SupportTicketFormEmptyPreview() {
    PasabayanTheme {
        SupportTicketFormContent(
            state = SupportTicketUiState(),
            onClose = {},
            onCategorySelect = {},
            onSubjectChange = {},
            onEmailChange = {},
            onPrioritySelect = {},
            onDescriptionChange = {},
            onPickAttachment = {},
            onRemoveAttachment = {},
            onSubmit = {},
            onClearError = {},
            onConsumeSuccess = {},
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Preview(name = "Support form — filled", showBackground = true)
@Composable
private fun SupportTicketFormFilledPreview() {
    PasabayanTheme {
        SupportTicketFormContent(
            state = SupportTicketUiState(
                draft = SupportTicketDraft(
                    category = SupportCategory.DELIVERY_ISSUES,
                    subject = "Carrier never arrived",
                    email = "shipper@example.com",
                    priority = SupportPriority.HIGH,
                    description = "I scheduled pickup at 2 PM but the carrier didn't show up by 4 PM and stopped replying in chat.",
                ),
            ),
            onClose = {},
            onCategorySelect = {},
            onSubjectChange = {},
            onEmailChange = {},
            onPrioritySelect = {},
            onDescriptionChange = {},
            onPickAttachment = {},
            onRemoveAttachment = {},
            onSubmit = {},
            onClearError = {},
            onConsumeSuccess = {},
        )
    }
}
