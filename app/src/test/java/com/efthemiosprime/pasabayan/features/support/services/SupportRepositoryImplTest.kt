package com.efthemiosprime.pasabayan.features.support.services

import android.net.Uri
import com.efthemiosprime.pasabayan.core.network.support.SupportApi
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
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

class SupportRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: SupportRepositoryImpl
    private lateinit var reader: StubAttachmentReader

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
            .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))
            .build()
        reader = StubAttachmentReader()
        repo = SupportRepositoryImpl(
            supportApi = retrofit.create(SupportApi::class.java),
            attachmentReader = reader,
            json = json,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `submit posts multipart fields and returns mapped ticket`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"data":{"id":7,"email":"u@x.com","subject":"s","category":"delivery_issues","priority":"high","description":"d"}}
                """.trimIndent(),
            ),
        )
        val ticket = repo.submit(
            category = SupportCategory.DELIVERY_ISSUES,
            subject = "Carrier never arrived",
            email = "u@x.com",
            priority = SupportPriority.HIGH,
            description = "Detailed description.",
            attachments = emptyList(),
        ).getOrThrow()
        assertEquals(7, ticket.id)
        assertEquals(SupportCategory.DELIVERY_ISSUES, ticket.category)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue("expected multipart Content-Type", contentType.startsWith("multipart/form-data"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("name=\"category\""))
        assertTrue(body.contains("delivery_issues"))
        assertTrue(body.contains("name=\"subject\""))
        assertTrue(body.contains("Carrier never arrived"))
        assertTrue(body.contains("name=\"email\""))
        assertTrue(body.contains("name=\"priority\""))
        assertTrue(body.contains("high"))
        assertTrue(body.contains("name=\"description\""))
    }

    @Test
    fun `submit attaches each uri as a Laravel-array part`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"id":8,"email":"u@x.com","subject":"s","category":"other","priority":"low","description":"d"}}""",
            ),
        )
        val a = mockk<Uri>()
        val b = mockk<Uri>()
        every { a.toString() } returns "content://test/a"
        every { b.toString() } returns "content://test/b"
        reader.parts[a] = MultipartBody.Part.createFormData(
            "attachments[]", "a.png", "AAA".toRequestBody("image/png".toMediaTypeOrNull()),
        )
        reader.parts[b] = MultipartBody.Part.createFormData(
            "attachments[]", "b.png", "BBB".toRequestBody("image/png".toMediaTypeOrNull()),
        )

        repo.submit(
            category = SupportCategory.PACKAGE_TRACKING,
            subject = "Where is my package",
            email = "u@x.com",
            priority = SupportPriority.MEDIUM,
            description = "Need an update on tracking.",
            attachments = listOf(a, b),
        ).getOrThrow()

        val body = server.takeRequest().body.readUtf8()
        val occurrences = body.split("name=\"attachments[]\"").size - 1
        assertEquals("expected two attachments[] parts, body=$body", 2, occurrences)
        assertTrue(body.contains("filename=\"a.png\""))
        assertTrue(body.contains("filename=\"b.png\""))
    }

    @Test
    fun `submit fails when attachment reader returns null`() = runBlocking {
        val a = mockk<Uri>(relaxed = true)
        every { a.toString() } returns "content://test/missing"
        reader.parts[a] = null

        val result = repo.submit(
            category = SupportCategory.OTHER,
            subject = "Subject ok",
            email = "u@x.com",
            priority = SupportPriority.LOW,
            description = "Description here is plenty long.",
            attachments = listOf(a),
        )
        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `submit fails on 500 with server message`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(500).setBody("""{"message":"server down"}"""),
        )
        val result = repo.submit(
            category = SupportCategory.OTHER,
            subject = "Subject",
            email = "u@x.com",
            priority = SupportPriority.LOW,
            description = "Description here is plenty long.",
            attachments = emptyList(),
        )
        assertTrue(result.isFailure)
    }
}

private class StubAttachmentReader : AttachmentReader {
    val parts: MutableMap<Uri, MultipartBody.Part?> = mutableMapOf()
    override fun readAsPart(fieldName: String, uri: Uri): MultipartBody.Part? = parts[uri]
}
