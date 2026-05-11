package com.efthemiosprime.pasabayan.features.profile.model

data class AccountManagementUiState(
    val isExporting: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletionReason: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
