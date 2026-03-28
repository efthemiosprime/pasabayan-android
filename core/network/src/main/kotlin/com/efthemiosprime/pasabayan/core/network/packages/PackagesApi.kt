package com.efthemiosprime.pasabayan.core.network.packages

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

/** Package endpoints per `android-spec/04-packages.md`. */
interface PackagesApi {

    @GET("packages")
    suspend fun getPackages(): Response<PackageRequestsResponseJson>

    @GET("packages/available")
    suspend fun getAvailablePackages(@QueryMap params: Map<String, String>): Response<AvailablePackagesResponseJson>

    @POST("packages")
    suspend fun createPackage(@Body body: CreatePackageRequestJson): Response<PackageRequestResponseJson>

    @GET("packages/{id}")
    suspend fun getPackage(@Path("id") id: Int): Response<PackageRequestResponseJson>

    @PUT("packages/{id}")
    suspend fun updatePackage(
        @Path("id") id: Int,
        @Body body: PackageUpdateRequestJson,
    ): Response<PackageRequestResponseJson>

    @POST("packages/{id}/cancel")
    suspend fun cancelPackage(@Path("id") id: Int): Response<PackageRequestResponseJson>

    @DELETE("packages/{id}")
    suspend fun deletePackage(@Path("id") id: Int): Response<PackageRequestResponseJson>

    @POST("services/request")
    suspend fun createServiceRequest(
        @Body body: CreateServiceRequestBodyJson,
    ): Response<PackageRequestResponseJson>
}
