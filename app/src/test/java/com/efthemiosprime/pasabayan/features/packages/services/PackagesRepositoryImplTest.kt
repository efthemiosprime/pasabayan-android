package com.efthemiosprime.pasabayan.features.packages.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.PackagesApi
import com.efthemiosprime.pasabayan.core.network.packages.ShoppingItemJson
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PackagesRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: PackagesRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(PackagesApi::class.java)
        repo = PackagesRepositoryImpl(api, json, TestMultipartFormDataFactory())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `loadPackages returns packages on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "pickup_city": "Toronto", "delivery_city": "Montreal", "request_status": "open"},
                            {"id": 2, "pickup_city": "Ottawa", "delivery_city": "Calgary", "request_status": "matched"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadPackages()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertEquals("Toronto", result.getOrThrow()[0].pickupCity)
    }

    @Test
    fun `loadPackages returns failure on error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Unauthenticated"}"""))

        val result = repo.loadPackages()
        assertTrue(result.isFailure)
    }

    @Test
    fun `loadPackages returns empty list on no data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message": "OK", "data": {"data": [], "current_page": 1, "last_page": 1, "total": 0, "per_page": 15}}""",
            ),
        )

        val result = repo.loadPackages()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `getPackage returns package on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message": "OK", "data": {"id": 10, "pickup_city": "Vancouver", "delivery_city": "Toronto"}}""",
            ),
        )

        val result = repo.getPackage(10)
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrThrow().id)
    }

    @Test
    fun `getPackage returns failure on 404`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Not found"}"""))

        val result = repo.getPackage(999)
        assertTrue(result.isFailure)
    }

    @Test
    fun `cancelPackage returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Cancelled", "data": {"id": 10, "request_status": "cancelled"}}""",
            ),
        )

        val result = repo.cancelPackage(10)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelPackage returns failure on 403`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"message":"Not authorized"}"""))

        val result = repo.cancelPackage(10)
        assertTrue(result.isFailure)
    }

    @Test
    fun `createPackage sends JSON request when no images`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message":"Created","data":{"id": 11, "pickup_city":"Toronto","delivery_city":"Montreal","request_status":"open"}}""",
            ),
        )

        val result = repo.createPackage(createPackageBody(), imageUris = emptyList())
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("/api/packages", request.path)
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue(contentType.contains("application/json"))
    }

    @Test
    fun `createPackage sends multipart request when images present`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message":"Created","data":{"id": 12, "pickup_city":"Toronto","delivery_city":"Ottawa","request_status":"open"}}""",
            ),
        )

        val result = repo.createPackage(
            request = createPackageBody(),
            imageUris = listOf(Uri.parse("content://images/1")),
        )
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("/api/packages", request.path)
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue(contentType.contains("multipart/form-data"))
        assertTrue(request.body.readUtf8().contains("images[]"))
    }

    @Test
    fun `updatePackageWithImages sends JSON PUT when no images`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message":"OK","data":{"id":20,"pickup_city":"Toronto","delivery_city":"Montreal","request_status":"open"}}""",
            ),
        )

        val result = repo.updatePackageWithImages(
            id = 20,
            request = PackageUpdateRequestJson(maxPriceBudget = 99.0),
            imageUris = emptyList(),
        )
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("/api/packages/20", request.path)
        assertEquals("PUT", request.method)
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue(contentType.contains("application/json"))
    }

    @Test
    fun `updatePackageWithImages sends multipart PUT when images present`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message":"OK","data":{"id":21,"pickup_city":"Toronto","delivery_city":"Montreal","request_status":"open","images_processing":true}}""",
            ),
        )

        val result = repo.updatePackageWithImages(
            id = 21,
            request = PackageUpdateRequestJson(urgencyLevel = "high"),
            imageUris = listOf(Uri.parse("content://images/1")),
        )
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("/api/packages/21", request.path)
        assertEquals("PUT", request.method)
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue(contentType.contains("multipart/form-data"))
        assertTrue(request.body.readUtf8().contains("images[]"))
    }

    @Test
    fun `createServiceRequest returns package on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message":"Created","data":{"id": 25, "pickup_city":"Toronto","delivery_city":"Toronto","request_status":"open","service_type":"grocery_shopping"}}""",
            ),
        )

        val result = repo.createServiceRequest(
            CreateServiceRequestBodyJson(
                serviceType = "grocery_shopping",
                shoppingList = listOf(ShoppingItemJson(item = "Milk", quantity = "2L")),
                deliveryCity = "Toronto",
                deliveryAddress = "123 Main St",
            ),
        )
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("/api/services/request", request.path)
    }

    private fun createPackageBody(): CreatePackageRequestJson = CreatePackageRequestJson(
        pickupAddress = "123 Main",
        pickupCity = "Toronto",
        pickupCountry = "CA",
        deliveryAddress = "999 Elm",
        deliveryCity = "Montreal",
        deliveryCountry = "CA",
        packageWeightKg = 2.5,
        packageType = "general",
        fragile = false,
        urgencyLevel = "normal",
        pickupDatePreferred = "2026-04-01",
        pickupDateFlexible = false,
        deliveryDateNeeded = "2026-04-02",
    )
}

private class TestMultipartFormDataFactory : MultipartFormDataFactory() {
    override fun createPackageFields(request: CreatePackageRequestJson): Map<String, RequestBody> = mapOf(
        "pickup_address" to request.pickupAddress.toRequestBody("text/plain".toMediaType()),
        "pickup_city" to request.pickupCity.toRequestBody("text/plain".toMediaType()),
    )

    override fun createImageParts(imageUris: List<Uri>): List<MultipartBody.Part> {
        val imageBody = "fake".toByteArray().toRequestBody("image/jpeg".toMediaType())
        return imageUris.mapIndexed { index, _ ->
            MultipartBody.Part.createFormData("images[]", "image_$index.jpg", imageBody)
        }
    }
}
