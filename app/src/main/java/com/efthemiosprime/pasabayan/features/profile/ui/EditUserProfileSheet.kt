package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ContactMethod
import com.efthemiosprime.pasabayan.features.profile.model.EditUserProfileUiState
import com.efthemiosprime.pasabayan.features.profile.model.SupportedTimezones
import com.efthemiosprime.pasabayan.features.profile.viewmodel.EditUserProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object EditUserProfileTestTags {
    const val FullName = "edit_profile_full_name"
    const val Address = "edit_profile_address"
    const val ContactMethod = "edit_profile_contact_method"
    const val Timezone = "edit_profile_timezone"
    const val Save = "edit_profile_save"
    const val AvatarSection = "edit_profile_avatar"
    const val AvatarUpload = "edit_profile_avatar_upload"
    const val AvatarDelete = "edit_profile_avatar_delete"
    const val AvatarConfirmDelete = "edit_profile_avatar_confirm_delete"
}

/**
 * Hilt-wired entry point. The host (`MainTabScreen`) renders this inside a `PModalBottomSheet`.
 *
 * @param onClose dismiss callback (called both on cancel and after a successful save).
 */
@Composable
fun EditUserProfileSheet(
    authUser: AuthUser,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileUpdated: () -> Unit = {},
    viewModel: EditUserProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val bytes = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                if (bytes != null && bytes.isNotEmpty()) {
                    viewModel.onAvatarSelected(bytes)
                }
            }
        }
    }
    LaunchedEffect(authUser.id) {
        viewModel.initialize(authUser)
    }
    LaunchedEffect(state.successMessage) {
        // After a save succeeds we dismiss; avatar success messages keep the sheet open
        // so the user can continue editing other fields without losing context.
        if (state.successMessage != null && !state.isAvatarUpdating) {
            onProfileUpdated()
            if (state.successMessage == context.getString(R.string.profile_edit_success)) {
                viewModel.consumeMessages()
                onClose()
            }
        }
    }
    EditUserProfileContent(
        state = state,
        onFullNameChange = viewModel::onFullNameChange,
        onDeliveryAddressChange = viewModel::onDeliveryAddressChange,
        onContactMethodChange = viewModel::onContactMethodChange,
        onTimezoneChange = viewModel::onTimezoneChange,
        onPickAvatar = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onRequestDeleteAvatar = viewModel::requestDeleteAvatar,
        onCancel = onClose,
        onSave = { viewModel.save() },
        modifier = modifier,
    )
    if (state.showDeleteAvatarConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteAvatar,
            title = { Text(stringResource(R.string.profile_avatar_confirm_title)) },
            text = { Text(stringResource(R.string.profile_avatar_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDeleteAvatar,
                    modifier = Modifier.testTag(EditUserProfileTestTags.AvatarConfirmDelete),
                ) {
                    Text(
                        text = stringResource(R.string.profile_avatar_confirm_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDeleteAvatar) {
                    Text(stringResource(R.string.profile_edit_cancel))
                }
            },
        )
    }
}

@Composable
fun EditUserProfileContent(
    state: EditUserProfileUiState,
    onFullNameChange: (String) -> Unit,
    onDeliveryAddressChange: (String) -> Unit,
    onContactMethodChange: (ContactMethod) -> Unit,
    onTimezoneChange: (String) -> Unit,
    onPickAvatar: () -> Unit,
    onRequestDeleteAvatar: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
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
            ),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.profile_edit_title),
            style = PasabayanTextStyles.Heading.h3,
        )
        ProfilePictureSection(
            displayName = state.fullName,
            profilePictureUrl = state.profilePictureUrl,
            hasCustomAvatar = state.hasCustomAvatar,
            isUpdating = state.isAvatarUpdating,
            onPickAvatar = onPickAvatar,
            onRequestDelete = onRequestDeleteAvatar,
        )
        BasicInfoSection(
            state = state,
            onFullNameChange = onFullNameChange,
            onDeliveryAddressChange = onDeliveryAddressChange,
        )
        ContactMethodSection(
            selected = state.contactMethod,
            onContactMethodChange = onContactMethodChange,
        )
        TimezoneSection(
            timezone = state.timezone,
            onTimezoneChange = onTimezoneChange,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PButton(
                text = stringResource(R.string.profile_edit_cancel),
                onClick = onCancel,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
            )
            PButton(
                text = stringResource(R.string.profile_edit_save),
                onClick = onSave,
                enabled = state.isFormValid,
                modifier = Modifier
                    .weight(1f)
                    .testTag(EditUserProfileTestTags.Save),
            )
        }
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PCircularProgress()
            }
        }
    }
}

@Composable
private fun ProfilePictureSection(
    displayName: String,
    profilePictureUrl: String?,
    hasCustomAvatar: Boolean,
    isUpdating: Boolean,
    onPickAvatar: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(EditUserProfileTestTags.AvatarSection),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_avatar_section_title),
                style = PasabayanTextStyles.Heading.h5,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isUpdating) {
                        PCircularProgress()
                    } else {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "?",
                            style = PasabayanTextStyles.Heading.h2,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                ) {
                    val statusRes = when {
                        hasCustomAvatar -> R.string.profile_avatar_status_custom
                        !profilePictureUrl.isNullOrBlank() -> R.string.profile_avatar_status_social
                        else -> R.string.profile_avatar_status_none
                    }
                    Text(
                        text = stringResource(statusRes),
                        style = PasabayanTextStyles.Body.medium,
                    )
                    Text(
                        text = stringResource(R.string.profile_avatar_hint),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                PButton(
                    text = stringResource(
                        if (hasCustomAvatar) {
                            R.string.profile_avatar_action_change
                        } else {
                            R.string.profile_avatar_action_upload
                        },
                    ),
                    onClick = onPickAvatar,
                    enabled = !isUpdating,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(EditUserProfileTestTags.AvatarUpload),
                )
                if (hasCustomAvatar) {
                    PButton(
                        text = stringResource(R.string.profile_avatar_action_delete),
                        onClick = onRequestDelete,
                        enabled = !isUpdating,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(EditUserProfileTestTags.AvatarDelete),
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicInfoSection(
    state: EditUserProfileUiState,
    onFullNameChange: (String) -> Unit,
    onDeliveryAddressChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_edit_section_basic),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(R.string.profile_edit_full_name_label)) },
                placeholder = { Text(stringResource(R.string.profile_edit_full_name_placeholder)) },
                isError = !state.isFullNameValid && state.fullName.isNotEmpty(),
                supportingText = {
                    Text(stringResource(R.string.profile_edit_full_name_helper))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditUserProfileTestTags.FullName),
            )
            POutlinedTextField(
                value = state.deliveryAddress,
                onValueChange = onDeliveryAddressChange,
                label = { Text(stringResource(R.string.profile_edit_delivery_address_label)) },
                placeholder = { Text(stringResource(R.string.profile_edit_delivery_address_placeholder)) },
                singleLine = false,
                maxLines = 4,
                supportingText = {
                    Text(stringResource(R.string.profile_edit_delivery_address_helper))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditUserProfileTestTags.Address),
            )
        }
    }
}

@Composable
private fun ContactMethodSection(
    selected: ContactMethod,
    onContactMethodChange: (ContactMethod) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth().testTag(EditUserProfileTestTags.ContactMethod)) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_edit_section_contact),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.profile_edit_contact_method_helper),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ContactMethod.entries.forEach { method ->
                ContactMethodRow(
                    method = method,
                    isSelected = method == selected,
                    onClick = { onContactMethodChange(method) },
                )
            }
        }
    }
}

@Composable
private fun ContactMethodRow(
    method: ContactMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val labelRes = when (method) {
        ContactMethod.PHONE -> R.string.profile_edit_contact_method_phone
        ContactMethod.EMAIL -> R.string.profile_edit_contact_method_email
        ContactMethod.APP_NOTIFICATION -> R.string.profile_edit_contact_method_app
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(
            text = stringResource(labelRes),
            style = PasabayanTextStyles.Body.medium,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun TimezoneSection(
    timezone: String,
    onTimezoneChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_edit_section_settings),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(R.string.profile_edit_timezone_label),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(PasabayanRadius.sm),
                        )
                        .padding(
                            horizontal = PasabayanSpacing.md,
                            vertical = PasabayanSpacing.sm,
                        )
                        .testTag(EditUserProfileTestTags.Timezone),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = timezone,
                        style = PasabayanTextStyles.Body.medium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.wrapContentSize(),
                ) {
                    SupportedTimezones.forEach { tz ->
                        DropdownMenuItem(
                            text = { Text(tz) },
                            onClick = {
                                expanded = false
                                onTimezoneChange(tz)
                            },
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.profile_edit_timezone_helper),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "EditProfile — light")
@Preview(showBackground = true, name = "EditProfile — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditUserProfilePreview() {
    PasabayanTheme {
        EditUserProfileContent(
            state = EditUserProfileUiState(
                fullName = "Bong Suyat",
                deliveryAddress = "123 Main St, Montreal",
                contactMethod = ContactMethod.EMAIL,
                timezone = "America/Toronto",
                profilePictureUrl = "https://cdn.example/avatar.jpg",
                isInitialized = true,
            ),
            onFullNameChange = {},
            onDeliveryAddressChange = {},
            onContactMethodChange = {},
            onTimezoneChange = {},
            onPickAvatar = {},
            onRequestDeleteAvatar = {},
            onCancel = {},
            onSave = {},
        )
    }
}
