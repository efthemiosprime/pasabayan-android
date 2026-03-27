package com.efthemiosprime.pasabayan.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.userMessage
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.session.TokenStore
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
import javax.inject.Inject

sealed interface SessionUiState {
    data object Checking : SessionUiState
    data object SignedOut : SessionUiState
    data class SignedIn(val user: AuthUser) : SessionUiState
}

data class AuthScreenState(
    val session: SessionUiState = SessionUiState.Checking,
    val isBusy: Boolean = false,
    val transientError: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val googleSignInHelper: GoogleSignInHelper,
    private val facebookLoginStarter: FacebookLoginStarter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenState())
    val uiState: StateFlow<AuthScreenState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(session = SessionUiState.Checking, transientError = null) }
            val token = tokenStore.getToken()
            if (token.isNullOrBlank()) {
                _uiState.update { it.copy(session = SessionUiState.SignedOut) }
                return@launch
            }
            authRepository.loadCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(session = SessionUiState.SignedIn(user)) }
                },
                onFailure = { e ->
                    tokenStore.clear()
                    googleSignInHelper.signOutGoogle()
                    _uiState.update { s ->
                        s.copy(
                            session = SessionUiState.SignedOut,
                            transientError = e.toUserMessage(),
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
                        it.copy(isBusy = false, transientError = "Google did not return an ID token")
                    }
                    return@launch
                }
                authRepository.loginWithProviderAccessToken("google", idToken).fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(session = SessionUiState.SignedIn(user), isBusy = false)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isBusy = false, transientError = e.toUserMessage())
                        }
                    },
                )
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    _uiState.update { it.copy(isBusy = false) }
                } else {
                    _uiState.update {
                        it.copy(isBusy = false, transientError = e.message ?: "Google sign-in failed")
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
                                _uiState.update {
                                    it.copy(session = SessionUiState.SignedIn(user), isBusy = false)
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(isBusy = false, transientError = e.toUserMessage())
                                }
                            },
                        )
                    },
                    onFailure = { e ->
                        if (e is FacebookLoginCancelledException) {
                            _uiState.update { it.copy(transientError = null) }
                        } else {
                            _uiState.update {
                                it.copy(transientError = e.message ?: "Facebook sign-in failed")
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
                it.copy(session = SessionUiState.SignedOut, isBusy = false)
            }
        }
    }

    fun clearTransientError() {
        _uiState.update { it.copy(transientError = null) }
    }

    fun googleSignInIntent() = googleSignInHelper.signInIntent

    private fun Throwable.toUserMessage(): String {
        (this as? DomainErrorMapperException)?.domainError?.userMessage()?.let { return it }
        return message ?: "Something went wrong"
    }
}
