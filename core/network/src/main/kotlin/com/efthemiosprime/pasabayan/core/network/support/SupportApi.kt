package com.efthemiosprime.pasabayan.core.network.support

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Support ticket endpoint. Spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Support". iOS reference:
 * `SupportTicketService`.
 *
 * The repository assembles the [MultipartBody] (text fields + `attachments[]` files) so the
 * call site can decide how to read each attachment URI; this keeps the Retrofit surface tiny.
 */
interface SupportApi {

    @POST("support/tickets")
    suspend fun submitTicket(@Body body: MultipartBody): Response<SupportTicketResponseJson>
}
