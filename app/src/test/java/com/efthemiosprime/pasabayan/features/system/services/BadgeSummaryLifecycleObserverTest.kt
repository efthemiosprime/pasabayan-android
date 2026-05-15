package com.efthemiosprime.pasabayan.features.system.services

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary
import com.efthemiosprime.pasabayan.features.profile.services.BadgeRefreshBus
import com.efthemiosprime.pasabayan.features.profile.services.BadgeSummaryRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Uses [UnconfinedTestDispatcher] so launched coroutines run eagerly, which
 * matches the `Dispatchers.Main.immediate` semantics that `repeatOnLifecycle`
 * and StateFlow collectors expect. With a `StandardTestDispatcher`, dispatcher
 * mismatch between the test scope and the main scope made these flows never
 * advance.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BadgeSummaryLifecycleObserverTest {

    private val mainDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: RecordingBadgeSummaryRepository
    private lateinit var refreshBus: BadgeRefreshBus
    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        repository = RecordingBadgeSummaryRepository()
        refreshBus = BadgeRefreshBus()
        authRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun observerWithLifecycle(
        initialState: Lifecycle.State = Lifecycle.State.INITIALIZED,
    ): Pair<BadgeSummaryLifecycleObserver, TestLifecycleOwner> {
        val owner = TestLifecycleOwner(initialState = initialState, coroutineDispatcher = mainDispatcher)
        val observer = BadgeSummaryLifecycleObserver(
            repository = repository,
            refreshBus = refreshBus,
            authRepository = authRepository,
            processLifecycle = owner.lifecycle,
        )
        return observer to owner
    }

    private fun CoroutineScope.startObserver(observer: BadgeSummaryLifecycleObserver) {
        observer.start(this)
    }

    @Test(timeout = 10_000)
    fun `ON_START transition triggers refresh with lastRequestedRole`() = runTest(mainDispatcher) {
        val (observer, owner) = observerWithLifecycle()
        repository.lastRequestedRole = UserRole.CARRIER
        backgroundScope.startObserver(observer)

        owner.currentState = Lifecycle.State.STARTED

        assertEquals(listOf(UserRole.CARRIER), refreshOnlyCalls())
    }

    @Test(timeout = 10_000)
    fun `re-entering STARTED state re-fires refresh`() = runTest(mainDispatcher) {
        val (observer, owner) = observerWithLifecycle()
        backgroundScope.startObserver(observer)

        owner.currentState = Lifecycle.State.STARTED
        owner.currentState = Lifecycle.State.CREATED
        owner.currentState = Lifecycle.State.STARTED

        assertEquals(2, refreshOnlyCalls().size)
    }

    @Test(timeout = 10_000)
    fun `auth user becoming non-null triggers refresh with lastRequestedRole`() = runTest(mainDispatcher) {
        val (observer, _) = observerWithLifecycle()
        repository.lastRequestedRole = UserRole.SHIPPER
        backgroundScope.startObserver(observer)

        authRepository.emit(authUser(id = 1L, phoneVerified = false))

        // The initial null emission triggers clear(); the first non-null emission
        // is the first refresh recorded.
        assertEquals(listOf(UserRole.SHIPPER), refreshOnlyCalls())
    }

    @Test(timeout = 10_000)
    fun `auth user becoming null triggers clear and no extra refresh`() = runTest(mainDispatcher) {
        val (observer, _) = observerWithLifecycle()
        backgroundScope.startObserver(observer)

        authRepository.emit(authUser(id = 1L, phoneVerified = false))
        val refreshCountBeforeLogout = repository.refreshCalls.size
        val clearCountBeforeLogout = repository.clearCount

        authRepository.emit(null)

        assertTrue(repository.clearCount > clearCountBeforeLogout)
        assertEquals(refreshCountBeforeLogout, repository.refreshCalls.size)
    }

    @Test(timeout = 10_000)
    fun `identical user re-emitted does not trigger another refresh`() = runTest(mainDispatcher) {
        val (observer, _) = observerWithLifecycle()
        backgroundScope.startObserver(observer)

        authRepository.emit(authUser(id = 7L, phoneVerified = true))
        val countAfterFirst = repository.refreshCalls.size

        authRepository.emit(authUser(id = 7L, phoneVerified = true))

        assertEquals(countAfterFirst, repository.refreshCalls.size)
    }

    @Test(timeout = 10_000)
    fun `phoneVerified flip on same user triggers refresh`() = runTest(mainDispatcher) {
        val (observer, _) = observerWithLifecycle()
        backgroundScope.startObserver(observer)

        authRepository.emit(authUser(id = 7L, phoneVerified = false))
        val countAfterFirst = repository.refreshCalls.size

        authRepository.emit(authUser(id = 7L, phoneVerified = true))

        assertEquals(countAfterFirst + 1, repository.refreshCalls.size)
    }

    @Test(timeout = 10_000)
    fun `bus emission triggers refresh with lastRequestedRole`() = runTest(mainDispatcher) {
        val (observer, _) = observerWithLifecycle()
        repository.lastRequestedRole = UserRole.CARRIER
        backgroundScope.startObserver(observer)
        val countAtStart = repository.refreshCalls.size

        refreshBus.emit()

        assertEquals(countAtStart + 1, repository.refreshCalls.size)
        assertEquals(UserRole.CARRIER, repository.refreshCalls.last())
    }

    /**
     * The initial `null` from `MutableStateFlow<AuthUser?>(null)` triggers
     * `repository.clear()` (not `refresh`). For "refresh"-only assertions we
     * filter to the calls that actually went through `refresh`.
     */
    private fun refreshOnlyCalls(): List<UserRole?> = repository.refreshCalls.toList()

    private fun authUser(id: Long, phoneVerified: Boolean): AuthUser = AuthUser(
        id = id,
        name = "User $id",
        email = "u$id@example.com",
        avatar = null,
        phone = null,
        phoneVerified = phoneVerified,
        profileCompleted = false,
        provider = "google",
        userTypes = emptyList(),
        isActiveCarrier = false,
        isActiveShipper = false,
    )
}

private class RecordingBadgeSummaryRepository : BadgeSummaryRepository {
    private val _summary = MutableStateFlow<BadgeSummary?>(null)
    private val _hasValidData = MutableStateFlow(false)
    private val _lastFetchedAt = MutableStateFlow<Instant?>(null)

    override val summary: StateFlow<BadgeSummary?> = _summary.asStateFlow()
    override val hasValidData: StateFlow<Boolean> = _hasValidData.asStateFlow()
    override val lastFetchedAt: StateFlow<Instant?> = _lastFetchedAt.asStateFlow()
    override var lastRequestedRole: UserRole? = null
    override var snapshotMode: Boolean = false

    val refreshCalls = mutableListOf<UserRole?>()
    var clearCount = 0

    override suspend fun refresh(role: UserRole?) {
        refreshCalls += role
    }

    override fun clear() {
        clearCount++
        _summary.value = null
        _hasValidData.value = false
        _lastFetchedAt.value = null
    }

    override fun seedForSnapshotTesting(summary: BadgeSummary?) {
        _summary.value = summary
        _hasValidData.value = summary != null
    }
}

private class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override fun currentUser(): StateFlow<AuthUser?> = _currentUser.asStateFlow()
    override fun clearCurrentUser() { _currentUser.value = null }

    fun emit(user: AuthUser?) { _currentUser.value = user }

    override suspend fun loginWithProviderAccessToken(provider: String, accessToken: String) =
        error("not used")
    override suspend fun loadCurrentUser() = error("not used")
    override suspend fun logout() = error("not used")
}
