package com.efthemiosprime.pasabayan.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.data.model.MatchCreationRequest
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.repository.DeliveryMatchRepositoryImpl
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import kotlinx.coroutines.launch

/**
 * MatchCreationScreen - Exact mirror of iOS MatchCreationView
 * 
 * Simple interface for shippers to:
 * 1. Select one of their packages from dropdown
 * 2. Enter proposed price
 * 3. Send match request
 * 
 * No booking types, no complex forms - just package selection + price + send
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCreationScreen(
    trip: Trip,
    onNavigateBack: () -> Unit,
    onMatchCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // ViewModels and repositories
    val packageViewModel: PackageViewModel = viewModel { PackageViewModel(context.applicationContext as android.app.Application) }
    val matchRepository = remember { DeliveryMatchRepositoryImpl.create(context) }
    
    // State
    val myPackages by packageViewModel.myPackageRequests.collectAsState()
    var selectedPackageId by remember { mutableStateOf<Int?>(null) }
    var proposedPrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessAlert by remember { mutableStateOf(false) }
    
    // Get pending packages (like iOS getPendingRequests())
    val pendingPackages = myPackages.filter { it.status == PackageRequestStatus.OPEN }
    
    // Load packages when screen appears
    LaunchedEffect(Unit) {
        packageViewModel.loadPackageRequests()
    }
    
    // Form validation (like iOS isFormValid)
    val isFormValid = selectedPackageId != null && 
                     proposedPrice.isNotBlank() && 
                     proposedPrice.toDoubleOrNull() != null &&
                     (proposedPrice.toDoubleOrNull() ?: 0.0) > 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Match") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(PasabayanDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            
            // Package Selection Section (like iOS packageSelectionSection)
            PCardStandard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
                ) {
                    Text(
                        text = "SELECT A PACKAGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (pendingPackages.isEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
                        ) {
                            Text(
                                text = "You have no pending package requests.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "To book this trip, you need to create a package request first. Go to the 'My Packages' tab and tap the '+' button to create one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Dropdown for package selection (like iOS Picker with MenuPickerStyle)
                        var expanded by remember { mutableStateOf(false) }
                        val selectedPackage = pendingPackages.find { it.id == selectedPackageId }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPackage?.title ?: "Select Package",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Your Packages") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                pendingPackages.forEach { pkg ->
                                    DropdownMenuItem(
                                        text = { Text(pkg.title) },
                                        onClick = {
                                            selectedPackageId = pkg.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Price Section (like iOS priceSection)
            PCardStandard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
                ) {
                    Text(
                        text = "PROPOSED PRICE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = proposedPrice,
                        onValueChange = { proposedPrice = it },
                        label = { Text("Enter price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter price") }
                    )
                }
            }
            
            // Error Section (like iOS errorSection)
            errorMessage?.let { error ->
                PCardStandard {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action Section (like iOS actionSection)
            Button(
                onClick = {
                    // Create match (like iOS createMatch())
                    val packageId = selectedPackageId
                    val price = proposedPrice.toDoubleOrNull()
                    
                    if (packageId != null && price != null) {
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            val request = MatchCreationRequest(
                                carrierTripId = trip.id,
                                packageRequestId = packageId,
                                agreedPrice = price
                            )
                            
                            val result = matchRepository.createMatch(request)
                            when (result) {
                                is com.efthemiosprime.pasabayan.data.common.Result.Success -> {
                                    isLoading = false
                                    showSuccessAlert = true
                                }
                                is com.efthemiosprime.pasabayan.data.common.Result.Failure -> {
                                    isLoading = false
                                    errorMessage = result.error.message
                                }
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading && pendingPackages.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Match Request")
                }
            }
        }
    }
    
    // Success Alert (like iOS alert in onChange)
    if (showSuccessAlert) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Request Sent Successfully!") },
            text = { Text("Your booking request has been sent to the carrier. You'll be notified once they respond.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessAlert = false
                        onMatchCreated()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}