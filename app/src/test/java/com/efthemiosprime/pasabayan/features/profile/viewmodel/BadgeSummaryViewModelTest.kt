package com.efthemiosprime.pasabayan.features.profile.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import com.efthemiosprime.pasabayan.features.profile.services.BadgeSummaryRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeSummaryViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBadgeSummaryRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        repository = FakeBadgeSummaryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test(timeout = 10_000)
    fun `summary and hasValidData mirror the repository`() {
        val vm = BadgeSummaryViewModel(repository)

        assertNull(vm.summary.value)
        assertFalse(vm.hasValidData.value)

        repository.set(BadgeSummary(total = 7), valid = true)

        assertEquals(7, vm.summary.value?.total)
        assertTrue(vm.hasValidData.value)
    }

    @Test(timeout = 10_000)
    fun `refresh delegates to the repository with the given role`() = runTest(mainDispatcher) {
        val vm = BadgeSummaryViewModel(repository)

        vm.refresh(UserRole.CARRIER)

        assertEquals(listOf(UserRole.CARRIER), repository.refreshCalls)
    }

    @Test(timeout = 10_000)
    fun `refresh with null role passes null through`() = runTest(mainDispatcher) {
        val vm = BadgeSummaryViewModel(repository)

        vm.refresh(null)

        assertEquals(listOf<UserRole?>(null), repository.refreshCalls)
    }
}

private class FakeBadgeSummaryRepository : BadgeSummaryRepository {
    private val _summary = MutableStateFlow<BadgeSummary?>(null)
    private val _hasValidData = MutableStateFlow(false)
    private val _lastFetchedAt = MutableStateFlow<Instant?>(null)

    override val summary: StateFlow<BadgeSummary?> = _summary.asStateFlow()
    override val hasValidData: StateFlow<Boolean> = _hasValidData.asStateFlow()
    override val lastFetchedAt: StateFlow<Instant?> = _lastFetchedAt.asStateFlow()
    override var lastRequestedRole: UserRole? = null
    override var snapshotMode: Boolean = false

    val refreshCalls = mutableListOf<UserRole?>()

    fun set(summary: BadgeSummary?, valid: Boolean) {
        _summary.value = summary
        _hasValidData.value = valid
    }

    override suspend fun refresh(role: UserRole?) {
        lastRequestedRole = role
        refreshCalls += role
    }

    override fun clear() {
        _summary.value = null
        _hasValidData.value = false
        _lastFetchedAt.value = null
    }

    override fun seedForSnapshotTesting(summary: BadgeSummary?) {
        _summary.value = summary
        _hasValidData.value = summary != null
    }
}
