package com.efthemiosprime.pasabayan.features.chat.viewmodel

/**
 * State machine for the in-chat receipt upload flow (carrier-side). Mirrors
 * iOS `ChatReceiptUploadSheet` view-state.
 */
sealed interface ChatReceiptUploadState {
    /** Sheet just opened — show the Choose-Photo CTA. */
    data object Idle : ChatReceiptUploadState

    /** Compression + multipart upload in flight. */
    data object Uploading : ChatReceiptUploadState

    /** Upload succeeded — host auto-dismisses and refreshes the chat thread. */
    data object Success : ChatReceiptUploadState

    /** Recoverable error; sheet stays open with "Try Again". */
    data class Error(val message: String) : ChatReceiptUploadState
}
