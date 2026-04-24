package com.efthemiosprime.pasabayan.features.packages.viewmodel

import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.PackageTutorialStore
import com.efthemiosprime.pasabayan.features.packages.services.SavedPackageDescriptionsStore
import com.efthemiosprime.pasabayan.features.packages.services.SavedPackageRouteTemplatesStore
import com.efthemiosprime.pasabayan.features.packages.services.ShipperDisclaimerStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PackageCreationAssistViewModelTest {
    private lateinit var disclaimerStore: ShipperDisclaimerStore
    private lateinit var tutorialStore: PackageTutorialStore
    private lateinit var descriptionsStore: SavedPackageDescriptionsStore
    private lateinit var templatesStore: SavedPackageRouteTemplatesStore
    private lateinit var viewModel: PackageCreationAssistViewModel

    @Before
    fun setUp() {
        disclaimerStore = mockk(relaxed = true)
        tutorialStore = mockk(relaxed = true)
        descriptionsStore = mockk(relaxed = true)
        templatesStore = mockk(relaxed = true)
        every { descriptionsStore.getAll() } returns emptyList()
        every { templatesStore.getPickupTemplates() } returns emptyList()
        every { templatesStore.getHandoffTemplates() } returns emptyList()
        viewModel = PackageCreationAssistViewModel(
            disclaimerStore = disclaimerStore,
            tutorialStore = tutorialStore,
            descriptionsStore = descriptionsStore,
            routeTemplatesStore = templatesStore,
        )
    }

    @Test
    fun `initialize reflects disclaimer and tutorial flags`() {
        every { disclaimerStore.hasAcknowledged(7) } returns true
        every { tutorialStore.hasSeenTutorial(7L) } returns false

        viewModel.initialize(7L)
        val state = viewModel.uiState.value
        assertTrue(state.hasAcknowledgedDisclaimer)
        assertTrue(state.showTutorial)
    }

    @Test
    fun `dismissTutorial hides tutorial and persists seen flag`() {
        viewModel.dismissTutorial(9L)
        verify(exactly = 1) { tutorialStore.markTutorialSeen(9L) }
        assertFalse(viewModel.uiState.value.showTutorial)
    }

    @Test
    fun `onPackageCreated saves description and templates`() {
        viewModel.onPackageCreated(
            PackageSubmitPayload(
                pickupAddress = "123 Main",
                pickupCity = "Toronto",
                pickupCountryCode = "CA",
                deliveryAddress = "456 Oak",
                deliveryCity = "Montreal",
                deliveryCountryCode = "CA",
                packageWeightKg = 1.0,
                packageTypeCode = "general",
                fragile = false,
                urgencyLevelCode = "normal",
                pickupDatePreferred = LocalDate.now(),
                pickupTimePreferred = null,
                pickupDateFlexible = false,
                deliveryDateNeeded = LocalDate.now().plusDays(1),
                deliveryTimeNeeded = null,
                packageValue = null,
                packageDescription = "Books",
                maxPriceBudget = null,
                specialHandlingRequirements = null,
            ),
        )

        verify(exactly = 1) { descriptionsStore.save("Books") }
        verify(exactly = 1) { templatesStore.savePickupTemplate(any()) }
        verify(exactly = 1) { templatesStore.saveHandoffTemplate(any()) }
    }
}
