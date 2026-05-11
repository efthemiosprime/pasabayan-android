package com.efthemiosprime.pasabayan.features.verification.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.profile.services.ImageCompressor
import com.efthemiosprime.pasabayan.features.verification.model.IdDocumentType
import com.efthemiosprime.pasabayan.features.verification.model.PremiumApplicationStatus
import com.efthemiosprime.pasabayan.features.verification.model.PremiumVerificationUiState
import com.efthemiosprime.pasabayan.features.verification.services.VerificationRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PremiumImageSlot { FRONT, BACK, SELFIE }

/**
 * Drives `PremiumVerificationSheet` (`10-verification.md` § premium upload). Picks images via the
 * shared [ImageCompressor] (≤ 512 px JPEG, same as avatars — keeps payload under the backend 5 MB
 * limit) then submits all three together via `POST /verification/request-premium` multipart.
 */
@HiltViewModel
class PremiumVerificationViewModel @Inject constructor(
    private val repository: VerificationRepository,
    private val imageCompressor: ImageCompressor,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumVerificationUiState())
    val state: StateFlow<PremiumVerificationUiState> = _state.asStateFlow()

    fun bootstrap() {
        _state.update { it.copy(isLoadingStatus = true, errorMessage = null) }
        viewModelScope.launch {
            repository.fetchPremiumStatus().fold(
                onSuccess = { data ->
                    val firstRequestStatus = data.requests.firstOrNull()?.status
                    val pending = firstRequestStatus?.let { PremiumApplicationStatus.fromRaw(it) }
                    _state.update {
                        it.copy(
                            isLoadingStatus = false,
                            pendingStatus = pending,
                            isSubmitted = data.verificationLevel == "premium" || pending != null,
                        )
                    }
                },
                onFailure = { applyError(it, finishLoadingStatus = true) },
            )
        }
    }

    fun onIdTypeChange(idType: IdDocumentType) {
        _state.update {
            it.copy(
                idType = idType,
                // Clear the back image when switching to a single-side ID (e.g. passport).
                idDocumentBack = if (idType.requiresBackImage) it.idDocumentBack else null,
                errorMessage = null,
            )
        }
    }

    fun onIdNumberChange(value: String) {
        _state.update { it.copy(idNumber = value, errorMessage = null) }
    }

    fun onBirthDateChange(value: String) {
        _state.update { it.copy(birthDate = value, errorMessage = null) }
    }

    fun onImageSelected(slot: PremiumImageSlot, imageBytes: ByteArray) {
        val compressed = try {
            imageCompressor.compressToJpeg(imageBytes)
        } catch (e: IllegalArgumentException) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.verification_premium_image_decode_error,
                    ),
                )
            }
            return
        }
        _state.update {
            when (slot) {
                PremiumImageSlot.FRONT -> it.copy(idDocumentFront = compressed, errorMessage = null)
                PremiumImageSlot.BACK -> it.copy(idDocumentBack = compressed, errorMessage = null)
                PremiumImageSlot.SELFIE -> it.copy(selfieWithId = compressed, errorMessage = null)
            }
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun submit() {
        val snapshot = _state.value
        if (snapshot.isSubmitting) return
        if (!snapshot.isReadyToSubmit) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.verification_premium_missing_images,
                    ),
                )
            }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            repository.submitPremiumVerification(
                idType = snapshot.idType.raw,
                idDocumentFront = snapshot.idDocumentFront!!,
                idDocumentBack = snapshot.idDocumentBack,
                selfieWithId = snapshot.selfieWithId!!,
                idNumber = snapshot.idNumber.trim().takeIf { it.isNotEmpty() },
                birthDate = snapshot.birthDate.trim().takeIf { it.isNotEmpty() },
            ).fold(
                onSuccess = { data ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            pendingStatus = PremiumApplicationStatus.fromRaw(data?.status),
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.verification_premium_submitted,
                            ),
                        )
                    }
                },
                onFailure = { applyError(it, finishLoadingStatus = false) },
            )
        }
    }

    private fun applyError(throwable: Throwable, finishLoadingStatus: Boolean) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _state.update {
            it.copy(
                isSubmitting = false,
                isLoadingStatus = if (finishLoadingStatus) false else it.isLoadingStatus,
                errorMessage = err.localizedMessage(appContext),
            )
        }
    }
}
