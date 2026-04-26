package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ProfileTabUiState
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileTabViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileTabUiState())
    val uiState: StateFlow<ProfileTabUiState> = _uiState.asStateFlow()

    private val loadInFlight = AtomicBoolean(false)

    /**
     * Loads profile + role-specific stats. Skips if a load is already in progress unless [forceRefresh] is true.
     */
    fun loadTabData(user: AuthUser, role: UserRole, forceRefresh: Boolean = false) {
        if (!forceRefresh && !loadInFlight.compareAndSet(false, true)) {
            return
        }
        if (forceRefresh) {
            loadInFlight.set(true)
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    authUser = user,
                    currentRole = role,
                )
            }
            try {
                val profileResult = profileRepository.fetchProfile(forceRefresh = forceRefresh)
                profileResult.fold(
                    onSuccess = { data -> _uiState.update { st -> st.copy(userProfile = data.profile) } },
                    onFailure = { e -> applyError(e) },
                )
                if (profileResult.isFailure) {
                    return@launch
                }
                when (role) {
                    UserRole.CARRIER -> {
                        profileRepository.fetchCarrierProfile().onSuccess { c ->
                            _uiState.update { st -> st.copy(carrierProfile = c) }
                        }
                        profileRepository.fetchCarrierStats().onSuccess { s ->
                            _uiState.update { st -> st.copy(carrierStats = s) }
                        }
                    }
                    UserRole.SHIPPER -> {
                        profileRepository.fetchUserStats().onSuccess { s ->
                            _uiState.update { st -> st.copy(userStats = s) }
                        }
                    }
                }
            } finally {
                loadInFlight.set(false)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onAvatarChanged() {
        _uiState.update { it.copy(avatarCacheBuster = UUID.randomUUID().toString()) }
    }

    private fun applyError(throwable: Throwable) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _uiState.update { it.copy(errorMessage = err.localizedMessage(appContext)) }
    }
}
