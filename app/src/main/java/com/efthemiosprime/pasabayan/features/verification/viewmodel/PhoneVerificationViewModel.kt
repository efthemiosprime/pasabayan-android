package com.efthemiosprime.pasabayan.features.verification.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.verification.model.PhoneVerificationUiState
import com.efthemiosprime.pasabayan.features.verification.services.VerificationRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives `PhoneVerificationSheet` (`10-verification.md` § Phone OTP). E.164 normalization,
 * 60-second resend gate, optimistic state, and surface-level error reconciliation match iOS
 * `PhoneVerificationViewModel` behavior. We trim the iOS Combine + dual-timeout plumbing — kotlin
 * coroutines + `runCatching` cover the same cases more compactly.
 */
@HiltViewModel
class PhoneVerificationViewModel @Inject constructor(
    private val repository: VerificationRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneVerificationUiState())
    val state: StateFlow<PhoneVerificationUiState> = _state.asStateFlow()

    private var resendTimerJob: Job? = null

    fun bootstrap() {
        viewModelScope.launch {
            repository.fetchPhoneStatus().onSuccess { status ->
                _state.update {
                    it.copy(
                        phoneNumber = status.phone?.removePrefix(it.countryCode)
                            ?.removePrefix("+1")
                            ?.takeIf { v -> v.isNotBlank() }
                            ?: it.phoneNumber,
                        isVerified = status.phoneVerified,
                        // Pending verification (sent earlier but not verified) → resume on the OTP step.
                        isOtpSent = status.pendingVerifications > 0 && !status.phoneVerified,
                    )
                }
            }
        }
    }

    fun onPhoneNumberChange(value: String) {
        _state.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onOtpCodeChange(value: String) {
        val trimmed = value.filter { it.isDigit() }.take(PhoneVerificationUiState.OTP_LENGTH)
        _state.update { it.copy(otpCode = trimmed, errorMessage = null) }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun sendOtp() {
        val snapshot = _state.value
        if (snapshot.isLoading) return
        if (!snapshot.isPhoneValid) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.verification_phone_error_invalid,
                    ),
                )
            }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            repository.sendOtp(snapshot.formattedE164Phone).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.verification_phone_otp_sent,
                            ),
                        )
                    }
                    startResendTimer()
                },
                onFailure = { applyError(it) },
            )
        }
    }

    fun verifyOtp() {
        val snapshot = _state.value
        if (snapshot.isLoading) return
        if (!snapshot.isOtpValid) {
            _state.update {
                it.copy(
                    errorMessage = appContext.getString(
                        com.efthemiosprime.pasabayan.R.string.verification_phone_error_otp_invalid,
                    ),
                )
            }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            repository.verifyOtp(snapshot.formattedE164Phone, snapshot.otpCode).fold(
                onSuccess = {
                    stopResendTimer()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isVerified = true,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.verification_phone_success,
                            ),
                        )
                    }
                },
                onFailure = { applyError(it) },
            )
        }
    }

    fun resendOtp() {
        val snapshot = _state.value
        if (!snapshot.canResend || snapshot.isLoading) return
        _state.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                canResend = false,
                otpCode = "",
            )
        }
        viewModelScope.launch {
            repository.resendOtp(snapshot.formattedE164Phone).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.verification_phone_otp_resent,
                            ),
                        )
                    }
                    startResendTimer()
                },
                onFailure = {
                    _state.update { it.copy(canResend = true) }
                    applyError(it)
                },
            )
        }
    }

    fun reset() {
        stopResendTimer()
        _state.update { PhoneVerificationUiState() }
    }

    private fun startResendTimer() {
        stopResendTimer()
        resendTimerJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    canResend = false,
                    remainingResendSeconds = PhoneVerificationUiState.RESEND_COOLDOWN_SECONDS,
                )
            }
            while (true) {
                val current = _state.value.remainingResendSeconds
                if (current <= 0) {
                    _state.update { it.copy(canResend = true) }
                    return@launch
                }
                delay(1_000)
                _state.update { it.copy(remainingResendSeconds = it.remainingResendSeconds - 1) }
            }
        }
    }

    private fun stopResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = null
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

    override fun onCleared() {
        super.onCleared()
        stopResendTimer()
    }
}
