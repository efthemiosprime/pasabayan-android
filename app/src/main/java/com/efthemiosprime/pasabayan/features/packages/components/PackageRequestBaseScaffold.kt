package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.shared.components.CreationRequirementChipUi
import com.efthemiosprime.pasabayan.shared.components.CreationWizardScaffold

typealias PackageRequirementChipUi = CreationRequirementChipUi

@Composable
fun PackageRequestBaseScaffold(
    title: String,
    completedRequirements: Int,
    totalRequirements: Int,
    requirementChips: List<PackageRequirementChipUi>,
    onClose: () -> Unit,
    showPagination: Boolean = false,
    currentStep: Int = 0,
    totalSteps: Int = 0,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    CreationWizardScaffold(
        title = title,
        closeContentDescription = stringResource(R.string.packages_create_close),
        requiredStepsLabel = stringResource(R.string.packages_create_required_steps),
        completedRequirements = completedRequirements,
        totalRequirements = totalRequirements,
        requirementChips = requirementChips,
        onClose = onClose,
        showPagination = showPagination,
        currentStep = currentStep,
        totalSteps = totalSteps,
        modifier = modifier,
        footer = footer,
        content = content,
    )
}
