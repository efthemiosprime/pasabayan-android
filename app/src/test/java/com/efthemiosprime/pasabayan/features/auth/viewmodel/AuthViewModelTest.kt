package com.efthemiosprime.pasabayan.features.auth.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.session.TokenStore
import com.efthemiosprime.pasabayan.core.session.UnauthorizedSessionNotifier
import com.efthemiosprime.pasabayan.features.auth.services.FacebookLoginStarter
import com.efthemiosprime.pasabayan.features.auth.services.GoogleSignInHelper
import com.efthemiosprime.pasabayan.features.onboarding.viewmodel.FakeOnboardingPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var fakeTokenStore: FakeTokenStore
    private lateinit var fakeOnboardingPrefs: FakeOnboardingPreferences
    private lateinit var unauthorizedNotifier: UnauthorizedSessionNotifier
    private lateinit var mockContext: Context
    private lateinit var mockGoogleHelper: GoogleSignInHelper
    private lateinit var mockFacebookStarter: FacebookLoginStarter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepo = FakeAuthRepository()
        fakeTokenStore = FakeTokenStore()
        fakeOnboardingPrefs = FakeOnboardingPreferences()
        unauthorizedNotifier = UnauthorizedSessionNotifier()

        mockContext = mockk(relaxed = true) {
            every { applicationContext } returns this
            every { getString(any()) } returns "error"
            every { getString(any(), any()) } returns "error"
        }
        mockGoogleHelper = mockk(relaxed = true)
        mockFacebookStarter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel = AuthViewModel(
        appContext = mockContext,
        authRepository = fakeAuthRepo,
        tokenStore = fakeTokenStore,
        googleSignInHelper = mockGoogleHelper,
        facebookLoginStarter = mockFacebookStarter,
        onboardingPreferences = fakeOnboardingPrefs,
        unauthorizedSessionNotifier = unauthorizedNotifier,
        notificationLifecycleManager = noopLifecycleManager,
    )

    private val noopLifecycleManager =
        object : com.efthemiosprime.pasabayan.features.notifications.services.NotificationLifecycleManager {
            override fun onTokenRefreshed(token: String) {}
            override suspend fun registerStoredTokenIfAuthenticated() = null
            override suspend fun unregisterAndClear() = Result.success(Unit)
        }

    // ── refreshSession ──

    @Test
    fun `initial state is Checking then transitions to SignedOut when no token`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(SessionUiState.SignedOut, vm.uiState.value.session)
        assertNull(vm.uiState.value.citySetupPhase)
        assertNull(vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `refreshSession transitions to SignedIn when token present and loadCurrentUser succeeds`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.session is SessionUiState.SignedIn)
        assertEquals(testUser(), (state.session as SessionUiState.SignedIn).user)
    }

    @Test
    fun `refreshSession shows city NeedsSetup when city onboarding not done`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = false

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.NeedsSetup, vm.uiState.value.citySetupPhase)
        assertNull(vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `refreshSession shows city Complete and consent NeedsSetup when city done but consent not done`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = true
        fakeOnboardingPrefs.consentSetupCompleted = false

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.Complete, vm.uiState.value.citySetupPhase)
        assertEquals(ConsentSetupPhase.NeedsSetup, vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `refreshSession shows both Complete when city and consent done`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = true
        fakeOnboardingPrefs.consentSetupCompleted = true

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.Complete, vm.uiState.value.citySetupPhase)
        assertEquals(ConsentSetupPhase.Complete, vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `refreshSession transitions to SignedOut and sets error when loadCurrentUser fails`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.failure(RuntimeException("server down"))

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SessionUiState.SignedOut, state.session)
        assertNull(fakeTokenStore.getToken())
        verify { mockGoogleHelper.signOutGoogle() }
    }

    @Test
    fun `refreshSession sets transientError when loadCurrentUser fails`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.failure(RuntimeException("fail"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.transientError != null)
    }

    // ── logout ──

    @Test
    fun `logout clears session and signs out Google`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.session is SessionUiState.SignedIn)

        vm.logout()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SessionUiState.SignedOut, state.session)
        assertFalse(state.isBusy)
        assertNull(state.citySetupPhase)
        assertNull(state.consentSetupPhase)
        assertFalse(state.didJustCompleteConsent)
        verify { mockGoogleHelper.signOutGoogle() }
    }

    // ── onboarding gates ──

    @Test
    fun `markCityOnboardingComplete transitions city to Complete and shows consent NeedsSetup`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = false

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.NeedsSetup, vm.uiState.value.citySetupPhase)

        vm.markCityOnboardingComplete()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.Complete, vm.uiState.value.citySetupPhase)
        assertEquals(ConsentSetupPhase.NeedsSetup, vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `markCityOnboardingComplete skips consent NeedsSetup when consent already done`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = false
        fakeOnboardingPrefs.consentSetupCompleted = true

        val vm = createViewModel()
        advanceUntilIdle()

        vm.markCityOnboardingComplete()
        advanceUntilIdle()

        assertEquals(CitySetupPhase.Complete, vm.uiState.value.citySetupPhase)
        assertEquals(ConsentSetupPhase.Complete, vm.uiState.value.consentSetupPhase)
    }

    @Test
    fun `markConsentOnboardingComplete transitions consent to Complete and sets didJustCompleteConsent`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = true
        fakeOnboardingPrefs.consentSetupCompleted = false

        val vm = createViewModel()
        advanceUntilIdle()

        vm.markConsentOnboardingComplete()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ConsentSetupPhase.Complete, state.consentSetupPhase)
        assertTrue(state.didJustCompleteConsent)
    }

    @Test
    fun `consumeDidJustCompleteConsent resets flag to false`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())
        fakeOnboardingPrefs.citySetupCompleted = true
        fakeOnboardingPrefs.consentSetupCompleted = false

        val vm = createViewModel()
        advanceUntilIdle()

        vm.markConsentOnboardingComplete()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.didJustCompleteConsent)

        vm.consumeDidJustCompleteConsent()

        assertFalse(vm.uiState.value.didJustCompleteConsent)
    }

    // ── clearTransientError ──

    @Test
    fun `clearTransientError sets transientError to null`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.failure(RuntimeException("fail"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.transientError != null)

        vm.clearTransientError()

        assertNull(vm.uiState.value.transientError)
    }

    // ── unauthorized from network ──

    @Test
    fun `unauthorized network event forces SignedOut`() = runTest {
        fakeTokenStore.setToken("valid-token")
        fakeAuthRepo.loadCurrentUserResult = Result.success(testUser())

        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.session is SessionUiState.SignedIn)

        unauthorizedNotifier.notifyUnauthorized()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SessionUiState.SignedOut, state.session)
        assertFalse(state.isBusy)
        assertNull(state.citySetupPhase)
        assertNull(state.consentSetupPhase)
        assertTrue(state.transientError != null)
        verify { mockGoogleHelper.signOutGoogle() }
    }

    // ── Fakes ──

    private fun testUser(
        id: Long = 1L,
        name: String = "Test User",
        email: String = "test@example.com",
    ) = AuthUser(
        id = id,
        name = name,
        email = email,
        avatar = null,
        phone = null,
        phoneVerified = false,
        profileCompleted = false,
        provider = "google",
        userTypes = listOf("shipper"),
        isActiveCarrier = false,
        isActiveShipper = true,
    )
}

private class FakeAuthRepository : AuthRepository {
    var loginResult: Result<AuthUser> = Result.failure(RuntimeException("not set"))
    var loadCurrentUserResult: Result<AuthUser> = Result.failure(RuntimeException("not set"))
    var logoutResult: Result<Unit> = Result.success(Unit)

    override suspend fun loginWithProviderAccessToken(
        provider: String,
        accessToken: String,
    ): Result<AuthUser> = loginResult

    override suspend fun loadCurrentUser(): Result<AuthUser> = loadCurrentUserResult

    override suspend fun logout(): Result<Unit> = logoutResult
}

private class FakeTokenStore : TokenStore {
    private var token: String? = null
    override fun getToken(): String? = token
    override fun setToken(token: String?) { this.token = token }
    override fun clear() { token = null }
}

