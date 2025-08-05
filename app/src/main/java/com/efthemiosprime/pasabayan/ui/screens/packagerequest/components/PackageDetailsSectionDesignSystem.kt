package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.PackageType
import com.efthemiosprime.pasabayan.data.model.UrgencyLevel
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Package Details Section - Design System Compliant
 * Uses light background container instead of card, following Pasabayan design system
 */
@Composable
fun PackageDetailsSectionDesignSystem(
    packageDescription: String,
    onPackageDescriptionChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    packageValue: String,
    onPackageValueChange: (String) -> Unit,
    maxBudget: String,
    onMaxBudgetChange: (String) -> Unit,
    packageSize: PackageSize,
    onPackageSizeChange: (PackageSize) -> Unit,
    packageType: PackageType,
    onPackageTypeChange: (PackageType) -> Unit,
    urgencyLevel: UrgencyLevel,
    onUrgencyLevelChange: (UrgencyLevel) -> Unit,
    packageLength: String,
    onPackageLengthChange: (String) -> Unit,
    packageWidth: String,
    onPackageWidthChange: (String) -> Unit,
    packageHeight: String,
    onPackageHeightChange: (String) -> Unit,
    isFragile: Boolean,
    onFragileChange: (Boolean) -> Unit,
    specialInstructions: String,
    onSpecialInstructionsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Design system compliant container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = PasabayanDesignSystem.Colors.surfaceVariant,
                shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.lg)
            )
            .padding(PasabayanDesignSystem.Spacing.cardPadding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.formFieldSpacing)
        ) {
            // Section title using design system typography
            Text(
                text = "Package Details",
                style = PasabayanDesignSystem.Typography.headingH3,
                color = PasabayanDesignSystem.Colors.onSurface
            )
            
            // Package description field
            PackageDescriptionField(
                value = packageDescription,
                onValueChange = onPackageDescriptionChange
            )
            
            // Package type and urgency level row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                PackageTypePicker(
                    selectedType = packageType,
                    onTypeSelected = onPackageTypeChange,
                    modifier = Modifier.weight(1f)
                )
                
                UrgencyLevelPicker(
                    selectedLevel = urgencyLevel,
                    onLevelSelected = onUrgencyLevelChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Weight and size row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                WeightInputField(
                    value = weight,
                    onValueChange = onWeightChange,
                    modifier = Modifier.weight(1f)
                )
                
                PackageSizePicker(
                    selectedSize = packageSize,
                    onSizeSelected = onPackageSizeChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Package dimensions section
            Text(
                text = "Package Dimensions (cm) - Optional",
                style = PasabayanDesignSystem.Typography.bodyLarge,
                color = PasabayanDesignSystem.Colors.onSurface
            )
            
            Text(
                text = "Leave empty for smart defaults (30×20×15 cm)",
                style = PasabayanDesignSystem.Typography.caption,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            )
            
            // Dimensions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                DimensionInputField(
                    value = packageLength,
                    onValueChange = onPackageLengthChange,
                    label = "Length",
                    modifier = Modifier.weight(1f)
                )
                
                DimensionInputField(
                    value = packageWidth,
                    onValueChange = onPackageWidthChange,
                    label = "Width",
                    modifier = Modifier.weight(1f)
                )
                
                DimensionInputField(
                    value = packageHeight,
                    onValueChange = onPackageHeightChange,
                    label = "Height",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Package value and budget row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                PackageValueField(
                    value = packageValue,
                    onValueChange = onPackageValueChange,
                    modifier = Modifier.weight(1f)
                )
                
                MaxBudgetField(
                    value = maxBudget,
                    onValueChange = onMaxBudgetChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Fragile package toggle
            FragilityToggle(
                isFragile = isFragile,
                onFragileChange = onFragileChange
            )
            
            // Special instructions
            SpecialInstructionsField(
                value = specialInstructions,
                onValueChange = onSpecialInstructionsChange
            )
        }
    }
}

@Composable
private fun PackageDescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Package description *") },
        placeholder = { Text("Describe your package") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@Composable
private fun WeightInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Weight *") },
        placeholder = { Text("0.0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = { 
            Text(
                "kg", 
                style = PasabayanDesignSystem.Typography.bodySmall,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            ) 
        },
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@Composable
private fun PackageValueField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Package value") },
        placeholder = { Text("0.00") },
        leadingIcon = { 
            Text(
                "$", 
                style = PasabayanDesignSystem.Typography.bodyLarge,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            ) 
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@Composable
private fun MaxBudgetField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Max budget") },
        placeholder = { Text("0.00") },
        leadingIcon = { 
            Text(
                "$", 
                style = PasabayanDesignSystem.Typography.bodyLarge,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            ) 
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageSizePicker(
    selectedSize: PackageSize,
    onSizeSelected: (PackageSize) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSize.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Size") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PackageSize.entries.forEach { size ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = size.displayName,
                                style = PasabayanDesignSystem.Typography.bodyRegular
                            )
                            Text(
                                text = size.description,
                                style = PasabayanDesignSystem.Typography.caption,
                                color = PasabayanDesignSystem.Colors.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onSizeSelected(size)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FragilityToggle(
    isFragile: Boolean,
    onFragileChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Fragile package",
                style = PasabayanDesignSystem.Typography.bodyLarge,
                color = PasabayanDesignSystem.Colors.onSurface
            )
            Text(
                text = "Requires special handling",
                style = PasabayanDesignSystem.Typography.caption,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            )
        }
        
        Switch(
            checked = isFragile,
            onCheckedChange = onFragileChange
        )
    }
}

@Composable
private fun SpecialInstructionsField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Special instructions") },
        placeholder = { Text("Any special handling requirements") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@Composable
private fun DimensionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = { 
            Text(
                "cm", 
                style = PasabayanDesignSystem.Typography.bodySmall,
                color = PasabayanDesignSystem.Colors.onSurfaceVariant
            ) 
        },
        modifier = modifier,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageTypePicker(
    selectedType: PackageType,
    onTypeSelected: (PackageType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedType.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Package Type *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PackageType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = type.displayName,
                            style = PasabayanDesignSystem.Typography.bodyRegular
                        ) 
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrgencyLevelPicker(
    selectedLevel: UrgencyLevel,
    onLevelSelected: (UrgencyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLevel.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Urgency Level *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UrgencyLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = level.displayName,
                            style = PasabayanDesignSystem.Typography.bodyRegular
                        ) 
                    },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackageDetailsSectionDesignSystemPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PasabayanDesignSystem.Colors.screenBackground)
                .padding(PasabayanDesignSystem.Spacing.screenPadding)
        ) {
            PackageDetailsSectionDesignSystem(
                packageDescription = "Electronics and gadgets",
                onPackageDescriptionChange = { },
                weight = "2.5",
                onWeightChange = { },
                packageValue = "1080",
                onPackageValueChange = { },
                maxBudget = "500",
                onMaxBudgetChange = { },
                packageSize = PackageSize.MEDIUM,
                onPackageSizeChange = { },
                packageType = PackageType.ELECTRONICS,
                onPackageTypeChange = { },
                urgencyLevel = UrgencyLevel.NORMAL,
                onUrgencyLevelChange = { },
                packageLength = "30",
                onPackageLengthChange = { },
                packageWidth = "20",
                onPackageWidthChange = { },
                packageHeight = "15",
                onPackageHeightChange = { },
                isFragile = true,
                onFragileChange = { },
                specialInstructions = "Handle with care",
                onSpecialInstructionsChange = { }
            )
        }
    }
}