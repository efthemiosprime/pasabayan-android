package com.efthemiosprime.pasabayan.features.support.services

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads a content [Uri] into an OkHttp [MultipartBody.Part]. Centralized so other features
 * (e.g. verification, support) can share the same `ContentResolver`-backed plumbing.
 */
interface AttachmentReader {
    fun readAsPart(fieldName: String, uri: Uri): MultipartBody.Part?
}

@Singleton
class ContentResolverAttachmentReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : AttachmentReader {

    override fun readAsPart(fieldName: String, uri: Uri): MultipartBody.Part? {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val mime = resolver.getType(uri) ?: guessMime(uri) ?: "application/octet-stream"
        val filename = queryDisplayName(uri) ?: defaultFilename(uri)
        val body: RequestBody = bytes.toRequestBody(mime.toMediaTypeOrNull(), 0, bytes.size)
        return MultipartBody.Part.createFormData(fieldName, filename, body)
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        }
    }.getOrNull()

    private fun defaultFilename(uri: Uri): String = uri.lastPathSegment ?: "attachment"

    private fun guessMime(uri: Uri): String? {
        val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase().orEmpty()
        if (ext.isBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }
}
