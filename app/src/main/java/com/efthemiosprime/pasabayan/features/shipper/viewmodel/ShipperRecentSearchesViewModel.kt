package com.efthemiosprime.pasabayan.features.shipper.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.features.shipper.model.RecentSearchEntry
import com.efthemiosprime.pasabayan.features.shipper.services.RecentSearchStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Exposes the shipper's recent (city, country) destination searches as a [StateFlow]
 * so `ShipperExploreContent` can react to fresh saves without polling. Reads are
 * synchronous (SharedPreferences); the StateFlow is just the in-memory mirror.
 */
@HiltViewModel
class ShipperRecentSearchesViewModel @Inject constructor(
    private val store: RecentSearchStore,
) : ViewModel() {

    private val _entries = MutableStateFlow(store.fetch())
    val entries: StateFlow<List<RecentSearchEntry>> = _entries.asStateFlow()

    fun save(city: String, country: String) {
        _entries.value = store.save(city, country)
    }
}
