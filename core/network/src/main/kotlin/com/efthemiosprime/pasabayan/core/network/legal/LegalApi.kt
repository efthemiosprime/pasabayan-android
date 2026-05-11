package com.efthemiosprime.pasabayan.core.network.legal

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Legal-document agreement endpoints. Spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Legal".
 * iOS reference: `LegalAgreementService`.
 */
interface LegalApi {

    @GET("legal/status")
    suspend fun getLegalStatus(): Response<LegalStatusResponseJson>

    @POST("legal/agree")
    suspend fun agree(@Body body: AgreementRequestJson): Response<AgreementResponseJson>

    @POST("legal/withdraw")
    suspend fun withdraw(@Body body: WithdrawalRequestJson): Response<WithdrawalResponseJson>
}
