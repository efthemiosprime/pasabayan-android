package com.efthemiosprime.pasabayan.features.profile.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "Clear cache" action from Settings § Storage & Data. Today this:
 *
 *  - Deletes everything under `context.cacheDir` (including the GDPR export written by
 *    [AccountManagementViewModel]).
 *  - Invalidates the in-memory profile cache via `ProfileRepository.fetchProfile(forceRefresh)`.
 *
 * Returns total bytes freed. OkHttp doesn't currently have an on-disk cache configured; if we add
 * one later this is the natural place to delete it.
 */
@Singleton
class CacheClearer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
) {

    suspend fun clear(): ClearCacheResult {
        val cacheDir = context.cacheDir
        val freed = if (cacheDir.exists()) cacheDir.sizeRecursive() else 0L
        cacheDir.deleteContents()
        // Drop the cached profile snapshot so the next tab visit re-fetches.
        profileRepository.fetchProfile(forceRefresh = true)
        return ClearCacheResult(bytesFreed = freed)
    }

    data class ClearCacheResult(val bytesFreed: Long)

    private fun File.deleteContents() {
        if (!isDirectory) return
        listFiles()?.forEach { child ->
            if (child.isDirectory) {
                child.deleteRecursively()
            } else {
                child.delete()
            }
        }
    }

    private fun File.sizeRecursive(): Long {
        if (!exists()) return 0L
        if (isFile) return length()
        return listFiles()?.sumOf { it.sizeRecursive() } ?: 0L
    }
}
