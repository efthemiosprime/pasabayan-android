package com.efthemiosprime.pasabayan.features.verification.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
import com.efthemiosprime.pasabayan.features.verification.model.IdDocumentType
import com.efthemiosprime.pasabayan.features.verification.model.PremiumVerificationUiState
import com.efthemiosprime.pasabayan.features.verification.viewmodel.PremiumImageSlot
import com.efthemiosprime.pasabayan.features.verification.viewmodel.PremiumVerificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PremiumVerificationTestTags {
    const val Root = "premium_verification"
    const val Submit = "premium_verification_submit"
    fun idTypeFor(type: IdDocumentType) = "premium_verification_id_${type.raw}"
    fun uploadFor(slot: PremiumImageSlot) = "premium_verification_upload_${slot.name.lowercase()}"
}

@Composable
fun PremiumVerificationSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingSlot by remember { mutableStateOf<PremiumImageSlot?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val slot = pendingSlot ?: return@rememberLauncherForActivityResult
        pendingSlot = null
        if (uri != null) {
            coroutineScope.launch {
                val bytes = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                if (bytes != null && bytes.isNotEmpty()) {
                    viewModel.onImageSelected(slot, bytes)
                }
            }
        }
    }
    LaunchedEffect(Unit) { viewModel.bootstrap() }
    PremiumVerificationContent(
        state = state,
        onIdTypeChange = viewModel::onIdTypeChange,
        onIdNumberChange = viewModel::onIdNumberChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onPickImage = { slot ->
            pendingSlot = slot
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onSubmit = viewModel::submit,
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
fun PremiumVerificationContent(
    state: PremiumVerificationUiState,
    onIdTypeChange: (IdDocumentType) -> Unit,
    onIdNumberChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onPickImage: (PremiumImageSlot) -> Unit,
    onSubmit: () -> Unit,
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
            .testTag(PremiumVerificationTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.verification_premium_title),
                style = PasabayanTextStyles.Heading.h3,
            )
            Text(
                text = stringResource(R.string.verification_premium_subtitle),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.pendingStatus != null) {
            PendingApplicationCard(state)
        } else {
            IdTypeSection(state, onIdTypeChange)
            DocumentDetailsSection(state, onIdNumberChange, onBirthDateChange)
            ImagesSection(state, onPickImage)
            if (state.isSubmitting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.verification_premium_submit),
                    onClick = onSubmit,
                    enabled = state.isReadyToSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PremiumVerificationTestTags.Submit),
                )
            }
        }
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        state.successMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = msg, style = PasabayanTextStyles.Body.small)
            }
        }
        PButton(
            text = stringResource(R.string.verification_premium_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun IdTypeSection(
    state: PremiumVerificationUiState,
    onIdTypeChange: (IdDocumentType) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.verification_premium_id_type_label),
                style = PasabayanTextStyles.Heading.h5,
            )
            IdDocumentType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIdTypeChange(type) }
                        .padding(vertical = PasabayanSpacing.xs)
                        .testTag(PremiumVerificationTestTags.idTypeFor(type)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    RadioButton(
                        selected = type == state.idType,
                        onClick = { onIdTypeChange(type) },
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(type.labelRes),
                            style = PasabayanTextStyles.Body.medium,
                        )
                        Text(
                            text = stringResource(type.descriptionRes),
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDetailsSection(
    state: PremiumVerificationUiState,
    onIdNumberChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            POutlinedTextField(
                value = state.idNumber,
                onValueChange = onIdNumberChange,
                label = { Text(stringResource(R.string.verification_premium_id_number_label)) },
                placeholder = {
                    Text(stringResource(R.string.verification_premium_id_number_placeholder))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = state.birthDate,
                onValueChange = onBirthDateChange,
                label = { Text(stringResource(R.string.verification_premium_birth_date_label)) },
                placeholder = {
                    Text(stringResource(R.string.verification_premium_birth_date_placeholder))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ImagesSection(
    state: PremiumVerificationUiState,
    onPickImage: (PremiumImageSlot) -> Unit,
) {
    UploadSlotCard(
        labelRes = R.string.verification_premium_front_label,
        hasImage = state.idDocumentFront != null,
        onClick = { onPickImage(PremiumImageSlot.FRONT) },
        testTag = PremiumVerificationTestTags.uploadFor(PremiumImageSlot.FRONT),
    )
    if (state.idType.requiresBackImage) {
        UploadSlotCard(
            labelRes = R.string.verification_premium_back_label,
            hasImage = state.idDocumentBack != null,
            onClick = { onPickImage(PremiumImageSlot.BACK) },
            testTag = PremiumVerificationTestTags.uploadFor(PremiumImageSlot.BACK),
        )
    }
    UploadSlotCard(
        labelRes = R.string.verification_premium_selfie_label,
        hasImage = state.selfieWithId != null,
        onClick = { onPickImage(PremiumImageSlot.SELFIE) },
        testTag = PremiumVerificationTestTags.uploadFor(PremiumImageSlot.SELFIE),
    )
}

@Composable
private fun UploadSlotCard(
    labelRes: Int,
    hasImage: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = stringResource(labelRes),
                    style = PasabayanTextStyles.Body.medium,
                )
                if (hasImage) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = " " + stringResource(R.string.verification_premium_added),
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            PButton(
                text = stringResource(
                    if (hasImage) {
                        R.string.verification_premium_replace
                    } else {
                        R.string.verification_premium_upload
                    },
                ),
                onClick = onClick,
                modifier = Modifier.testTag(testTag),
            )
        }
    }
}

@Composable
private fun PendingApplicationCard(state: PremiumVerificationUiState) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.verification_premium_pending_card_title),
                style = PasabayanTextStyles.Heading.h5,
            )
            state.pendingStatus?.let { status ->
                Text(
                    text = stringResource(status.labelRes),
                    style = PasabayanTextStyles.Body.medium,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Premium — light")
@Preview(showBackground = true, name = "Premium — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PremiumVerificationPreview() {
    PasabayanTheme {
        PremiumVerificationContent(
            state = PremiumVerificationUiState(
                idType = IdDocumentType.PASSPORT,
                idNumber = "AB1234567",
                birthDate = "1990-05-21",
                idDocumentFront = byteArrayOf(1, 2, 3),
                selfieWithId = byteArrayOf(4, 5, 6),
            ),
            onIdTypeChange = {},
            onIdNumberChange = {},
            onBirthDateChange = {},
            onPickImage = {},
            onSubmit = {},
            onClose = {},
        )
    }
}
