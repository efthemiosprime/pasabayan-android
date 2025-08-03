package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.PhoneVerificationStatus
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import com.efthemiosprime.pasabayan.data.repository.AuthRepositoryImpl
import com.efthemiosprime.pasabayan.data.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * VerificationStatusViewModel - exactly matching iOS VerificationStatusViewModel
 * Manages phone verification status checking and refresh functionality
 * Uses proper repository pattern following Android architecture
 */
class VerificationStatusViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val authRepository: AuthRepository = AuthRepositoryImpl(getApplication())
    
    private val TAG = "VerificationStatusViewModel"
    
    init {
        loadVerificationStatus()
    }
    
    // MARK: - Phone Verification Status
    
    /**
     * Load phone verification status - iOS: loadVerificationStatus()
     */
    fun loadVerificationStatus() {
        Log.d(TAG, "📱 Loading verification status")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.getPhoneVerificationStatus().collect { result ->
                _isLoading.value = false
                
                when (result) {
                    is Result.Success -> {
                        val status = result.data.data
                        _isVerified.value = status.isVerified
                        _phone.value = status.phoneNumber ?: ""
                        
                        Log.d(TAG, "✅ Verification status loaded - verified: ${status.isVerified}")
                        if (status.isVerified && status.phoneNumber != null) {
                            Log.d(TAG, "   📞 Phone: ${status.phoneNumber}")
                        }
                    }
                    is Result.Failure -> {
                        _errorMessage.value = result.error.message
                        _isVerified.value = false // Default to unverified on error
                        
                        Log.e(TAG, "❌ Failed to load verification status - ${result.error.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Refresh verification status - iOS: refresh()
     */
    fun refresh() {
        Log.d(TAG, "🔄 Refreshing verification status")
        loadVerificationStatus()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

