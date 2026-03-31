package com.efthemiosprime.pasabayan.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar

data class CreationRequirementChipUi(
    val icon: ImageVector,
    val label: String,
    val isComplete: Boolean,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreationWizardScaffold(
    title: String,
    closeContentDescription: String,
    requiredStepsLabel: String,
    completedRequirements: Int,
    totalRequirements: Int,
    requirementChips: List<CreationRequirementChipUi>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    showPagination: Boolean = false,
    currentStep: Int = 0,
    totalSteps: Int = 0,
    footer: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closeContentDescription,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            CreationRequirementsPrompt(
                requiredStepsLabel = requiredStepsLabel,
                completedRequirements = completedRequirements,
                totalRequirements = totalRequirements,
                requirementChips = requirementChips,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
                ) {
                    if (showPagination && totalSteps > 0) {
                        CreationWizardPagination(
                            currentStep = currentStep,
                            totalSteps = totalSteps,
                            modifier = Modifier.padding(top = PasabayanSpacing.sm),
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        content(PaddingValues(horizontal = PasabayanSpacing.lg, vertical = PasabayanSpacing.md))
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface) {
                footer()
            }
        }
    }
}

@Composable
fun CreationWizardStepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h4,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = subtitle,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun CreationWizardPagination(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            Surface(
                modifier = Modifier.size(PasabayanSpacing.sm),
                shape = CircleShape,
                color = if (index == currentStep) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                },
            ) {}
            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
            }
        }
    }
}

@Composable
private fun CreationRequirementsPrompt(
    requiredStepsLabel: String,
    completedRequirements: Int,
    totalRequirements: Int,
    requirementChips: List<CreationRequirementChipUi>,
) {
    PCard(
        modifier = Modifier.padding(horizontal = PasabayanSpacing.lg, vertical = PasabayanSpacing.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = requiredStepsLabel,
                    style = PasabayanTextStyles.Caption.regular,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(PasabayanSpacing.sm))
                Text(
                    text = "$completedRequirements/$totalRequirements",
                    style = PasabayanTextStyles.Caption.small,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = if (completedRequirements == totalRequirements) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = MaterialTheme.shapes.large,
                        )
                        .padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
            ) {
                requirementChips.forEach { chip ->
                    CreationRequirementChip(chip)
                }
            }
        }
    }
}

@Composable
private fun CreationRequirementChip(chip: CreationRequirementChipUi) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Surface(
            color = if (chip.isComplete) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
        ) {
            Icon(
                imageVector = chip.icon,
                contentDescription = null,
                tint = if (chip.isComplete) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(PasabayanSpacing.sm),
            )
        }
        Text(
            text = chip.label,
            style = PasabayanTextStyles.Caption.small,
            color = if (chip.isComplete) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
