package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.DashboardTopBar
import com.efthemiosprime.pasabayan.features.dashboard.components.PasabayanBottomBar
import com.efthemiosprime.pasabayan.features.dashboard.model.MainTabs
import com.efthemiosprime.pasabayan.features.dashboard.model.DashboardSheetRoute
import com.efthemiosprime.pasabayan.features.dashboard.viewmodel.DashboardViewModel
import com.efthemiosprime.pasabayan.features.payments.ui.PaymentsProfileScreen
import com.efthemiosprime.pasabayan.features.profile.ui.AccountManagementSheet
import com.efthemiosprime.pasabayan.features.profile.ui.EditCarrierProfileSheet
import com.efthemiosprime.pasabayan.features.profile.ui.EditUserProfileSheet
import com.efthemiosprime.pasabayan.features.profile.ui.PrivacyPreferencesSheet
import com.efthemiosprime.pasabayan.features.profile.ui.ProfileTabScreen
import com.efthemiosprime.pasabayan.features.profile.ui.SettingsScreen
import com.efthemiosprime.pasabayan.features.profile.viewmodel.DisclaimerSyncViewModel
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierInfoJson
import com.efthemiosprime.pasabayan.features.favorites.ui.FavoritesListScreen
import com.efthemiosprime.pasabayan.features.favorites.ui.SendRequestSheet
import com.efthemiosprime.pasabayan.features.ratings.ui.RatingsScreen
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.ui.PhoneVerificationSheet
import com.efthemiosprime.pasabayan.features.verification.ui.PremiumVerificationSheet
import com.efthemiosprime.pasabayan.features.verification.ui.VerifyPhonePromptSheet
import com.efthemiosprime.pasabayan.features.chat.ui.MessagesTabScreen
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsViewModel
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.MatchingViewModel
import com.efthemiosprime.pasabayan.features.notifications.services.ActionableItemIds
import com.efthemiosprime.pasabayan.features.notifications.services.NavigationEvent
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRouter
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRouterEntryPoint
import com.efthemiosprime.pasabayan.features.notifications.services.buildActionableItemSpecs
import com.efthemiosprime.pasabayan.features.notifications.ui.ActionableItem
import com.efthemiosprime.pasabayan.features.notifications.ui.ComprehensiveNotificationsScreen
import com.efthemiosprime.pasabayan.features.notifications.viewmodel.NotificationViewModel
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import com.efthemiosprime.pasabayan.features.packages.components.CreatePackageOptionsSheet
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.ui.CarrierPackageDetailSheet
import com.efthemiosprime.pasabayan.features.packages.ui.EditPackageSheet
import com.efthemiosprime.pasabayan.features.packages.ui.PackageErrandRequestScreen
import com.efthemiosprime.pasabayan.features.packages.ui.PackageDetailScreen
import com.efthemiosprime.pasabayan.features.packages.ui.PackageRequestScreen
import com.efthemiosprime.pasabayan.features.bookings.ui.RequestToCarrySheet
import com.efthemiosprime.pasabayan.features.legal.ui.LegalViewerSheet
import com.efthemiosprime.pasabayan.features.support.model.HelpArticle
import com.efthemiosprime.pasabayan.features.support.ui.ArticleDetailSheet
import com.efthemiosprime.pasabayan.features.support.ui.HelpCenterSheet
import com.efthemiosprime.pasabayan.features.support.ui.SupportTicketFormScreen
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageCreationAssistViewModel
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.ui.TripFilterSheet
import com.efthemiosprime.pasabayan.features.trips.ui.CreateTripFromPackageScreen
import com.efthemiosprime.pasabayan.features.trips.ui.EditTripSheet
import com.efthemiosprime.pasabayan.features.trips.ui.TripDetailsScreen
import com.efthemiosprime.pasabayan.features.trips.ui.TripCreationScreen
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CarrierTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CarrierPreferencesFormViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripCreationSavedRoutesViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripsLocalStateViewModel

/**
 * Main tabbed dashboard shell — replaces the Phase 1 placeholder.
 * 5 tabs per role, matching iOS tab order.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainTabScreen(
    user: AuthUser,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    // Pull the singleton router via Hilt EntryPoint so we don't have to prop-drill it through
    // AuthScreen / AppEntryContent. Behavior parity with iOS `NotificationCenter` observer.
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationRouter: NotificationRouter = remember(context) {
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationRouterEntryPoint::class.java,
        ).notificationRouter()
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val packageViewModel: PackageViewModel = hiltViewModel()
    val conversationsViewModel: ConversationsViewModel = hiltViewModel()
    val conversationsState by conversationsViewModel.uiState.collectAsStateWithLifecycle()
    val packageCreationAssistViewModel: PackageCreationAssistViewModel = hiltViewModel()
    val packageCreationAssistState by packageCreationAssistViewModel.uiState.collectAsStateWithLifecycle()
    val browseTripsViewModel: BrowseTripsViewModel = hiltViewModel()
    val browseTripsState by browseTripsViewModel.uiState.collectAsStateWithLifecycle()
    val carrierPreferencesFormViewModel: CarrierPreferencesFormViewModel = hiltViewModel()
    val carrierTripsViewModel: CarrierTripsViewModel = hiltViewModel()
    val carrierTripsState by carrierTripsViewModel.uiState.collectAsStateWithLifecycle()
    val tripCreationSavedRoutesViewModel: TripCreationSavedRoutesViewModel = hiltViewModel()
    val tripCreationSavedRoutesState by tripCreationSavedRoutesViewModel.uiState.collectAsStateWithLifecycle()
    val tripsLocalStateViewModel: TripsLocalStateViewModel = hiltViewModel()
    val disclaimerSyncViewModel: DisclaimerSyncViewModel = hiltViewModel()
    val notificationsBootstrapViewModel: com.efthemiosprime.pasabayan.features.notifications.viewmodel.NotificationsBootstrapViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val notificationState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    // Profile-tab attention badge — same activity-scoped instance ProfileTabScreen consumes,
    // so the count stays in sync without an extra fetch.
    val profileAttentionViewModel: com.efthemiosprime.pasabayan.features.profile.viewmodel.ProfileAttentionViewModel =
        hiltViewModel()
    val profileAttention by profileAttentionViewModel.attention.collectAsStateWithLifecycle()
    var notificationsSheetOpen by remember { mutableStateOf(false) }
    // iOS parity (ActionableItem aggregation): pull matches + profile so we can compute
    // booking-request / status-update / pickup-ready / verify-prompt cards on the sheet.
    val matchingViewModel: MatchingViewModel = hiltViewModel()
    val matchingState by matchingViewModel.uiState.collectAsStateWithLifecycle()
    val profileTabViewModel: com.efthemiosprime.pasabayan.features.profile.viewmodel.ProfileTabViewModel = hiltViewModel()
    val profileState by profileTabViewModel.uiState.collectAsStateWithLifecycle()
    val packageUiState by packageViewModel.uiState.collectAsStateWithLifecycle()
    val tabs = MainTabs.forRole(state.currentRole)
    var showCreateOptionsSheet by remember { mutableStateOf(false) }
    var showPackageRequestSheet by remember { mutableStateOf(false) }
    var showPackageDisclaimerGate by remember { mutableStateOf(false) }
    var showErrandRequestSheet by remember { mutableStateOf(false) }
    var openErrandAfterDisclaimer by remember { mutableStateOf(false) }
    var showTripCreationSheet by remember { mutableStateOf(false) }
    var showCarrierPreferencesGate by remember { mutableStateOf(false) }
    var selectedCarrierTripId by remember { mutableStateOf<Int?>(null) }
    // iOS parity: when the user taps a chat pill inside trip details, jump to the Messages
    // tab and open that conversation directly.
    var pendingConversationId by remember { mutableStateOf<Int?>(null) }
    // iOS parity: same deep-link pattern for matches — a push tap or in-app notification card
    // for an `OpenMatch` event sets this; MatchListScreen consumes it.
    var pendingMatchId by remember { mutableStateOf<Int?>(null) }
    // Distinct surface from [pendingMatchId]: `OpenCounterOffer` jumps to the composer instead
    // of the details sheet so the user lands on the action they were notified about.
    var pendingCounterOfferMatchId by remember { mutableStateOf<Int?>(null) }
    // `OpenTransactionDetail(id)` deep-links into PaymentsProfileScreen → TransactionDetailScreen.
    var pendingTransactionId by remember { mutableStateOf<Int?>(null) }
    var profilePaymentsOpen by remember { mutableStateOf(false) }
    var profilePayoutSetupOpen by remember { mutableStateOf(false) }
    var showEditUserProfileSheet by remember { mutableStateOf(false) }
    var showEditCarrierProfileSheet by remember { mutableStateOf(false) }
    var showPrivacyPreferencesSheet by remember { mutableStateOf(false) }
    var showAccountManagementSheet by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var showPhoneVerificationSheet by remember { mutableStateOf(false) }
    var showPremiumVerificationSheet by remember { mutableStateOf(false) }
    // Local-source-of-truth for the phone-verification gate prompt. Set by UI short-circuits
    // (e.g. tapping "Create package" while unverified). VM-side guards in feature view-models
    // also surface a `requiresPhoneVerification` flag — we OR them in `gateReason` below.
    var verifyPhoneReason by remember { mutableStateOf<VerifyPhoneReason?>(null) }
    // Carrier flow: after "View Details → Request to Carry", track which package the carrier
    // intends to offer on. The submit currently no-ops (TODO: trip-picker + VM wiring).
    var carrierRequestPackage by remember { mutableStateOf<AvailablePackage?>(null) }
    // Profile menu → Support / Legal modal sheets (iOS HelpCenterView / TermsAndPrivacyView).
    var showHelpCenterSheet by remember { mutableStateOf(false) }
    var helpCenterArticle by remember { mutableStateOf<HelpArticle?>(null) }
    var showSupportTicketFormSheet by remember { mutableStateOf(false) }
    var showLegalViewerSheet by remember { mutableStateOf(false) }
    var favoritesOpen by remember { mutableStateOf(false) }
    var sendRequestCarrier by remember { mutableStateOf<FavoriteCarrierInfoJson?>(null) }
    var ratingsOpen by remember { mutableStateOf(false) }
    val dismissActiveSheetRoute = { viewModel.dismissActiveSheetRoute() }
    val openTripFilterSheet = { viewModel.openTripFilterSheet() }
    val openCreateTripFromPackageSheet: (Int) -> Unit = { packageId ->
        if (!user.phoneVerified) {
            verifyPhoneReason = VerifyPhoneReason.CreateTrip
        } else {
            viewModel.openCreateTripFromPackageSheet(packageId)
        }
    }
    val openCarrierTripEditor: (Int) -> Unit = { tripId ->
        selectedCarrierTripId = null
        viewModel.openEditTripSheet(tripId)
    }
    val createTripFromPackageRouteActions = remember(viewModel, carrierTripsViewModel, packageViewModel) {
        CreateTripFromPackageRouteActions(
            dismissRoute = dismissActiveSheetRoute,
            refreshCarrierTrips = { carrierTripsViewModel.refreshTrips() },
            refreshPackages = { packageViewModel.refreshPackages() },
        )
    }

    LaunchedEffect(user) {
        viewModel.initializeRole(user)
        tripsLocalStateViewModel.retryCarrierDisclaimerPendingSync(user.id)
        disclaimerSyncViewModel.bootstrapAndRetry(user.id)
        notificationsBootstrapViewModel.registerIfNeeded()
        // iOS parity: seed the bell badge with the current unread count on launch.
        notificationViewModel.loadUnreadCounts()
        // Pre-warm profile so the notifications-sheet verify-prompt reflects the real
        // verification level instead of the BASIC default before the user visits the
        // profile tab. Idempotent via `loadInFlight` CAS — `ProfileTabScreen`'s own
        // `LaunchedEffect` shares the in-flight flag, so no duplicate request fires.
        profileTabViewModel.loadTabData(user, state.currentRole)
    }

    // iOS parity: observe push-tap + in-app routing events and react by switching tabs and/or
    // opening the matching profile sub-screen. Per-id deep links (specific match / counter-offer
    // / transaction) currently land on the parent tab — refining those is a follow-up. Keyed on
    // the role so the closure re-resolves tab indices when the user switches role.
    LaunchedEffect(state.currentRole) {
        notificationRouter.events.collect { event ->
            // Close the notifications sheet if it was the source of the event so the
            // user lands on the destination instead of staring at the list.
            notificationsSheetOpen = false
            val matchesIndex = tabs.indexOfFirst { it.route == "matches" }
            val messagesIndex = tabs.indexOfFirst { it.route == "messages" }
            val profileIndex = tabs.indexOfFirst { it.route == "profile" }
            when (event) {
                NavigationEvent.OpenMatchesTab -> {
                    if (matchesIndex >= 0) viewModel.selectTab(matchesIndex)
                }
                is NavigationEvent.OpenMatch -> {
                    pendingMatchId = event.matchId
                    if (matchesIndex >= 0) viewModel.selectTab(matchesIndex)
                }
                is NavigationEvent.OpenCounterOffer -> {
                    pendingCounterOfferMatchId = event.matchId
                    if (matchesIndex >= 0) viewModel.selectTab(matchesIndex)
                }
                NavigationEvent.OpenConversations -> {
                    if (messagesIndex >= 0) viewModel.selectTab(messagesIndex)
                }
                is NavigationEvent.OpenConversation -> {
                    pendingConversationId = event.conversationId
                    if (messagesIndex >= 0) viewModel.selectTab(messagesIndex)
                }
                NavigationEvent.OpenTransactions,
                NavigationEvent.OpenPaymentMethods -> {
                    if (profileIndex >= 0) viewModel.selectTab(profileIndex)
                    profilePaymentsOpen = true
                }
                is NavigationEvent.OpenTransactionDetail -> {
                    pendingTransactionId = event.transactionId
                    if (profileIndex >= 0) viewModel.selectTab(profileIndex)
                    profilePaymentsOpen = true
                }
                NavigationEvent.OpenRatings -> {
                    if (profileIndex >= 0) viewModel.selectTab(profileIndex)
                    ratingsOpen = true
                }
                NavigationEvent.OpenProfileVerification -> {
                    if (profileIndex >= 0) viewModel.selectTab(profileIndex)
                }
            }
            // After routing, refresh the bell count so the badge clears down.
            notificationViewModel.loadUnreadCounts()
        }
    }

    LaunchedEffect(state.currentRole) {
        val roleFilter = when (state.currentRole) {
            UserRole.SHIPPER -> "shipper"
            UserRole.CARRIER -> "carrier"
        }
        conversationsViewModel.loadConversations(role = roleFilter)
        // Match list feeds actionable items (booking requests / status updates / responses).
        matchingViewModel.loadMatches(role = roleFilter)
    }

    LaunchedEffect(state.selectedTabIndex) {
        val tab = tabs.getOrNull(state.selectedTabIndex)
        if (tab?.route != "profile") {
            profilePaymentsOpen = false
            profilePayoutSetupOpen = false
            settingsOpen = false
            favoritesOpen = false
            ratingsOpen = false
        }
    }

    PScaffold(
        modifier = modifier,
        topBar = {
            DashboardTopBar(
                userName = user.name,
                currentRole = state.currentRole,
                onSwitchRole = { viewModel.switchRole() },
                verificationLevel = VerificationLevel.normalized(
                    profileState.userProfile?.verificationLevel,
                ),
                notificationsUnreadCount = notificationState.unreadCount,
                onOpenNotifications = { notificationsSheetOpen = true },
            )
        },
        bottomBar = {
            PasabayanBottomBar(
                tabs = tabs,
                selectedIndex = state.selectedTabIndex,
                onTabSelected = { viewModel.selectTab(it) },
                badgeCountByRoute = mapOf(
                    "messages" to conversationsState.allUnreadCount,
                    "profile" to profileAttention.total,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val currentTab = tabs.getOrNull(state.selectedTabIndex)
            when (currentTab?.route) {
                "explore" -> when (state.currentRole) {
                    UserRole.CARRIER ->
                        CarrierExploreContent(
                            user = user,
                            onSwitchRole = { viewModel.switchRole() },
                            onViewPackageDetails = { packageId ->
                                viewModel.openCarrierPackageDetailSheet(packageId)
                            },
                            onRequestToCarry = { packageId ->
                                packageUiState.availablePackages
                                    .firstOrNull { it.effectiveId == packageId }
                                    ?.let { carrierRequestPackage = it }
                            },
                            // Empty-state "Post Trip Schedule" → role-specific tab (index 2 = my_trips).
                            onNavigateToMyTrips = { viewModel.selectTab(2) },
                            packageViewModel = packageViewModel,
                        )
                    UserRole.SHIPPER ->
                        ShipperExploreContent(
                            user = user,
                            onSwitchRole = { viewModel.switchRole() },
                            onOpenTripFilter = openTripFilterSheet,
                            onPhoneVerificationRequired = { reason -> verifyPhoneReason = reason },
                            // Empty-state "Create Package" → role-specific tab (index 2 = packages).
                            onNavigateToPackages = { viewModel.selectTab(2) },
                            browseTripsViewModel = browseTripsViewModel,
                        )
                }
                "matches" -> com.efthemiosprime.pasabayan.features.bookings.ui.MatchListScreen(
                    isCarrier = state.currentRole == UserRole.CARRIER,
                    onAction = { action, matchId -> /* TODO: handle booking actions */ },
                    initialMatchId = pendingMatchId,
                    onInitialMatchConsumed = { pendingMatchId = null },
                    initialCounterOfferMatchId = pendingCounterOfferMatchId,
                    onInitialCounterOfferConsumed = { pendingCounterOfferMatchId = null },
                    onPhoneVerificationRequired = { reason -> verifyPhoneReason = reason },
                )
                "my_trips" -> com.efthemiosprime.pasabayan.features.trips.ui.CarrierMyTripsScreen(
                    onViewTripDetails = { trip -> selectedCarrierTripId = trip.id },
                    onEditTrip = { trip -> openCarrierTripEditor(trip.id) },
                    onCreateTrip = {
                        if (!user.phoneVerified) {
                            verifyPhoneReason = VerifyPhoneReason.CreateTrip
                        } else {
                            tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                            if (carrierPreferencesFormViewModel.isAcknowledged(user.id)) {
                                showTripCreationSheet = true
                            } else {
                                showCarrierPreferencesGate = true
                            }
                        }
                    },
                    viewModel = carrierTripsViewModel,
                )
                "packages" -> com.efthemiosprime.pasabayan.features.packages.ui.PackageListScreen(
                    onViewPackageDetails = { packageId ->
                        viewModel.openPackageDetailSheet(packageId)
                    },
                    onCreatePackage = {
                        if (!user.phoneVerified) {
                            verifyPhoneReason = VerifyPhoneReason.CreatePackage
                        } else {
                            packageCreationAssistViewModel.initialize(user.id)
                            showCreateOptionsSheet = true
                        }
                    },
                    onCreateTripFromPackage = openCreateTripFromPackageSheet,
                    onEditPackage = { packageId ->
                        viewModel.openEditPackageSheet(packageId)
                    },
                    onCancelPackage = { packageId ->
                        packageViewModel.cancelPackage(packageId)
                    },
                    viewModel = packageViewModel,
                )
                "messages" -> MessagesTabScreen(
                    currentUserId = user.id,
                    initialConversationId = pendingConversationId,
                    onInitialConversationConsumed = { pendingConversationId = null },
                )
                "profile" -> {
                    when {
                        ratingsOpen -> {
                            Column {
                                PTopBar(
                                    title = stringResource(R.string.ratings_title),
                                    navigationIcon = {
                                        IconButton(onClick = { ratingsOpen = false }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.ratings_back),
                                            )
                                        }
                                    },
                                )
                                RatingsScreen(
                                    userId = user.id.toInt(),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        favoritesOpen -> {
                            Column {
                                PTopBar(
                                    title = stringResource(R.string.favorites_title),
                                    navigationIcon = {
                                        IconButton(onClick = { favoritesOpen = false }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.favorites_back),
                                            )
                                        }
                                    },
                                )
                                FavoritesListScreen(
                                    onRequestDelivery = { carrier -> sendRequestCarrier = carrier },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        settingsOpen -> {
                            Column {
                                PTopBar(
                                    title = stringResource(R.string.profile_settings_title),
                                    navigationIcon = {
                                        IconButton(onClick = { settingsOpen = false }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.profile_settings_back),
                                            )
                                        }
                                    },
                                )
                                SettingsScreen(
                                    currentRole = state.currentRole,
                                    onOpenCarrierPreferences = { showEditCarrierProfileSheet = true },
                                    onOpenPrivacyPreferences = { showPrivacyPreferencesSheet = true },
                                    onOpenAccountManagement = { showAccountManagementSheet = true },
                                    onSignOut = onLogout,
                                    onOpenTerms = { showLegalViewerSheet = true },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        profilePayoutSetupOpen -> {
                            com.efthemiosprime.pasabayan.features.payments.ui.PayoutSetupScreen(
                                onClose = { profilePayoutSetupOpen = false },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        profilePaymentsOpen -> {
                            Column {
                                PTopBar(
                                    title = stringResource(R.string.payments_profile_title),
                                    navigationIcon = {
                                        IconButton(
                                            onClick = { profilePaymentsOpen = false },
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.profile_back_payments),
                                            )
                                        }
                                    },
                                )
                                PaymentsProfileScreen(
                                    onLogout = onLogout,
                                    modifier = Modifier.fillMaxSize(),
                                    onOpenPayoutSetup = {
                                        profilePaymentsOpen = false
                                        profilePayoutSetupOpen = true
                                    },
                                    initialTransactionId = pendingTransactionId,
                                    onInitialTransactionConsumed = { pendingTransactionId = null },
                                )
                            }
                        }
                        else -> ProfileTabScreen(
                            user = user,
                            currentRole = state.currentRole,
                            onSwitchRole = { viewModel.switchRole() },
                            onLogout = onLogout,
                            onOpenPaymentsHub = { profilePaymentsOpen = true },
                            onOpenPayoutSetup = { profilePayoutSetupOpen = true },
                            onOpenPersonalInfo = { showEditUserProfileSheet = true },
                            onOpenVehicleInfo = { showEditCarrierProfileSheet = true },
                            onOpenVerification = { showPhoneVerificationSheet = true },
                            onOpenPremiumVerification = { showPremiumVerificationSheet = true },
                            onOpenSettings = { settingsOpen = true },
                            onOpenAccountManagement = { showAccountManagementSheet = true },
                            onOpenFavorites = { favoritesOpen = true },
                            onOpenRatings = { ratingsOpen = true },
                            onOpenHelpCenter = { showHelpCenterSheet = true },
                            onOpenLegal = { showLegalViewerSheet = true },
                        )
                    }
                }
                else -> {}
            }
        }
    }

    if (showEditUserProfileSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showEditUserProfileSheet = false },
        ) {
            EditUserProfileSheet(
                authUser = user,
                onClose = { showEditUserProfileSheet = false },
            )
        }
    }

    if (showEditCarrierProfileSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showEditCarrierProfileSheet = false },
        ) {
            EditCarrierProfileSheet(
                onClose = { showEditCarrierProfileSheet = false },
            )
        }
    }

    if (showPrivacyPreferencesSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPrivacyPreferencesSheet = false },
        ) {
            PrivacyPreferencesSheet(
                onClose = { showPrivacyPreferencesSheet = false },
            )
        }
    }

    if (showAccountManagementSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showAccountManagementSheet = false },
        ) {
            AccountManagementSheet(
                onClose = { showAccountManagementSheet = false },
                onAccountDeletionRequested = {
                    showAccountManagementSheet = false
                    onLogout()
                },
            )
        }
    }

    // Profile menu → Help Center (iOS HelpCenterView). Landing page shows Popular
    // Articles (each opens its HTML via ArticleDetailSheet) and a Contact Support
    // section; the "Submit a Support Request" entry opens the existing ticket form.
    if (showHelpCenterSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showHelpCenterSheet = false },
        ) {
            HelpCenterSheet(
                onClose = { showHelpCenterSheet = false },
                onOpenArticle = { article -> helpCenterArticle = article },
                onOpenSupportTicketForm = { showSupportTicketFormSheet = true },
            )
        }
    }

    helpCenterArticle?.let { article ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { helpCenterArticle = null },
        ) {
            val languageCode = java.util.Locale.getDefault().language
            ArticleDetailSheet(
                title = stringResource(article.titleRes),
                htmlFilename = article.resolveHtmlFilename(languageCode = languageCode),
                onClose = { helpCenterArticle = null },
            )
        }
    }

    if (showSupportTicketFormSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showSupportTicketFormSheet = false },
        ) {
            SupportTicketFormScreen(
                onClose = { showSupportTicketFormSheet = false },
            )
        }
    }

    // Profile menu → Terms & Privacy read-only viewer (iOS TermsAndPrivacyView).
    if (showLegalViewerSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showLegalViewerSheet = false },
        ) {
            LegalViewerSheet()
        }
    }

    if (showPhoneVerificationSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPhoneVerificationSheet = false },
        ) {
            PhoneVerificationSheet(
                onClose = { showPhoneVerificationSheet = false },
                onUpgradeToPremium = {
                    showPhoneVerificationSheet = false
                    showPremiumVerificationSheet = true
                },
            )
        }
    }

    // Single owner of the phone-verification gate prompt. Reason can come from the local UI
    // short-circuit or a feature-VM `requiresPhoneVerification` flag — whichever fires first.
    val gateReason: VerifyPhoneReason? =
        packageUiState.requiresPhoneVerification ?: verifyPhoneReason
    if (gateReason != null) {
        VerifyPhonePromptSheet(
            reason = gateReason,
            onVerifyNow = {
                verifyPhoneReason = null
                packageViewModel.consumeRequiresPhoneVerification()
                showPhoneVerificationSheet = true
            },
            onDismiss = {
                verifyPhoneReason = null
                packageViewModel.consumeRequiresPhoneVerification()
            },
        )
    }

    if (showPremiumVerificationSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPremiumVerificationSheet = false },
        ) {
            PremiumVerificationSheet(
                onClose = { showPremiumVerificationSheet = false },
            )
        }
    }

    sendRequestCarrier?.let { carrier ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { sendRequestCarrier = null },
        ) {
            SendRequestSheet(
                carrierId = carrier.id,
                carrierName = carrier.name,
                onClose = { sendRequestCarrier = null },
            )
        }
    }

    if (showCreateOptionsSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showCreateOptionsSheet = false },
        ) {
            CreatePackageOptionsSheet(
                onClose = { showCreateOptionsSheet = false },
                onShipPackage = {
                    showCreateOptionsSheet = false
                    if (packageCreationAssistState.hasAcknowledgedDisclaimer) {
                        showPackageRequestSheet = true
                    } else {
                        openErrandAfterDisclaimer = false
                        showPackageDisclaimerGate = true
                    }
                },
                onErrandService = {
                    showCreateOptionsSheet = false
                    if (packageCreationAssistState.hasAcknowledgedDisclaimer) {
                        showErrandRequestSheet = true
                    } else {
                        openErrandAfterDisclaimer = true
                        showPackageDisclaimerGate = true
                    }
                },
            )
        }
    }

    if (showPackageRequestSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPackageRequestSheet = false },
        ) {
            PackageRequestScreen(
                onSave = { payload, imageUris ->
                    packageViewModel.createPackageRequest(
                        payload = payload,
                        imageUris = imageUris,
                    ) { result ->
                        if (result.isSuccess) {
                            packageCreationAssistViewModel.onPackageCreated(payload)
                            showPackageRequestSheet = false
                            packageViewModel.refreshPackages()
                        }
                    }
                },
                onCancel = { showPackageRequestSheet = false },
                savedDescriptions = packageCreationAssistState.savedDescriptions,
                savedPickupTemplates = packageCreationAssistState.savedPickupTemplates,
                savedHandoffTemplates = packageCreationAssistState.savedHandoffTemplates,
                showTutorialOverlay = packageCreationAssistState.showTutorial,
                onDismissTutorial = { packageCreationAssistViewModel.dismissTutorial(user.id) },
                isSubmitting = packageUiState.isSubmittingPackageRequest,
            )
        }
    }

    if (showPackageDisclaimerGate) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPackageDisclaimerGate = false },
        ) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold(
                title = stringResource(R.string.packages_disclaimer_title),
                closeContentDescription = stringResource(R.string.packages_create_close),
                onClose = { showPackageDisclaimerGate = false },
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.packages_disclaimer_body),
                )
                com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
                    text = stringResource(R.string.packages_disclaimer_acknowledge),
                    onClick = {
                        packageCreationAssistViewModel.acknowledgeDisclaimer(user.id)
                        showPackageDisclaimerGate = false
                        if (openErrandAfterDisclaimer) {
                            showErrandRequestSheet = true
                        } else {
                            showPackageRequestSheet = true
                        }
                        openErrandAfterDisclaimer = false
                    },
                )
            }
        }
    }

    if (showErrandRequestSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showErrandRequestSheet = false },
        ) {
            PackageErrandRequestScreen(
                onSave = { payload ->
                    packageViewModel.createServiceRequest(payload) { result ->
                        if (result.isSuccess) {
                            showErrandRequestSheet = false
                            packageViewModel.refreshPackages()
                        }
                    }
                },
                onCancel = { showErrandRequestSheet = false },
                isSubmitting = packageUiState.isSubmittingServiceRequest,
            )
        }
    }

    if (showCarrierPreferencesGate) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showCarrierPreferencesGate = false },
        ) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold(
                title = stringResource(R.string.trips_preferences_gate_title),
                closeContentDescription = stringResource(R.string.trips_detail_close),
                onClose = { showCarrierPreferencesGate = false },
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.trips_preferences_gate_body),
                )
                com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
                    text = stringResource(R.string.trips_preferences_gate_acknowledge),
                    onClick = {
                        carrierPreferencesFormViewModel.markAcknowledged(user.id)
                        tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                        showCarrierPreferencesGate = false
                        showTripCreationSheet = true
                    },
                )
            }
        }
    }

    if (showTripCreationSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showTripCreationSheet = false },
        ) {
            TripCreationScreen(
                userId = user.id,
                savedRoutes = tripCreationSavedRoutesState.savedRoutes,
                onTripCreated = {
                    showTripCreationSheet = false
                    tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                    carrierTripsViewModel.refreshTrips()
                },
                onCancel = { showTripCreationSheet = false },
            )
        }
    }

    if (state.activeSheetRoute is DashboardSheetRoute.TripFilter) {
        TripFilterSheet(
            filter = browseTripsState.filter,
            onDismiss = dismissActiveSheetRoute,
            onSearchChange = { browseTripsViewModel.updateSearchText(it) },
            onOriginChange = { browseTripsViewModel.updateOrigin(it) },
            onDestinationChange = { browseTripsViewModel.updateDestination(it) },
            onApply = {
                browseTripsViewModel.applyFilterAndFetch()
                dismissActiveSheetRoute()
            },
            onClear = {
                browseTripsViewModel.clearFilters()
                browseTripsViewModel.applyFilterAndFetch()
                dismissActiveSheetRoute()
            },
        )
    }

    val createFromPackageRoute = state.activeSheetRoute as? DashboardSheetRoute.CreateTripFromPackage
    createFromPackageRoute?.let { route ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = createTripFromPackageRouteActions::onClose,
        ) {
            CreateTripFromPackageScreen(
                packageId = route.packageId,
                userId = user.id,
                onClose = createTripFromPackageRouteActions::onClose,
                onTripCreated = createTripFromPackageRouteActions::onTripCreated,
            )
        }
    }

    val packageDetailRoute = state.activeSheetRoute as? DashboardSheetRoute.PackageDetail
    packageDetailRoute?.let { route ->
        LaunchedEffect(route.packageId) {
            packageViewModel.loadPackageDetail(route.packageId)
        }
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = {
                packageViewModel.clearPackageDetail()
                dismissActiveSheetRoute()
            },
        ) {
            val detail = packageUiState.selectedPackageDetail
            if (detail != null) {
                PackageDetailScreen(
                    pkg = detail,
                    onEdit = { viewModel.openEditPackageSheet(detail.id) },
                    onCancel = {
                        packageViewModel.cancelPackage(detail.id)
                        packageViewModel.clearPackageDetail()
                        dismissActiveSheetRoute()
                    },
                    onBack = {
                        packageViewModel.clearPackageDetail()
                        dismissActiveSheetRoute()
                    },
                )
            }
        }
    }

    val carrierPackageDetailRoute = state.activeSheetRoute as? DashboardSheetRoute.CarrierPackageDetail
    carrierPackageDetailRoute?.let { route ->
        val available = packageUiState.availablePackages.firstOrNull { it.effectiveId == route.packageId }
        if (available != null) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
                onDismissRequest = { dismissActiveSheetRoute() },
            ) {
                CarrierPackageDetailSheet(
                    pkg = available,
                    onRequestToCarry = {
                        dismissActiveSheetRoute()
                        carrierRequestPackage = available
                    },
                    onClose = { dismissActiveSheetRoute() },
                )
            }
        } else {
            // Package no longer in the available list (refresh, filter change) — dismiss silently.
            LaunchedEffect(route.packageId) { dismissActiveSheetRoute() }
        }
    }

    carrierRequestPackage?.let { _ ->
        RequestToCarrySheet(
            onSubmit = { _, _ ->
                // TODO: wire to MatchingViewModel.requestPackageAsCarrier once the trip-picker UI lands
                // (iOS RequestToCarrySheet asks the carrier to pick one of their trips, then calls
                // POST /matches/carrier-request with that tripId + packageId + offeredPrice).
                carrierRequestPackage = null
            },
            onDismiss = { carrierRequestPackage = null },
        )
    }

    val editPackageRoute = state.activeSheetRoute as? DashboardSheetRoute.EditPackage
    editPackageRoute?.let { route ->
        val detail = packageUiState.selectedPackageDetail
            ?: packageUiState.packageRequests.firstOrNull { it.id == route.packageId }
        detail?.let { pkg ->
            EditPackageSheet(
                pkg = pkg,
                onDismiss = dismissActiveSheetRoute,
                onSave = { request ->
                    packageViewModel.updatePackage(route.packageId, request)
                    packageViewModel.refreshPackages()
                    dismissActiveSheetRoute()
                },
            )
        }
    }

    val selectedCarrierTrip = selectedCarrierTripId?.let { id ->
        carrierTripsState.trips.firstOrNull { it.id == id }
    }
    selectedCarrierTrip?.let { trip ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { selectedCarrierTripId = null },
        ) {
            TripDetailsScreen(
                trip = trip,
                isCarrier = true,
                onEdit = { openCarrierTripEditor(trip.id) },
                onCancel = {
                    carrierTripsViewModel.cancelTrip(trip.id)
                    selectedCarrierTripId = null
                },
                onBack = { selectedCarrierTripId = null },
                onOpenChat = { conversationId ->
                    selectedCarrierTripId = null
                    pendingConversationId = conversationId
                    val messagesIndex = tabs.indexOfFirst { it.route == "messages" }
                    if (messagesIndex >= 0) viewModel.selectTab(messagesIndex)
                },
                onUpdateStatus = { targetStatus ->
                    carrierTripsViewModel.suspendUpdateTripStatus(trip.id, targetStatus)
                },
            )
        }
    }

    val editingCarrierTripId = (state.activeSheetRoute as? DashboardSheetRoute.EditTrip)?.tripId
    val editingCarrierTrip = editingCarrierTripId?.let { id ->
        carrierTripsState.trips.firstOrNull { it.id == id }
    }
    editingCarrierTrip?.let { trip ->
        EditTripSheet(
            trip = trip,
            onDismiss = dismissActiveSheetRoute,
            onSave = { availableWeightKg, notes ->
                carrierTripsViewModel.updateTripDetails(
                    tripId = trip.id,
                    availableWeightKg = availableWeightKg,
                    specialNotes = notes,
                )
                dismissActiveSheetRoute()
            },
        )
    }

    // iOS parity: full-screen notifications sheet launched from the top-bar bell. The screen
    // observes its own view-model; tapping a card emits a routing event picked up above.
    if (notificationsSheetOpen) {
        val role = when (state.currentRole) {
            UserRole.SHIPPER -> "shipper"
            UserRole.CARRIER -> "carrier"
        }
        val verificationLevel = VerificationLevel.normalized(
            profileState.userProfile?.verificationLevel,
        )
        val specs = remember(
            state.currentRole,
            carrierTripsState.trips,
            matchingState.matches,
            packageUiState.packageRequests,
            conversationsState.allUnreadCount,
            verificationLevel,
        ) {
            buildActionableItemSpecs(
                role = state.currentRole,
                carrierTrips = carrierTripsState.trips,
                matches = matchingState.matches,
                shipperPackages = packageUiState.packageRequests,
                unreadMessageCount = conversationsState.allUnreadCount,
                verificationLevel = verificationLevel,
            )
        }
        val actionableItems = specs.map { spec ->
            ActionableItem(
                id = spec.id,
                type = spec.type,
                title = actionableTitle(spec),
                description = actionableDescription(spec),
                onClick = {
                    handleActionableItemTap(
                        id = spec.id,
                        tabs = tabs,
                        selectTab = { viewModel.selectTab(it) },
                        closeSheet = { notificationsSheetOpen = false },
                        onSetCarrierTripFilter = { carrierTripsViewModel.setStatusFilter(it) },
                        onOpenPhoneVerification = { showPhoneVerificationSheet = true },
                        onOpenPremiumVerification = { showPremiumVerificationSheet = true },
                    )
                },
            )
        }
        ComprehensiveNotificationsScreen(
            onClose = { notificationsSheetOpen = false },
            role = role,
            actionableItems = actionableItems,
            viewModel = notificationViewModel,
        )
    }
}

@Composable
private fun actionableTitle(spec: com.efthemiosprime.pasabayan.features.notifications.services.ActionableItemSpec): String {
    return when (spec.id) {
        ActionableItemIds.BOOKING_REQUESTS -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_booking_requests_title, spec.count, spec.count,
        )
        ActionableItemIds.STATUS_UPDATES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_status_updates_title, spec.count, spec.count,
        )
        ActionableItemIds.INACTIVE_TRIPS -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_inactive_trips_title, spec.count, spec.count,
        )
        ActionableItemIds.CARRIER_RESPONSES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_carrier_responses_title, spec.count, spec.count,
        )
        ActionableItemIds.PICKUP_READY -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_pickup_ready_title, spec.count, spec.count,
        )
        ActionableItemIds.UNREAD_MESSAGES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_unread_messages_title, spec.count, spec.count,
        )
        ActionableItemIds.VERIFY_NUMBER -> stringResource(R.string.notifications_actionable_verify_number_title)
        ActionableItemIds.UPGRADE_PREMIUM -> stringResource(R.string.notifications_actionable_upgrade_premium_title)
        else -> ""
    }
}

@Composable
private fun actionableDescription(spec: com.efthemiosprime.pasabayan.features.notifications.services.ActionableItemSpec): String {
    return when (spec.id) {
        ActionableItemIds.BOOKING_REQUESTS -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_booking_requests_description, spec.count, spec.count,
        )
        ActionableItemIds.STATUS_UPDATES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_status_updates_description, spec.count, spec.count,
        )
        ActionableItemIds.INACTIVE_TRIPS -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_inactive_trips_description, spec.count, spec.count,
        )
        ActionableItemIds.CARRIER_RESPONSES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_carrier_responses_description, spec.count, spec.count,
        )
        ActionableItemIds.PICKUP_READY -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_pickup_ready_description, spec.count, spec.count,
        )
        ActionableItemIds.UNREAD_MESSAGES -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.notifications_actionable_unread_messages_description, spec.count, spec.count,
        )
        ActionableItemIds.VERIFY_NUMBER -> stringResource(R.string.notifications_actionable_verify_number_description)
        ActionableItemIds.UPGRADE_PREMIUM -> stringResource(R.string.notifications_actionable_upgrade_premium_description)
        else -> ""
    }
}

/**
 * iOS parity: each `ActionableItem.action` dismisses the sheet and either switches tabs (with
 * an optional filter) or opens a verification flow. Per-spec ids let us keep the dispatch logic
 * outside the aggregator (which stays pure).
 */
private fun handleActionableItemTap(
    id: String,
    tabs: List<com.efthemiosprime.pasabayan.features.dashboard.model.MainTab>,
    selectTab: (Int) -> Unit,
    closeSheet: () -> Unit,
    onSetCarrierTripFilter: (TripStatus?) -> Unit,
    onOpenPhoneVerification: () -> Unit,
    onOpenPremiumVerification: () -> Unit,
) {
    closeSheet()
    val matchesIndex = tabs.indexOfFirst { it.route == "matches" }
    val myTripsIndex = tabs.indexOfFirst { it.route == "my_trips" }
    val packagesIndex = tabs.indexOfFirst { it.route == "packages" }
    val messagesIndex = tabs.indexOfFirst { it.route == "messages" }
    when (id) {
        ActionableItemIds.BOOKING_REQUESTS,
        ActionableItemIds.STATUS_UPDATES,
        ActionableItemIds.CARRIER_RESPONSES -> {
            if (matchesIndex >= 0) selectTab(matchesIndex)
        }
        ActionableItemIds.INACTIVE_TRIPS -> {
            onSetCarrierTripFilter(TripStatus.PLANNING)
            if (myTripsIndex >= 0) selectTab(myTripsIndex)
        }
        ActionableItemIds.PICKUP_READY -> {
            if (packagesIndex >= 0) selectTab(packagesIndex)
        }
        ActionableItemIds.UNREAD_MESSAGES -> {
            if (messagesIndex >= 0) selectTab(messagesIndex)
        }
        ActionableItemIds.VERIFY_NUMBER -> onOpenPhoneVerification()
        ActionableItemIds.UPGRADE_PREMIUM -> onOpenPremiumVerification()
    }
}

