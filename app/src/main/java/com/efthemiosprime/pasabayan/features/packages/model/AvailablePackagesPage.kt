package com.efthemiosprime.pasabayan.features.packages.model

/**
 * Domain envelope for one page of `GET /packages/available`. Mirrors the
 * top-level `nearby` flag and the Laravel pagination block iOS reads via
 * `AvailablePackagesPaginatedResult`.
 *
 * `hasMore` is derived from `currentPage` vs `lastPage` — never from "the
 * page came back empty" (the iOS-parity contract). An empty page mid-stream
 * is a server filter quirk, not the end of the stream.
 */
data class AvailablePackagesPage(
    val packages: List<AvailablePackage>,
    val currentPage: Int,
    val lastPage: Int,
    val total: Int,
    val perPage: Int,
    /**
     * Top-level `nearby` flag from `/packages/available`. `true` → server
     * filtered by proximity to the carrier's home city; `false` → unfiltered;
     * `null` → not applicable (legacy response shape).
     */
    val nearby: Boolean?,
) {
    val hasMore: Boolean get() = currentPage < lastPage
}
