package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplatesStore
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class TripCreationSavedRoutesViewModelTest {

    @Test
    fun `refreshSavedRoutes loads routes from store`() {
        val store = mockk<SavedRouteTemplatesStore>()
        val saved = listOf(
            SavedRouteTemplate(
                startCountryCode = "CA",
                startLocation = "Toronto",
                endCountryCode = "CA",
                endLocation = "Montreal",
            ),
        )
        every { store.getAll() } returns saved

        val viewModel = TripCreationSavedRoutesViewModel(store)
        viewModel.refreshSavedRoutes()

        assertEquals(saved, viewModel.uiState.value.savedRoutes)
    }
}
