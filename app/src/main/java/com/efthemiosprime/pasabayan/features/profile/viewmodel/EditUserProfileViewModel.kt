package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.UserProfileJson
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ContactMethod
import com.efthemiosprime.pasabayan.features.profile.model.EditUserProfileUiState
import com.efthemiosprime.pasabayan.features.profile.model.SupportedTimezones
import com.efthemiosprime.pasabayan.features.profile.services.ImageCompressor
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives `EditUserProfileSheet`. Smart pre-filling rules mirror iOS `EditUserProfileSheet.init`
 * (`09-profile-carrier-consent.md` § Smart defaults). On `initialize` it loads the current
 * profile through [ProfileRepository] (cached after the profile-tab fetch) so we can preserve
 * legacy `additional_info` keys (e.g. `oauth_provider`) when saving.
 */
@HiltViewModel
class EditUserProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val imageCompressor: ImageCompressor,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUserProfileUiState())
    val state: StateFlow<EditUserProfileUiState> = _state.asStateFlow()

    private var loadedProfile: UserProfileJson? = null
    private var systemTimezoneProvider: () -> String = { TimeZone.getDefault().id }

    @VisibleForTesting
    internal fun overrideSystemTimezoneProvider(provider: () -> String) {
        systemTimezoneProvider = provider
    }

    fun initialize(authUser: AuthUser) {
        if (_state.value.isInitialized) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val fetched = repository.fetchProfile(forceRefresh = false).getOrNull()
            val profile = fetched?.profile
            loadedProfile = profile
            val prefillName = profile?.fullName?.takeIf { it.isNotBlank() } ?: authUser.name
            val prefillAddress = profile?.deliveryAddress.orEmpty()
            val prefillContact = resolveContactMethod(authUser, profile)
            val prefillTimezone = resolveTimezone(profile)
            _state.update {
                it.copy(
                    fullName = prefillName,
                    deliveryAddress = prefillAddress,
                    contactMethod = prefillContact,
                    timezone = prefillTimezone,
                    profilePictureUrl = profile?.profilePicture ?: authUser.avatar,
                    isInitialized = true,
                    isLoading = false,
                )
            }
        }
    }

    fun onFullNameChange(value: String) {
        _state.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onDeliveryAddressChange(value: String) {
        _state.update { it.copy(deliveryAddress = value, errorMessage = null) }
    }

    fun onContactMethodChange(value: ContactMethod) {
        _state.update { it.copy(contactMethod = value, errorMessage = null) }
    }

    fun onTimezoneChange(value: String) {
        _state.update { it.copy(timezone = value, errorMessage = null) }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Validates and `PUT /profile`. Preserves any existing `additional_info` keys we don't manage
     * here (e.g. `oauth_provider`, `language`) so we never drop legacy values.
     */
    fun save(onSaved: () -> Unit = {}) {
        val snapshot = _state.value
        if (!snapshot.isFullNameValid) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.profile_edit_error_name_required,
                    ),
                )
            }
            return
        }
        if (snapshot.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            val preservedExtras = loadedProfile?.additionalInfo.orEmpty()
            val mergedAdditional = preservedExtras.toMutableMap().apply {
                put(KEY_TIMEZONE, snapshot.timezone)
            }
            val request = UpdateProfileRequestJson(
                fullName = snapshot.fullName.trim(),
                deliveryAddress = snapshot.deliveryAddress.trim().takeIf { it.isNotEmpty() },
                preferredContactMethod = snapshot.contactMethod.raw,
                additionalInfo = mergedAdditional,
            )
            val result = repository.updateProfile(request)
            result.fold(
                onSuccess = { updated ->
                    loadedProfile = updated.profile
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_edit_success,
                            ),
                        )
                    }
                    onSaved()
                },
                onFailure = { throwable -> applyError(throwable) },
            )
        }
    }

    /**
     * Compresses [imageBytes] (Bitmap → JPEG ≤ 512 px) and `POST /api/profile` multipart with the
     * current form's basic info to preserve fields per iOS `uploadImageWithCurrentProfileData`.
     */
    fun onAvatarSelected(imageBytes: ByteArray) {
        val snapshot = _state.value
        if (snapshot.isAvatarUpdating) return
        _state.update { it.copy(isAvatarUpdating = true, errorMessage = null) }
        viewModelScope.launch {
            val compressed = try {
                imageCompressor.compressToJpeg(imageBytes)
            } catch (e: IllegalArgumentException) {
                _state.update {
                    it.copy(
                        isAvatarUpdating = false,
                        errorMessage = appContext.getString(
                            com.efthemiosprime.pasabayan.R.string.profile_avatar_error_decode,
                        ),
                    )
                }
                return@launch
            }
            val result = repository.uploadProfileAvatar(
                imageBytes = compressed,
                mimeType = "image/jpeg",
                fileName = "avatar.jpg",
                fullName = snapshot.fullName.takeIf { it.isNotBlank() },
                deliveryAddress = snapshot.deliveryAddress.takeIf { it.isNotBlank() },
                preferredContactMethod = snapshot.contactMethod.raw,
            )
            result.fold(
                onSuccess = { updated ->
                    loadedProfile = updated.profile
                    _state.update {
                        it.copy(
                            isAvatarUpdating = false,
                            profilePictureUrl = updated.profile?.profilePicture,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_avatar_success_uploaded,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isAvatarUpdating = false) }
                    applyError(throwable)
                },
            )
        }
    }

    fun requestDeleteAvatar() {
        if (!_state.value.hasCustomAvatar) return
        _state.update { it.copy(showDeleteAvatarConfirm = true) }
    }

    fun cancelDeleteAvatar() {
        _state.update { it.copy(showDeleteAvatarConfirm = false) }
    }

    fun confirmDeleteAvatar() {
        val snapshot = _state.value
        if (snapshot.isAvatarUpdating) return
        _state.update {
            it.copy(
                showDeleteAvatarConfirm = false,
                isAvatarUpdating = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            val result = repository.deleteProfilePicture()
            result.fold(
                onSuccess = {
                    loadedProfile = loadedProfile?.copy(profilePicture = null)
                    _state.update {
                        it.copy(
                            isAvatarUpdating = false,
                            profilePictureUrl = null,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_avatar_success_deleted,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isAvatarUpdating = false) }
                    applyError(throwable)
                },
            )
        }
    }

    private fun applyError(throwable: Throwable) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = err.localizedMessage(appContext),
            )
        }
    }

    private fun resolveContactMethod(
        authUser: AuthUser,
        currentProfile: UserProfileJson?,
    ): ContactMethod {
        val saved = currentProfile?.preferredContactMethod
        if (!saved.isNullOrBlank()) {
            return ContactMethod.fromRaw(saved)
        }
        return when {
            authUser.email.isNotBlank() -> ContactMethod.EMAIL
            authUser.phoneVerified -> ContactMethod.PHONE
            else -> ContactMethod.APP_NOTIFICATION
        }
    }

    private fun resolveTimezone(currentProfile: UserProfileJson?): String {
        val stored = currentProfile?.additionalInfo?.get(KEY_TIMEZONE)
        if (!stored.isNullOrBlank()) return stored
        val system = systemTimezoneProvider()
        return if (system in SupportedTimezones) system else SupportedTimezones.first()
    }

    private companion object {
        const val KEY_TIMEZONE = "timezone"
    }
}
