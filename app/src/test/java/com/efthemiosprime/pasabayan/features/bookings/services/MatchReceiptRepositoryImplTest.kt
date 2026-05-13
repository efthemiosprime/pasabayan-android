package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.network.bookings.MatchReceiptApi
import com.efthemiosprime.pasabayan.features.profile.services.ImageCompressor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MatchReceiptRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: MatchReceiptRepositoryImpl
    private lateinit var compressor: RecordingCompressor

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
        val api = retrofit.create(MatchReceiptApi::class.java)
        compressor = RecordingCompressor()
        repo = MatchReceiptRepositoryImpl(api, compressor, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `uploadReceipt compresses with receipt budget and posts multipart`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "Receipt uploaded",
                    "data": {
                      "receipt_photo": "receipts/match-100.jpg",
                      "receipt_url": "https://cdn.example.com/receipts/match-100.jpg"
                    }
                }""",
            ),
        )

        val result = repo.uploadReceipt(matchId = 100, photoBytes = byteArrayOf(0x01, 0x02, 0x03))
        assertTrue(result.isSuccess)
        val receipt = result.getOrThrow()
        assertEquals("receipts/match-100.jpg", receipt.receiptPhoto)
        assertEquals("https://cdn.example.com/receipts/match-100.jpg", receipt.receiptUrl)
        assertNull("upload response has no uploaded_at — should stay null", receipt.uploadedAt)

        // Compressor was called with receipt-tuned params (1024 / 80).
        assertEquals(1024, compressor.lastMaxDimension)
        assertEquals(80, compressor.lastJpegQuality)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/services/matches/100/receipt", request.path)
        val body = request.body.readUtf8()
        assertTrue("multipart body should include receipt_photo field", body.contains("name=\"receipt_photo\""))
        assertTrue("multipart filename should be receipt.jpg", body.contains("filename=\"receipt.jpg\""))
    }

    @Test
    fun `uploadReceipt returns failure on 422`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(422).setBody("""{"message": "Invalid file"}"""),
        )

        val result = repo.uploadReceipt(matchId = 100, photoBytes = byteArrayOf(0x01))
        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchReceipt returns receipt on 200`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "data": {
                      "receipt_photo": "receipts/match-100.jpg",
                      "receipt_url": "https://cdn.example.com/receipts/match-100.jpg",
                      "uploaded_at": "2026-04-01T12:00:00Z"
                    }
                }""",
            ),
        )

        val result = repo.fetchReceipt(100)
        assertTrue(result.isSuccess)
        val receipt = result.getOrThrow()
        assertNotNull(receipt)
        assertEquals("2026-04-01T12:00:00Z", receipt!!.uploadedAt)
    }

    @Test
    fun `fetchReceipt returns null on 404 (no receipt yet)`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message": "Not found"}"""))

        val result = repo.fetchReceipt(100)
        assertTrue("404 should map to success(null), not failure", result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `fetchReceipt returns null when body data is absent`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success": false, "message": "No receipt"}"""))

        val result = repo.fetchReceipt(100)
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `fetchReceipt returns failure on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message": "boom"}"""))

        val result = repo.fetchReceipt(100)
        assertTrue(result.isFailure)
    }

    private class RecordingCompressor : ImageCompressor {
        var lastMaxDimension: Int = -1
        var lastJpegQuality: Int = -1

        override fun compressToJpeg(input: ByteArray): ByteArray = input

        override fun compressToJpeg(input: ByteArray, maxDimension: Int, jpegQuality: Int): ByteArray {
            lastMaxDimension = maxDimension
            lastJpegQuality = jpegQuality
            return input
        }
    }
}
