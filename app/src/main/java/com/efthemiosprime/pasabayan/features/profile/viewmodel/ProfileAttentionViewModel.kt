package com.efthemiosprime.pasabayan.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.network.profile.AttentionSignalsJson
import com.efthemiosprime.pasabayan.features.profile.services.ProfileAttentionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the cached `AttentionSignals` for the Profile tab badges. Refresh on:
 *  - Profile-tab open (LaunchedEffect at the screen),
 *  - app foreground transitions,
 *  - after the user completes a verification, payout-setup, or rating action.
 *
 * Per CLAUDE.md "no client polling" — refresh is event-driven only. The repo
 * call is deduped via [refreshing] so a Profile-tab open during an in-flight
 * refresh is a no-op.
 *
 * Failures are silenced into the empty signals (no badges) — iOS parity:
 * profile-attention is a "best effort" surface, never an error UI.
 */
@HiltViewModel
class ProfileAttentionViewModel @Inject constructor(
    private val repository: ProfileAttentionRepository,
) : ViewModel() {

    private val _attention = MutableStateFlow(AttentionSignalsJson())
    val attention: StateFlow<AttentionSignalsJson> = _attention.asStateFlow()

    private val refreshing = AtomicBoolean(false)

    fun refresh() {
        if (!refreshing.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                repository.fetchAttention().fold(
                    onSuccess = { signals -> _attention.update { signals } },
                    onFailure = { /* best-effort surface — keep prior signals */ },
                )
            } finally {
                refreshing.set(false)
            }
        }
    }
}
