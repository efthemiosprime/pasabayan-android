package com.efthemiosprime.pasabayan.features.legal.model

/** Snapshot of the user's outstanding legal obligations. iOS parity: `LegalStatusData`. */
data class LegalStatus(
    val allAgreed: Boolean,
    val pendingDocuments: List<LegalDocument>,
    val pendingCount: Int,
)
