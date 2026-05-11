package com.efthemiosprime.pasabayan.features.profile.viewmodel

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.profile.model.AccountManagementUiState
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Drives `AccountManagementSheet`. Two destructive actions per `09-profile-carrier-consent`:
 *
 * - **Export user data** (`GET /profile/export-data`): pretty-print the JSON, write to the app
 *   cache, and emit a one-shot [shareEvents] event that the UI uses to launch a system share
 *   sheet via [FileProvider].
 * - **Disable account** (`POST /profile/request-deletion`): confirmation dialog with optional
 *   reason text; success expects HTTP 202. On success we emit [signOutEvents] so the host can
 *   tear down the session.
 */
@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountManagementUiState())
    val state: StateFlow<AccountManagementUiState> = _state.asStateFlow()

    private val _shareEvents = Channel<ExportShareRequest>(Channel.BUFFERED)
    val shareEvents = _shareEvents.receiveAsFlow()

    private val _signOutEvents = Channel<Unit>(Channel.BUFFERED)
    val signOutEvents = _signOutEvents.receiveAsFlow()

    private var fileWriter: ExportFileWriter = DefaultExportFileWriter()

    @VisibleForTesting
    internal fun overrideFileWriter(writer: ExportFileWriter) {
        fileWriter = writer
    }

    fun onDeletionReasonChange(value: String) {
        _state.update { it.copy(deletionReason = value, errorMessage = null) }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun exportData() {
        if (_state.value.isExporting) return
        _state.update { it.copy(isExporting = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            repository.exportUserData().fold(
                onSuccess = { bytes ->
                    val written = runCatching {
                        withContext(Dispatchers.IO) {
                            fileWriter.write(appContext, prettyPrintIfJson(bytes))
                        }
                    }.getOrNull()
                    if (written != null) {
                        _shareEvents.send(written)
                        _state.update {
                            it.copy(
                                isExporting = false,
                                infoMessage = appContext.getString(
                                    com.efthemiosprime.pasabayan.R.string.profile_account_export_success,
                                ),
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isExporting = false,
                                errorMessage = appContext.getString(
                                    com.efthemiosprime.pasabayan.R.string.profile_account_export_error,
                                ),
                            )
                        }
                    }
                },
                onFailure = { applyError(it, isDeletion = false) },
            )
        }
    }

    fun requestDelete() {
        if (_state.value.isDeleting) return
        _state.update { it.copy(showDeleteConfirm = true, errorMessage = null) }
    }

    fun cancelDelete() {
        _state.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDelete() {
        val snapshot = _state.value
        if (snapshot.isDeleting) return
        _state.update {
            it.copy(
                showDeleteConfirm = false,
                isDeleting = true,
                errorMessage = null,
                infoMessage = null,
            )
        }
        val reason = snapshot.deletionReason.trim().takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            repository.requestAccountDeletion(reason).fold(
                onSuccess = { data ->
                    val message = data.status?.takeIf { it.isNotBlank() }
                        ?.let { status ->
                            appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.profile_account_deletion_success_formatted,
                                status,
                            )
                        }
                        ?: appContext.getString(
                            com.efthemiosprime.pasabayan.R.string.profile_account_deletion_success,
                        )
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            infoMessage = message,
                        )
                    }
                    _signOutEvents.send(Unit)
                },
                onFailure = { applyError(it, isDeletion = true) },
            )
        }
    }

    private fun applyError(throwable: Throwable, isDeletion: Boolean) {
        val err = (throwable as? DomainErrorMapperException)?.domainError
            ?: DomainError.NetworkError(throwable)
        _state.update {
            it.copy(
                isExporting = if (isDeletion) it.isExporting else false,
                isDeleting = if (isDeletion) false else it.isDeleting,
                errorMessage = err.localizedMessage(appContext),
            )
        }
    }

    private fun prettyPrintIfJson(raw: ByteArray): ByteArray {
        val text = runCatching { String(raw, Charsets.UTF_8) }.getOrNull() ?: return raw
        return runCatching {
            val parsed = PrettyPrintJson.parseToJsonElement(text)
            PrettyPrintJson.encodeToString(JsonElement.serializer(), parsed).toByteArray(Charsets.UTF_8)
        }.getOrDefault(raw)
    }

    private companion object {
        val PrettyPrintJson = Json {
            prettyPrint = true
            isLenient = true
        }
    }
}

/**
 * Identifies an exported-data file ready for the system share sheet. UI calls
 * `FileProvider.getUriForFile` with this path + the configured authority and launches an
 * `ACTION_SEND` intent.
 */
data class ExportShareRequest(val file: File)

internal fun interface ExportFileWriter {
    fun write(context: Context, payload: ByteArray): ExportShareRequest
}

private class DefaultExportFileWriter : ExportFileWriter {
    override fun write(context: Context, payload: ByteArray): ExportShareRequest {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, "pasabayan-data-export.json")
        file.writeBytes(payload)
        return ExportShareRequest(file)
    }
}

/**
 * Helper for the UI: resolves the cache file to a content `Uri` for the configured authority.
 */
fun ExportShareRequest.toContentUri(context: Context) = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file,
)
