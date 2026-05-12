package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel

/**
 * User-controlled filters for the carrier-explore "browse packages" feed.
 *
 * Parity with iOS `PackageBrowseFilter` in
 * `CarrierBrowsePackagesViewModel.swift`.
 *
 * `searchText` → `q`, plus `urgency`, `maxWeight`, `maxPrice` are forwarded
 * to `/packages/available` as server-side query parameters. `packageType`
 * has no server counterpart today and is applied **client-side** on the
 * loaded buffer (iOS has a matching `// TODO` to move this server-side).
 *
 * `maxWeight` / `maxPrice` are raw user input held as `String`s and parsed
 * to `Double` at serialization time so invalid input doesn't poison the
 * filter state.
 */
data class PackageBrowseFilter(
    val searchText: String = "",
    val urgency: UrgencyLevel? = null,
    val packageType: PackageType? = null,
    val maxWeight: String = "",
    val maxPrice: String = "",
) {
    val isEmpty: Boolean
        get() = searchText.isBlank() &&
            urgency == null &&
            packageType == null &&
            maxWeight.isBlank() &&
            maxPrice.isBlank()

    /**
     * Server-side query parameters. `packageType` is intentionally omitted —
     * it filters the response in the VM. `page` and `per_page` are added by
     * the repo / VM at fetch time, not stored on the filter.
     */
    fun toQueryMap(): Map<String, String> = buildMap {
        if (searchText.isNotBlank()) put("q", searchText.trim())
        urgency?.let { put("urgency", it.name.lowercase()) }
        maxWeight.trim().toDoubleOrNull()?.let { put("max_weight", it.toString()) }
        maxPrice.trim().toDoubleOrNull()?.let { put("max_budget", it.toString()) }
    }
}
