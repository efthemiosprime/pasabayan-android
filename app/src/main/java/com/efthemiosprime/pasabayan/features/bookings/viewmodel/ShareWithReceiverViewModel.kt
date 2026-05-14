package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the "Share with receiver" sheet. Mirrors iOS `ShareWithReceiverViewModel.State`. */
sealed interface ShareWithReceiverState {
    data object Loading : ShareWithReceiverState
    data class Success(val token: ReceiverAccessToken) : ShareWithReceiverState
    data class Error(val message: String) : ShareWithReceiverState
}

/**
 * Drives the shipper-side "Share with receiver" sheet. Mirrors iOS
 * `ShareWithReceiverViewModel`'s three-step chain:
 *
 *   1. `GET /matches/{id}/receiver-access` — fetch existing tokens
 *   2. `DELETE /matches/{id}/receiver-access/{tokenId}` — revoke every active token in parallel
 *   3. `POST /matches/{id}/receiver-access` — create a fresh token (with PIN)
 *
 * Revoke failures are intentionally swallowed (matches iOS behaviour) so a
 * stale-token cleanup error never blocks issuing a new link.
 */
@HiltViewModel
class ShareWithReceiverViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareWithReceiverState>(ShareWithReceiverState.Loading)
    val uiState: StateFlow<ShareWithReceiverState> = _uiState.asStateFlow()

    private var inFlight: Job? = null

    /**
     * Kicks off (or retries) the share flow for [matchId]. No-ops if a chain
     * is already in flight — protects against double `onAppear`/`LaunchedEffect`
     * firings.
     */
    fun start(matchId: Int) {
        if (inFlight?.isActive == true) return
        _uiState.value = ShareWithReceiverState.Loading
        inFlight = viewModelScope.launch {
            try {
                val token = runChain(matchId)
                _uiState.value = ShareWithReceiverState.Success(token)
            } catch (e: Exception) {
                _uiState.value = ShareWithReceiverState.Error(
                    e.message ?: context.getString(R.string.bookings_share_with_receiver_error_generic),
                )
            } finally {
                inFlight = null
            }
        }
    }

    /** Cancels any in-flight share chain. Safe to call from screen dispose. */
    fun cancel() {
        inFlight?.cancel()
        inFlight = null
    }

    private suspend fun runChain(matchId: Int): ReceiverAccessToken {
        val existing = bookingsRepository.getReceiverAccess(matchId).getOrThrow()

        // Revoke each active token in parallel. Swallow per-token failures —
        // a stale cleanup error must not block issuing the new link.
        coroutineScope {
            existing.filter { it.isActive }
                .map { token ->
                    async { runCatching { bookingsRepository.revokeReceiverAccess(matchId, token.id) } }
                }
                .awaitAll()
        }

        return bookingsRepository.createReceiverAccess(matchId, generatePin = true).getOrThrow()
    }
}
