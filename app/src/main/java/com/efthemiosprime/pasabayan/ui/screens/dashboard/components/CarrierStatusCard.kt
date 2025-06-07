package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.ui.components.CarrierStatusCard as ExistingCarrierStatusCard

/**
 * Carrier Status Card - Pure wrapper component
 * Uses existing CarrierStatusCard with clean interface
 */
@Composable
fun CarrierStatusCard(
    viewModel: CarrierViewModel,
    modifier: Modifier = Modifier
) {
    ExistingCarrierStatusCard(
        carrierViewModel = viewModel,
        modifier = modifier
    )
} 