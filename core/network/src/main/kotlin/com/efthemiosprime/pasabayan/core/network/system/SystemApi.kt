package com.efthemiosprime.pasabayan.core.network.system

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Cross-cutting health + activity log endpoints. Spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Health / activity logs".
 */
interface SystemApi {

    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponseJson>

    @POST("activity-logs")
    suspend fun logActivity(@Body body: ActivityLogJson): Response<ActivityLogResponseJson>
}
