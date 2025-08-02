package com.efthemiosprime.pasabayan.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.presentation.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.BookingUIState
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

/**
 * DirectBookingSheet - Exactly mirrors iOS DirectBookingSheet (427 lines)
 * Complete booking configuration with:
 * - Booking type selection (space_only, full_service, passenger)
 * - Requirements input based on booking type
 * - Location configuration
 * - Price negotiation and special requirements
 * - Form validation and submission
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectBookingSheet(
    trip: Trip,
    onBookingComplete: (DirectBookingData) -> Unit,
    onDismiss: () -> Unit,
    viewModel: BrowseTripsViewModel? = null,
    modifier: Modifier = Modifier
) {
    var selectedBookingType by remember { mutableStateOf(BookingType.SPACE_ONLY) }
    var spaceNeeded by remember { mutableStateOf("") }
    var weightNeeded by remember { mutableStateOf("") }
    var passengerCount by remember { mutableStateOf("1") }
    var pickupLocation by remember { mutableStateOf("") }
    var deliveryLocation by remember { mutableStateOf("") }
    var priceAgreed by remember { mutableStateOf("") }
    var serviceFee by remember { mutableStateOf("") }
    var specialRequirements by remember { mutableStateOf("") }
    
    var showingConfirmation by remember { mutableStateOf(false) }
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Observe ViewModel booking state for real API responses
    val bookingState by (viewModel?.bookingState?.collectAsStateWithLifecycle(BookingUIState.Idle) ?: 
        flowOf(BookingUIState.Idle).collectAsStateWithLifecycle(BookingUIState.Idle))
    
    // Form validation - mirrors iOS isFormValid
    val isFormValid = priceAgreed.isNotEmpty() && 
        priceAgreed.toDoubleOrNull() != null &&
        (selectedBookingType != BookingType.PASSENGER || 
         (passengerCount.isNotEmpty() && passengerCount.toIntOrNull() != null))
    
    // Calculated total - mirrors iOS calculatedTotal
    val calculatedTotal = (priceAgreed.toDoubleOrNull() ?: 0.0) + 
                         (serviceFee.toDoubleOrNull() ?: 0.0)
    
    // Handle booking state changes from real API responses
    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookingUIState.Loading -> {
                isBooking = true
                errorMessage = null
            }
            is BookingUIState.Success -> {
                isBooking = false
                errorMessage = null
                // Use REAL API response data - no more mock data!
                onBookingComplete(state.response.data)
                viewModel?.resetBookingState()
            }
            is BookingUIState.Error -> {
                isBooking = false
                errorMessage = state.message
                viewModel?.resetBookingState()
            }
            is BookingUIState.Idle -> {
                isBooking = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar - mirrors iOS navigation bar
                TopAppBar(
                    title = { 
                        Text(
                            text = "Book Trip",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = PasabayanDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
                ) {
                    item { Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md)) }
                    
                    // Trip Summary Section - mirrors iOS tripSummarySection
                    item {
                        TripSummarySection(trip = trip)
                    }
                    
                    // Booking Type Selection - mirrors iOS bookingTypeSection
                    item {
                        BookingTypeSection(
                            selectedBookingType = selectedBookingType,
                            onBookingTypeChange = { selectedBookingType = it }
                        )
                    }
                    
                    // Requirements Section - mirrors iOS requirementsSection
                    item {
                        RequirementsSection(
                            selectedBookingType = selectedBookingType,
                            spaceNeeded = spaceNeeded,
                            onSpaceNeededChange = { spaceNeeded = it },
                            weightNeeded = weightNeeded,
                            onWeightNeededChange = { weightNeeded = it },
                            passengerCount = passengerCount,
                            onPassengerCountChange = { passengerCount = it }
                        )
                    }
                    
                    // Location Section - mirrors iOS locationSection (for full service)
                    if (selectedBookingType == BookingType.FULL_SERVICE) {
                        item {
                            LocationSection(
                                pickupLocation = pickupLocation,
                                onPickupLocationChange = { pickupLocation = it },
                                deliveryLocation = deliveryLocation,
                                onDeliveryLocationChange = { deliveryLocation = it }
                            )
                        }
                    }
                    
                    // Pricing Section - mirrors iOS pricingSection
                    item {
                        PricingSection(
                            priceAgreed = priceAgreed,
                            onPriceAgreedChange = { priceAgreed = it },
                            serviceFee = serviceFee,
                            onServiceFeeChange = { serviceFee = it },
                            calculatedTotal = calculatedTotal
                        )
                    }
                    
                    // Special Requirements Section - mirrors iOS specialRequirementsSection
                    item {
                        SpecialRequirementsSection(
                            specialRequirements = specialRequirements,
                            onSpecialRequirementsChange = { specialRequirements = it }
                        )
                    }
                    
                    // Error Message
                    errorMessage?.let { error ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(PasabayanDesignSystem.Spacing.md),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Action Buttons Section - mirrors iOS actionButtonsSection
                    item {
                        ActionButtonsSection(
                            isFormValid = isFormValid,
                            isBooking = isBooking,
                            onBookTripClick = { showingConfirmation = true },
                            onCancelClick = onDismiss
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.xl)) }
                }
            }
        }
    }
    
    // Confirmation Dialog - mirrors iOS confirmation alert
    if (showingConfirmation) {
        AlertDialog(
            onDismissRequest = { showingConfirmation = false },
            title = { Text("Confirm Booking") },
            text = { 
                Text("Book this trip for CAD $${String.format("%.2f", calculatedTotal)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showingConfirmation = false
                        submitBooking(
                            trip = trip,
                            selectedBookingType = selectedBookingType,
                            spaceNeeded = spaceNeeded,
                            weightNeeded = weightNeeded,
                            passengerCount = passengerCount,
                            pickupLocation = pickupLocation,
                            deliveryLocation = deliveryLocation,
                            priceAgreed = priceAgreed,
                            serviceFee = serviceFee,
                            specialRequirements = specialRequirements,
                            viewModel = viewModel,
                            onBookingComplete = onBookingComplete,
                            onError = { errorMessage = it },
                            onLoadingChange = { isBooking = it }
                        )
                    }
                ) {
                    Text("Book Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showingConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Trip Summary Section - mirrors iOS tripSummarySection
 */
@Composable
private fun TripSummarySection(trip: Trip) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Trip Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                // Route display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.originCity,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "to",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.destinationCity,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Trip details
                TripDetailRow(
                    label = "Departure:", 
                    value = formatDate(trip.departureDate)
                )
                TripDetailRow(
                    label = "Available Space:", 
                    value = String.format("%.1f kg", trip.availableWeightKg)
                )
                if (trip.availableSpaceLiters > 0) {
                    TripDetailRow(
                        label = "Available Volume:", 
                        value = String.format("%.1f L", trip.availableSpaceLiters)
                    )
                }
            }
        }
    }
}

/**
 * Trip Detail Row helper component
 */
@Composable
private fun TripDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Booking Type Selection Section - mirrors iOS bookingTypeSection
 */
@Composable
private fun BookingTypeSection(
    selectedBookingType: BookingType,
    onBookingTypeChange: (BookingType) -> Unit
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Booking Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                BookingType.values().forEach { type ->
                    BookingTypeOption(
                        bookingType = type,
                        isSelected = selectedBookingType == type,
                        onSelect = { onBookingTypeChange(type) }
                    )
                }
            }
        }
    }
}

/**
 * Booking Type Option - mirrors iOS booking type button
 */
@Composable
private fun BookingTypeOption(
    bookingType: BookingType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.primary
            ) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = bookingType.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = bookingType.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = bookingType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}

/**
 * Requirements Section - mirrors iOS requirementsSection
 */
@Composable
private fun RequirementsSection(
    selectedBookingType: BookingType,
    spaceNeeded: String,
    onSpaceNeededChange: (String) -> Unit,
    weightNeeded: String,
    onWeightNeededChange: (String) -> Unit,
    passengerCount: String,
    onPassengerCountChange: (String) -> Unit
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Requirements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            when (selectedBookingType) {
                BookingType.PASSENGER -> {
                    OutlinedTextField(
                        value = passengerCount,
                        onValueChange = onPassengerCountChange,
                        label = { Text("Number of Passengers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
                    ) {
                        OutlinedTextField(
                            value = spaceNeeded,
                            onValueChange = onSpaceNeededChange,
                            label = { Text("Space Needed (Liters)") },
                            placeholder = { Text("Optional") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = weightNeeded,
                            onValueChange = onWeightNeededChange,
                            label = { Text("Weight Needed (kg)") },
                            placeholder = { Text("Optional") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * Location Section - mirrors iOS locationSection
 */
@Composable
private fun LocationSection(
    pickupLocation: String,
    onPickupLocationChange: (String) -> Unit,
    deliveryLocation: String,
    onDeliveryLocationChange: (String) -> Unit
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = onPickupLocationChange,
                    label = { Text("Pickup Location") },
                    placeholder = { Text("Enter pickup address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = deliveryLocation,
                    onValueChange = onDeliveryLocationChange,
                    label = { Text("Delivery Location") },
                    placeholder = { Text("Enter delivery address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * Pricing Section - mirrors iOS pricingSection
 */
@Composable
private fun PricingSection(
    priceAgreed: String,
    onPriceAgreedChange: (String) -> Unit,
    serviceFee: String,
    onServiceFeeChange: (String) -> Unit,
    calculatedTotal: Double
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Pricing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                OutlinedTextField(
                    value = priceAgreed,
                    onValueChange = onPriceAgreedChange,
                    label = { Text("Agreed Price (CAD)") },
                    placeholder = { Text("Enter agreed price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = serviceFee,
                    onValueChange = onServiceFeeChange,
                    label = { Text("Service Fee (CAD)") },
                    placeholder = { Text("Optional additional fee") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (calculatedTotal > 0) {
                    HorizontalDivider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("CAD $%.2f", calculatedTotal),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Special Requirements Section - mirrors iOS specialRequirementsSection
 */
@Composable
private fun SpecialRequirementsSection(
    specialRequirements: String,
    onSpecialRequirementsChange: (String) -> Unit
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Special Requirements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = specialRequirements,
                onValueChange = onSpecialRequirementsChange,
                label = { Text("Special instructions") },
                placeholder = { Text("Enter any special instructions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )
        }
    }
}

/**
 * Action Buttons Section - mirrors iOS actionButtonsSection
 */
@Composable
private fun ActionButtonsSection(
    isFormValid: Boolean,
    isBooking: Boolean,
    onBookTripClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
    ) {
        Button(
            onClick = onBookTripClick,
            enabled = isFormValid && !isBooking,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline
            )
        ) {
            if (isBooking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Booking...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "Book Trip",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        TextButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Submit Booking - REAL API call only, no mock data
 * Exactly mirrors iOS submitBooking behavior
 */
private fun submitBooking(
    trip: Trip,
    selectedBookingType: BookingType,
    spaceNeeded: String,
    weightNeeded: String,
    passengerCount: String,
    pickupLocation: String,
    deliveryLocation: String,
    priceAgreed: String,
    serviceFee: String,
    specialRequirements: String,
    viewModel: BrowseTripsViewModel?,
    onBookingComplete: (DirectBookingData) -> Unit,
    onError: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val bookingRequest = DirectBookingRequest(
        bookingType = selectedBookingType,
        spaceNeededLiters = spaceNeeded.toDoubleOrNull(),
        weightNeededKg = weightNeeded.toDoubleOrNull(),
        passengerCount = if (selectedBookingType == BookingType.PASSENGER) 
            passengerCount.toIntOrNull() else null,
        pickupLocation = pickupLocation.ifBlank { null },
        deliveryLocation = deliveryLocation.ifBlank { null },
        pickupLat = null, // Could be enhanced with location picker
        pickupLng = null,
        deliveryLat = null,
        deliveryLng = null,
        priceAgreed = priceAgreed.toDoubleOrNull() ?: 0.0,
        serviceFee = serviceFee.toDoubleOrNull(),
        specialRequirements = specialRequirements.ifBlank { null }
    )
    
    if (viewModel != null) {
        // REAL API CALL - response handled by LaunchedEffect observing bookingState
        viewModel.bookTripDirectly(trip.id.toString(), bookingRequest)
    } else {
        // Fallback error if no ViewModel
        onError("ViewModel not available for booking")
    }
}

/**
 * Date formatting helper - mirrors iOS formatDate
 */
private fun formatDate(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        outputFormat.format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }
}