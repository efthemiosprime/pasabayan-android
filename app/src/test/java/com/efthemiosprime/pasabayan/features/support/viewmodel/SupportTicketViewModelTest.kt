package com.efthemiosprime.pasabayan.features.support.viewmodel

import android.net.Uri
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import com.efthemiosprime.pasabayan.features.support.model.SupportTicket
import com.efthemiosprime.pasabayan.features.support.model.SupportTicketDraft
import com.efthemiosprime.pasabayan.features.support.services.SupportRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupportTicketViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeSupportRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeSupportRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `field setters update draft and isSubmittable flips when valid`() = runTest {
        val vm = SupportTicketViewModel(repo)
        assertFalse(vm.uiState.value.draft.isSubmittable)

        vm.selectCategory(SupportCategory.DELIVERY_ISSUES)
        vm.setSubject("Carrier never arrived")
        vm.setEmail("user@example.com")
        vm.setDescription("Detailed description of what happened.")
        vm.selectPriority(SupportPriority.HIGH)

        val draft = vm.uiState.value.draft
        assertEquals(SupportCategory.DELIVERY_ISSUES, draft.category)
        assertEquals(SupportPriority.HIGH, draft.priority)
        assertTrue(draft.isSubmittable)
    }

    @Test
    fun `setSubject caps at MAX_SUBJECT`() = runTest {
        val vm = SupportTicketViewModel(repo)
        val tooLong = "a".repeat(SupportTicketDraft.MAX_SUBJECT + 50)
        vm.setSubject(tooLong)
        assertEquals(SupportTicketDraft.MAX_SUBJECT, vm.uiState.value.draft.subject.length)
    }

    @Test
    fun `attachments cap at MAX_ATTACHMENTS and dedupe`() = runTest {
        val vm = SupportTicketViewModel(repo)
        val uris = List(SupportTicketDraft.MAX_ATTACHMENTS + 2) { idx ->
            mockk<Uri>().also { every { it.toString() } returns "u$idx" }
        }
        uris.forEach(vm::addAttachment)
        // Try a duplicate
        vm.addAttachment(uris.first())
        assertEquals(SupportTicketDraft.MAX_ATTACHMENTS, vm.uiState.value.draft.attachments.size)
    }

    @Test
    fun `submit is a no-op until draft is valid`() = runTest {
        val vm = SupportTicketViewModel(repo)
        vm.submit()
        advanceUntilIdle()
        assertNull(vm.uiState.value.submittedTicket)
        assertFalse(vm.uiState.value.isSubmitting)
        assertEquals(0, repo.submitCalls)
    }

    @Test
    fun `submit success resets draft and exposes ticket`() = runTest {
        val vm = SupportTicketViewModel(repo)
        vm.selectCategory(SupportCategory.OTHER)
        vm.setSubject("Subject ok")
        vm.setEmail("user@example.com")
        vm.setDescription("Long enough description here.")
        repo.submitResult = Result.success(
            SupportTicket(
                id = 42,
                email = "user@example.com",
                subject = "Subject ok",
                category = SupportCategory.OTHER,
                priority = SupportPriority.MEDIUM,
                status = "open",
                description = "Long enough description here.",
                createdAt = null,
                updatedAt = null,
                userId = null,
                attachments = emptyList(),
            ),
        )

        vm.submit()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.submittedTicket)
        assertEquals(42, state.submittedTicket!!.id)
        // Draft reset
        assertEquals(SupportTicketDraft(), state.draft)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `submit failure surfaces error message and keeps draft`() = runTest {
        val vm = SupportTicketViewModel(repo)
        vm.selectCategory(SupportCategory.PAYMENT_BILLING)
        vm.setSubject("Subject ok")
        vm.setEmail("user@example.com")
        vm.setDescription("Long enough description here.")
        repo.submitResult = Result.failure(RuntimeException("server down"))

        vm.submit()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.submittedTicket)
        assertEquals("server down", state.errorMessage)
        assertEquals("Subject ok", state.draft.subject)
    }
}

private class FakeSupportRepository : SupportRepository {
    var submitResult: Result<SupportTicket> = Result.failure(IllegalStateException("not set"))
    var submitCalls = 0
    override suspend fun submit(
        category: SupportCategory,
        subject: String,
        email: String,
        priority: SupportPriority,
        description: String,
        attachments: List<Uri>,
    ): Result<SupportTicket> {
        submitCalls++
        return submitResult
    }
}
