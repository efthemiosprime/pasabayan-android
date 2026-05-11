package com.efthemiosprime.pasabayan.core.network.legal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response shape for `GET /legal/status` (iOS parity: `LegalStatusResponse`). */
@Serializable
data class LegalStatusResponseJson(
    val success: Boolean = false,
    val data: LegalStatusDataJson? = null,
    val message: String? = null,
)

@Serializable
data class LegalStatusDataJson(
    @SerialName("all_agreed") val allAgreed: Boolean? = null,
    @SerialName("pending_documents") val pendingDocuments: List<PendingDocumentJson>? = null,
    @SerialName("pending_count") val pendingCount: Int? = null,
)

@Serializable
data class PendingDocumentJson(
    val id: Int = 0,
    val type: String = "",
    val title: String = "",
    val version: String = "",
)

/** Request body for `POST /legal/agree`. */
@Serializable
data class AgreementRequestJson(
    @SerialName("document_ids") val documentIds: List<Int>,
    @SerialName("device_id") val deviceId: String? = null,
)

@Serializable
data class AgreementResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: AgreementDataJson? = null,
)

@Serializable
data class AgreementDataJson(
    val agreements: List<AgreementRecordJson>? = null,
    @SerialName("all_required_agreed") val allRequiredAgreed: Boolean? = null,
    @SerialName("pending_required_count") val pendingRequiredCount: Int? = null,
)

@Serializable
data class AgreementRecordJson(
    @SerialName("document_type") val documentType: String = "",
    @SerialName("document_version") val documentVersion: String = "",
    @SerialName("agreed_at") val agreedAt: String = "",
)

/** Request body for `POST /legal/withdraw`. */
@Serializable
data class WithdrawalRequestJson(
    @SerialName("document_type") val documentType: String,
)

@Serializable
data class WithdrawalResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: WithdrawalDataJson? = null,
)

@Serializable
data class WithdrawalDataJson(
    @SerialName("document_type") val documentType: String? = null,
    val warning: String? = null,
)
