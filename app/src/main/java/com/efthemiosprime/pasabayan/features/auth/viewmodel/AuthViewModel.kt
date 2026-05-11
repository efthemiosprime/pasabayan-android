package com.efthemiosprime.pasabayan.features.auth.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.session.TokenStore
import com.efthemiosprime.pasabayan.core.session.UnauthorizedSessionNotifier
import com.efthemiosprime.pasabayan.features.auth.services.FacebookLoginCancelledException
import com.efthemiosprime.pasabayan.features.auth.services.FacebookLoginStarter
import com.efthemiosprime.pasabayan.features.auth.services.GoogleSignInHelper
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationLifecycleManager
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import android.util.Log
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

/** Post-auth consent onboarding (iOS `hasCompletedConsentSetup` in `ContentView`). */
enum class ConsentSetupPhase {
    NeedsSetup,
    Complete,
}

data class AuthScreenState(
    val session: SessionUiState = SessionUiState.Checking,
    val isBusy: Boolean = false,
    val transientError: String? = null,
    /** Non-null only while [SessionUiState.SignedIn]; cleared when signed out. */
    val citySetupPhase: CitySetupPhase? = null,
    /** After city is complete; non-null while signed in with city done. Cleared when signed out. */
    val consentSetupPhase: ConsentSetupPhase? = null,
    /**
     * One-shot after consent onboarding completes ([markConsentOnboardingComplete]).
     * Parity with iOS `OnboardingState.didJustCompleteConsent` (carrier dashboard uses first frame).
     */
    val didJustCompleteConsent: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val googleSignInHelper: GoogleSignInHelper,
    private val facebookLoginStarter: FacebookLoginStarter,
    private val onboardingPreferences: OnboardingPreferences,
    private val unauthorizedSessionNotifier: UnauthorizedSessionNotifier,
    private val notificationLifecycleManager: NotificationLifecycleManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenState())
    val uiState: StateFlow<AuthScreenState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        refreshSession()
        viewModelScope.launch {
            unauthorizedSessionNotifier.events.collect {
                handleUnauthorizedFromNetwork()
            }
        }
    }

    fun consumeDidJustCompleteConsent() {
        _uiState.update { it.copy(didJustCompleteConsent = false) }
    }

    fun refreshSession() {
        Log.d(TAG, "refreshSession: checking stored token...")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    session = SessionUiState.Checking,
                    transientError = null,
                    citySetupPhase = null,
                    consentSetupPhase = null,
                    didJustCompleteConsent = false,
                )
            }
            val token = tokenStore.getToken()
            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        session = SessionUiState.SignedOut,
                        citySetupPhase = null,
                        consentSetupPhase = null,
                        didJustCompleteConsent = false,
                    )
                }
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
                            consentSetupPhase = null,
                            didJustCompleteConsent = false,
                            transientError = e.toLocalizedUserMessage(),
                        )
                    }
                },
            )
        }
    }

    fun onGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            Log.d(TAG, "onGoogleSignInResult: task received")
            _uiState.update { it.copy(isBusy = true, transientError = null) }
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google account: ${account.email}, hasIdToken=${account.idToken != null}")
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    Log.e(TAG, "Google sign-in: no ID token returned")
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            transientError = appContext.getString(R.string.auth_error_google_no_id_token),
                        )
                    }
                    return@launch
                }
                Log.d(TAG, "Calling backend loginWithProviderAccessToken(google)...")
                authRepository.loginWithProviderAccessToken("google", idToken).fold(
                    onSuccess = { user ->
                        Log.d(TAG, "Backend login SUCCESS: userId=${user.id}, name=${user.name}")
                        applySignedInWithCityGate(user)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Backend login FAILED: ${e.javaClass.simpleName}: ${e.message}")
                        _uiState.update {
                            it.copy(isBusy = false, transientError = e.toLocalizedUserMessage())
                        }
                    },
                )
            } catch (e: ApiException) {
                Log.e(TAG, "Google ApiException: statusCode=${e.statusCode}, message=${e.message}")
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
            // Unregister FCM token while we still have a valid bearer; ignore failures so the
            // user can always sign out even if the device-token endpoint is down.
            runCatching { notificationLifecycleManager.unregisterAndClear() }
            authRepository.logout()
            googleSignInHelper.signOutGoogle()
            _uiState.update {
                it.copy(
                    session = SessionUiState.SignedOut,
                    isBusy = false,
                    citySetupPhase = null,
                    consentSetupPhase = null,
                    didJustCompleteConsent = false,
                )
            }
        }
    }

    fun markCityOnboardingComplete() {
        viewModelScope.launch {
            onboardingPreferences.setHasCompletedCitySetup(true)
            val consentDone = onboardingPreferences.hasCompletedConsentSetup()
            _uiState.update {
                it.copy(
                    citySetupPhase = CitySetupPhase.Complete,
                    consentSetupPhase = if (consentDone) {
                        ConsentSetupPhase.Complete
                    } else {
                        ConsentSetupPhase.NeedsSetup
                    },
                )
            }
        }
    }

    fun markConsentOnboardingComplete() {
        viewModelScope.launch {
            onboardingPreferences.setHasCompletedConsentSetup(true)
            _uiState.update {
                it.copy(
                    consentSetupPhase = ConsentSetupPhase.Complete,
                    didJustCompleteConsent = true,
                )
            }
        }
    }

    private suspend fun applySignedInWithCityGate(user: AuthUser) {
        val cityDone = onboardingPreferences.hasCompletedCitySetup()
        val consentDone = onboardingPreferences.hasCompletedConsentSetup()
        Log.d(TAG, "applySignedInWithCityGate: user=${user.name}, cityDone=$cityDone, consentDone=$consentDone")
        val consentPhase = when {
            !cityDone -> null
            !consentDone -> ConsentSetupPhase.NeedsSetup
            else -> ConsentSetupPhase.Complete
        }
        _uiState.update {
            it.copy(
                session = SessionUiState.SignedIn(user),
                isBusy = false,
                citySetupPhase = if (cityDone) CitySetupPhase.Complete else CitySetupPhase.NeedsSetup,
                consentSetupPhase = consentPhase,
                didJustCompleteConsent = false,
            )
        }
    }

    private fun handleUnauthorizedFromNetwork() {
        googleSignInHelper.signOutGoogle()
        _uiState.update { s ->
            s.copy(
                session = SessionUiState.SignedOut,
                isBusy = false,
                citySetupPhase = null,
                consentSetupPhase = null,
                didJustCompleteConsent = false,
                transientError = appContext.getString(R.string.error_unauthorized),
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
