package com.efthemiosprime.pasabayan.features.legal.viewmodel

import com.efthemiosprime.pasabayan.features.legal.model.LegalDocument
import com.efthemiosprime.pasabayan.features.legal.model.LegalStatus
import com.efthemiosprime.pasabayan.features.legal.services.AgreementOutcome
import com.efthemiosprime.pasabayan.features.legal.services.LegalAssetCatalog
import com.efthemiosprime.pasabayan.features.legal.services.LegalRepository
import com.efthemiosprime.pasabayan.features.legal.services.WithdrawalOutcome
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LegalAgreementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeLegalRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeLegalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleStatus = LegalStatus(
        allAgreed = false,
        pendingDocuments = listOf(
            LegalDocument(1, "terms_of_service", "Terms", "1.0"),
            LegalDocument(2, "privacy_policy", "Privacy", "2.0"),
        ),
        pendingCount = 2,
    )

    @Test
    fun `loadStatus populates state on success`() = runTest {
        repo.statusResult = Result.success(sampleStatus)
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.loadStatus()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(2, state.status?.pendingDocuments?.size)
        assertFalse(state.isLoading)
        assertTrue(state.hasPending)
    }

    @Test
    fun `loadStatus surfaces error`() = runTest {
        repo.statusResult = Result.failure(RuntimeException("boom"))
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.loadStatus()
        advanceUntilIdle()
        assertEquals("boom", vm.uiState.value.errorMessage)
    }

    @Test
    fun `agreeToAllPending sends ids and clears list on full agreement`() = runTest {
        repo.statusResult = Result.success(sampleStatus)
        repo.agreeResult = Result.success(AgreementOutcome(allRequiredAgreed = true, pendingRequiredCount = 0))
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.loadStatus()
        advanceUntilIdle()

        vm.agreeToAllPending(deviceId = "Android_abc12345")
        advanceUntilIdle()

        assertEquals(listOf(1, 2), repo.agreeIds)
        assertEquals("Android_abc12345", repo.agreeDeviceId)
        val state = vm.uiState.value
        assertTrue("expected justAgreed flag", state.justAgreed)
        assertEquals(true, state.status?.allAgreed)
        assertTrue(state.status?.pendingDocuments.isNullOrEmpty())
    }

    @Test
    fun `agree on partial outcome removes only specified ids`() = runTest {
        repo.statusResult = Result.success(sampleStatus)
        repo.agreeResult = Result.success(AgreementOutcome(allRequiredAgreed = false, pendingRequiredCount = 1))
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.loadStatus()
        advanceUntilIdle()

        vm.agree(listOf(1))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.justAgreed)
        assertEquals(1, state.status?.pendingDocuments?.size)
        assertEquals("privacy_policy", state.status?.pendingDocuments?.first()?.type)
        assertEquals(1, state.status?.pendingCount)
    }

    @Test
    fun `agree with empty list is a no-op`() = runTest {
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.agree(emptyList())
        advanceUntilIdle()
        assertTrue(repo.agreeIds.isEmpty())
    }

    @Test
    fun `withdraw reloads status and surfaces warning`() = runTest {
        repo.withdrawResult = Result.success(WithdrawalOutcome("marketing_communications", "Some services may be limited"))
        repo.statusResult = Result.success(sampleStatus.copy(pendingCount = 0, pendingDocuments = emptyList(), allAgreed = true))
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.withdraw("marketing_communications")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Some services may be limited", state.withdrawWarning)
        assertTrue(state.status?.allAgreed == true)
    }

    @Test
    fun `withdraw blank type is a no-op`() = runTest {
        val vm = LegalAgreementViewModel(repo, NoOpAssetCatalog)
        vm.withdraw("")
        advanceUntilIdle()
        assertEquals(0, repo.withdrawCalls.size)
    }
}

private object NoOpAssetCatalog : LegalAssetCatalog {
    override val frenchArticleFiles: Set<String> = emptySet()
}

private class FakeLegalRepository : LegalRepository {
    var statusResult: Result<LegalStatus> = Result.failure(IllegalStateException("not set"))
    var agreeResult: Result<AgreementOutcome> = Result.failure(IllegalStateException("not set"))
    var withdrawResult: Result<WithdrawalOutcome> = Result.failure(IllegalStateException("not set"))
    val agreeIds = mutableListOf<Int>()
    var agreeDeviceId: String? = null
    val withdrawCalls = mutableListOf<String>()

    override suspend fun fetchStatus(): Result<LegalStatus> = statusResult
    override suspend fun agree(documentIds: List<Int>, deviceId: String?): Result<AgreementOutcome> {
        agreeIds += documentIds
        agreeDeviceId = deviceId
        return agreeResult
    }
    override suspend fun withdraw(documentType: String): Result<WithdrawalOutcome> {
        withdrawCalls += documentType
        return withdrawResult
    }
}
