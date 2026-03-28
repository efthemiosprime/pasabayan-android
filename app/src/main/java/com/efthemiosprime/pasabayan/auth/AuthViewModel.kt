package com.efthemiosprime.pasabayan.auth

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.error.localizedMessage
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.session.TokenStore
import com.efthemiosprime.pasabayan.onboarding.OnboardingPreferences
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

sealed interface SessionUiState {
    data object Checking : SessionUiState
    data object SignedOut : SessionUiState
    data class SignedIn(val user: AuthUser) : SessionUiState
}

/** Post-auth city onboarding gate (iOS `hasCompletedCitySetup` in `ContentView`). */
enum class CitySetupPhase {
    NeedsSetup,
    Complete,
}

data class AuthScreenState(
    val session: SessionUiState = SessionUiState.Checking,
    val isBusy: Boolean = false,
    val transientError: String? = null,
    /** Non-null only while [SessionUiState.SignedIn]; cleared when signed out. */
    val citySetupPhase: CitySetupPhase? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val googleSignInHelper: GoogleSignInHelper,
    private val facebookLoginStarter: FacebookLoginStarter,
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenState())
    val uiState: StateFlow<AuthScreenState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(session = SessionUiState.Checking, transientError = null, citySetupPhase = null)
            }
            val token = tokenStore.getToken()
            if (token.isNullOrBlank()) {
                _uiState.update { it.copy(session = SessionUiState.SignedOut, citySetupPhase = null) }
                return@launch
            }
            authRepository.loadCurrentUser().fold(
                onSuccess = { user -> applySignedInWithCityGate(user) },
                onFailure = { e ->
                    tokenStore.clear()
                    googleSignInHelper.signOutGoogle()
                    _uiState.update { s ->
                        s.copy(
                            session = SessionUiState.SignedOut,
                            citySetupPhase = null,
                            transientError = e.toLocalizedUserMessage(),
                        )
                    }
                },
            )
        }
    }

    fun onGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, transientError = null) }
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            transientError = appContext.getString(R.string.auth_error_google_no_id_token),
                        )
                    }
                    return@launch
                }
                authRepository.loginWithProviderAccessToken("google", idToken).fold(
                    onSuccess = { user ->
                        applySignedInWithCityGate(user)
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isBusy = false, transientError = e.toLocalizedUserMessage())
                        }
                    },
                )
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    _uiState.update { it.copy(isBusy = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            transientError = e.message
                                ?: appContext.getString(R.string.auth_error_google_sign_in_failed),
                        )
                    }
                }
            }
        }
    }

    fun signInWithFacebook(activity: Activity) {
        facebookLoginStarter.startLogin(activity) { fbResult ->
            viewModelScope.launch {
                fbResult.fold(
                    onSuccess = { accessToken ->
                        _uiState.update { it.copy(isBusy = true, transientError = null) }
                        authRepository.loginWithProviderAccessToken("facebook", accessToken).fold(
                            onSuccess = { user ->
                                applySignedInWithCityGate(user)
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(isBusy = false, transientError = e.toLocalizedUserMessage())
                                }
                            },
                        )
                    },
                    onFailure = { e ->
                        if (e is FacebookLoginCancelledException) {
                            _uiState.update { it.copy(transientError = null) }
                        } else {
                            _uiState.update {
                                it.copy(
                                    transientError = e.message
                                        ?: appContext.getString(R.string.auth_error_facebook_sign_in_failed),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, transientError = null) }
            authRepository.logout()
            googleSignInHelper.signOutGoogle()
            _uiState.update {
                it.copy(session = SessionUiState.SignedOut, isBusy = false, citySetupPhase = null)
            }
        }
    }

    fun markCityOnboardingComplete() {
        viewModelScope.launch {
            onboardingPreferences.setHasCompletedCitySetup(true)
            _uiState.update { it.copy(citySetupPhase = CitySetupPhase.Complete) }
        }
    }

    private suspend fun applySignedInWithCityGate(user: AuthUser) {
        val cityDone = onboardingPreferences.hasCompletedCitySetup()
        _uiState.update {
            it.copy(
                session = SessionUiState.SignedIn(user),
                isBusy = false,
                citySetupPhase = if (cityDone) CitySetupPhase.Complete else CitySetupPhase.NeedsSetup,
            )
        }
    }

    fun clearTransientError() {
        _uiState.update { it.copy(transientError = null) }
    }

    fun googleSignInIntent() = googleSignInHelper.signInIntent

    private fun Throwable.toLocalizedUserMessage(): String {
        (this as? DomainErrorMapperException)?.domainError?.let { return it.localizedMessage(appContext) }
        return message ?: appContext.getString(R.string.error_generic)
    }
}
