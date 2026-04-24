package com.efthemiosprime.pasabayan.features.packages.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.HandoffTemplate
import com.efthemiosprime.pasabayan.features.packages.services.PackageTutorialStore
import com.efthemiosprime.pasabayan.features.packages.services.PickupTemplate
import com.efthemiosprime.pasabayan.features.packages.services.SavedPackageDescriptionsStore
import com.efthemiosprime.pasabayan.features.packages.services.SavedPackageRouteTemplatesStore
import com.efthemiosprime.pasabayan.features.packages.services.ShipperDisclaimerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PackageCreationAssistUiState(
    val hasAcknowledgedDisclaimer: Boolean = false,
    val showTutorial: Boolean = false,
    val savedDescriptions: List<String> = emptyList(),
    val savedPickupTemplates: List<PickupTemplate> = emptyList(),
    val savedHandoffTemplates: List<HandoffTemplate> = emptyList(),
)

@HiltViewModel
class PackageCreationAssistViewModel @Inject constructor(
    private val disclaimerStore: ShipperDisclaimerStore,
    private val tutorialStore: PackageTutorialStore,
    private val descriptionsStore: SavedPackageDescriptionsStore,
    private val routeTemplatesStore: SavedPackageRouteTemplatesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackageCreationAssistUiState())
    val uiState: StateFlow<PackageCreationAssistUiState> = _uiState.asStateFlow()

    fun initialize(userId: Long) {
        _uiState.update {
            it.copy(
                hasAcknowledgedDisclaimer = disclaimerStore.hasAcknowledged(userId.toInt()),
                showTutorial = !tutorialStore.hasSeenTutorial(userId),
                savedDescriptions = descriptionsStore.getAll(),
                savedPickupTemplates = routeTemplatesStore.getPickupTemplates(),
                savedHandoffTemplates = routeTemplatesStore.getHandoffTemplates(),
            )
        }
    }

    fun acknowledgeDisclaimer(userId: Long) {
        disclaimerStore.setAcknowledged(userId.toInt())
        disclaimerStore.setPendingSync(userId.toInt(), pending = true)
        _uiState.update { it.copy(hasAcknowledgedDisclaimer = true) }
    }

    fun dismissTutorial(userId: Long) {
        tutorialStore.markTutorialSeen(userId)
        _uiState.update { it.copy(showTutorial = false) }
    }

    fun onPackageCreated(payload: PackageSubmitPayload) {
        payload.packageDescription?.let { descriptionsStore.save(it) }
        routeTemplatesStore.savePickupTemplate(
            PickupTemplate(
                pickupCountryCode = payload.pickupCountryCode,
                pickupCity = payload.pickupCity,
                pickupAddress = payload.pickupAddress,
            ),
        )
        routeTemplatesStore.saveHandoffTemplate(
            HandoffTemplate(
                deliveryCountryCode = payload.deliveryCountryCode,
                deliveryCity = payload.deliveryCity,
                deliveryAddress = payload.deliveryAddress,
            ),
        )
        _uiState.update {
            it.copy(
                savedDescriptions = descriptionsStore.getAll(),
                savedPickupTemplates = routeTemplatesStore.getPickupTemplates(),
                savedHandoffTemplates = routeTemplatesStore.getHandoffTemplates(),
            )
        }
    }
}
