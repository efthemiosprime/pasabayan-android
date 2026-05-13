package com.efthemiosprime.pasabayan.features.chat.viewmodel

import com.efthemiosprime.pasabayan.features.bookings.model.MatchReceipt
import com.efthemiosprime.pasabayan.features.bookings.services.MatchReceiptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatReceiptUploadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeRepo
    private lateinit var viewModel: ChatReceiptUploadViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeRepo()
        viewModel = ChatReceiptUploadViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uploadReceipt success transitions Idle to Uploading to Success`() = runTest {
        fakeRepo.uploadResult = Result.success(
            MatchReceipt(receiptPhoto = "x.jpg", receiptUrl = "https://x", uploadedAt = null),
        )

        viewModel.uploadReceipt(matchId = 100, photoBytes = byteArrayOf(0x01))
        // Before coroutine runs the repo call, state is Uploading.
        assertEquals(ChatReceiptUploadState.Uploading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(ChatReceiptUploadState.Success, viewModel.uiState.value)
    }

    @Test
    fun `uploadReceipt failure surfaces Error with message`() = runTest {
        fakeRepo.uploadResult = Result.failure(Exception("server down"))

        viewModel.uploadReceipt(matchId = 100, photoBytes = byteArrayOf(0x01))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("expected Error, got $state", state is ChatReceiptUploadState.Error)
        assertEquals("server down", (state as ChatReceiptUploadState.Error).message)
    }

    @Test
    fun `reset returns to Idle`() = runTest {
        fakeRepo.uploadResult = Result.success(
            MatchReceipt(receiptPhoto = "x", receiptUrl = "https://x"),
        )
        viewModel.uploadReceipt(100, byteArrayOf(0x01))
        advanceUntilIdle()
        assertEquals(ChatReceiptUploadState.Success, viewModel.uiState.value)

        viewModel.reset()
        assertEquals(ChatReceiptUploadState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `uploadReceipt forwards matchId and bytes to repository`() = runTest {
        fakeRepo.uploadResult = Result.success(
            MatchReceipt(receiptPhoto = "x", receiptUrl = "https://x"),
        )
        val bytes = byteArrayOf(0x0A, 0x0B, 0x0C)

        viewModel.uploadReceipt(matchId = 42, photoBytes = bytes)
        advanceUntilIdle()

        assertEquals(42, fakeRepo.lastMatchId)
        assertEquals(listOf<Byte>(0x0A, 0x0B, 0x0C), fakeRepo.lastBytes?.toList())
    }

    private class FakeRepo : MatchReceiptRepository {
        var uploadResult: Result<MatchReceipt> = Result.failure(Exception("Not set"))
        var lastMatchId: Int = -1
        var lastBytes: ByteArray? = null

        override suspend fun uploadReceipt(matchId: Int, photoBytes: ByteArray): Result<MatchReceipt> {
            lastMatchId = matchId
            lastBytes = photoBytes
            return uploadResult
        }

        override suspend fun fetchReceipt(matchId: Int): Result<MatchReceipt?> =
            Result.failure(Exception("Not used"))
    }
}
