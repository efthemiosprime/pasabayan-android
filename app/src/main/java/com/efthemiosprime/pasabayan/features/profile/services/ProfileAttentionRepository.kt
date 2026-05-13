package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.network.profile.AttentionSignalsJson

/**
 * Read-only access to the profile attention signals (`GET /me/attention`).
 * Failures surface as `Result.failure` so callers (the VM) can decide whether
 * to fall back to empty signals or surface the error — per CLAUDE.md
 * "Result + sealed domain errors".
 */
interface ProfileAttentionRepository {
    suspend fun fetchAttention(): Result<AttentionSignalsJson>
}
