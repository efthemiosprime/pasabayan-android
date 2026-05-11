package com.efthemiosprime.pasabayan.features.verification.model

data class PremiumVerificationUiState(
    val idType: IdDocumentType = IdDocumentType.DRIVERS_LICENSE,
    val idNumber: String = "",
    val birthDate: String = "",
    val idDocumentFront: ByteArray? = null,
    val idDocumentBack: ByteArray? = null,
    val selfieWithId: ByteArray? = null,
    val isSubmitting: Boolean = false,
    val isLoadingStatus: Boolean = false,
    val isSubmitted: Boolean = false,
    val pendingStatus: PremiumApplicationStatus? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isReadyToSubmit: Boolean
        get() = !isSubmitting &&
            idDocumentFront != null &&
            (!idType.requiresBackImage || idDocumentBack != null) &&
            selfieWithId != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PremiumVerificationUiState) return false
        return idType == other.idType &&
            idNumber == other.idNumber &&
            birthDate == other.birthDate &&
            idDocumentFront.contentEqualsOrBothNull(other.idDocumentFront) &&
            idDocumentBack.contentEqualsOrBothNull(other.idDocumentBack) &&
            selfieWithId.contentEqualsOrBothNull(other.selfieWithId) &&
            isSubmitting == other.isSubmitting &&
            isLoadingStatus == other.isLoadingStatus &&
            isSubmitted == other.isSubmitted &&
            pendingStatus == other.pendingStatus &&
            errorMessage == other.errorMessage &&
            successMessage == other.successMessage
    }

    override fun hashCode(): Int {
        var result = idType.hashCode()
        result = 31 * result + idNumber.hashCode()
        result = 31 * result + birthDate.hashCode()
        result = 31 * result + (idDocumentFront?.contentHashCode() ?: 0)
        result = 31 * result + (idDocumentBack?.contentHashCode() ?: 0)
        result = 31 * result + (selfieWithId?.contentHashCode() ?: 0)
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + isLoadingStatus.hashCode()
        result = 31 * result + isSubmitted.hashCode()
        result = 31 * result + (pendingStatus?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (successMessage?.hashCode() ?: 0)
        return result
    }
}

private fun ByteArray?.contentEqualsOrBothNull(other: ByteArray?): Boolean =
    if (this == null && other == null) true
    else if (this == null || other == null) false
    else this.contentEquals(other)
