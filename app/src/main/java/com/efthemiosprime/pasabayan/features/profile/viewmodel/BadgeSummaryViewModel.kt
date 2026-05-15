package com.efthemiosprime.pasabayan.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import com.efthemiosprime.pasabayan.features.profile.services.BadgeSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI-facing surface for the singleton [BadgeSummaryRepository]. Compose code
 * reads [summary] + [hasValidData] and calls [refresh] on screen entry / role
 * change. iOS parity: `BadgeSummaryStore` consumed via `@EnvironmentObject`.
 */
@HiltViewModel
class BadgeSummaryViewModel @Inject constructor(
    private val repository: BadgeSummaryRepository,
) : ViewModel() {

    val summary: StateFlow<BadgeSummary?> = repository.summary
    val hasValidData: StateFlow<Boolean> = repository.hasValidData

    /**
     * Triggers a coalesced refresh for [role]. Pass `null` for the dual-role
     * union; pass the active role on a role-scoped screen.
     */
    fun refresh(role: UserRole?) {
        viewModelScope.launch { repository.refresh(role) }
    }
}
