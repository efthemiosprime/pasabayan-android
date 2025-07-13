package com.efthemiosprime.pasabayan.ui.screens.carrier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.efthemiosprime.pasabayan.data.model.TransportationMethod
import com.efthemiosprime.pasabayan.ui.screens.carrier.models.TripCreationEvent
import com.efthemiosprime.pasabayan.ui.screens.carrier.models.TripCreationUiState
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Trip Creation Screen - Full activity for creating trips
 * Following the same pattern as DeliveryRequestScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreationScreen(
    viewModel: TripCreationViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val actualViewModel: TripCreationViewModel = viewModel ?: viewModel { 
        TripCreationViewModel(context.applicationContext as android.app.Application) 
    }
    
    val uiState by actualViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Date picker states
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }
    val departureDatePickerState = rememberDatePickerState()
    val arrivalDatePickerState = rememberDatePickerState()
    
    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    // Handle events
    LaunchedEffect(Unit) {
        actualViewModel.uiEvent.collect { event ->
            when (event) {
                is TripCreationEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Dismiss"
                    )
                }
                is TripCreationEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "OK"
                    )
                }
                is TripCreationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is TripCreationEvent.ClearForm -> {
                    // Form cleared, no additional action needed
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Create New Trip",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        TripCreationContent(
            uiState = uiState,
            onOriginCityChange = actualViewModel::updateOriginCity,
            onDestinationCityChange = actualViewModel::updateDestinationCity,
            onTransportationMethodChange = actualViewModel::updateSelectedTransportationMethod,
            onDepartureDateChange = actualViewModel::updateDepartureDate,
            onDepartureTimeChange = actualViewModel::updateDepartureTime,
            onArrivalDateChange = actualViewModel::updateArrivalDate,
            onArrivalTimeChange = actualViewModel::updateArrivalTime,
            onAvailableWeightChange = actualViewModel::updateAvailableWeight,
            onAvailableSpaceChange = actualViewModel::updateAvailableSpace,
            onPricePerKgChange = actualViewModel::updatePricePerKg,
            onSpecialNotesChange = actualViewModel::updateSpecialNotes,
            onSubmit = actualViewModel::createTrip,
            onClearForm = actualViewModel::clearForm,
            isFormValid = actualViewModel.isFormValid,
            showDepartureDatePicker = showDepartureDatePicker,
            onShowDepartureDatePicker = { showDepartureDatePicker = it },
            showArrivalDatePicker = showArrivalDatePicker,
            onShowArrivalDatePicker = { showArrivalDatePicker = it },
            departureDatePickerState = departureDatePickerState,
            arrivalDatePickerState = arrivalDatePickerState,
            dateFormatter = dateFormatter,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripCreationContent(
    uiState: TripCreationUiState,
    onOriginCityChange: (String) -> Unit,
    onDestinationCityChange: (String) -> Unit,
    onTransportationMethodChange: (TransportationMethod?) -> Unit,
    onDepartureDateChange: (String) -> Unit,
    onDepartureTimeChange: (String) -> Unit,
    onArrivalDateChange: (String) -> Unit,
    onArrivalTimeChange: (String) -> Unit,
    onAvailableWeightChange: (String) -> Unit,
    onAvailableSpaceChange: (String) -> Unit,
    onPricePerKgChange: (String) -> Unit,
    onSpecialNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearForm: () -> Unit,
    isFormValid: Boolean,
    showDepartureDatePicker: Boolean,
    onShowDepartureDatePicker: (Boolean) -> Unit,
    showArrivalDatePicker: Boolean,
    onShowArrivalDatePicker: (Boolean) -> Unit,
    departureDatePickerState: DatePickerState,
    arrivalDatePickerState: DatePickerState,
    dateFormatter: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route Information Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Route Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Origin City
                    OutlinedTextField(
                        value = uiState.originCity,
                        onValueChange = onOriginCityChange,
                        label = { Text("Origin City") },
                        placeholder = { Text("e.g., Manila") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    
                    // Destination City
                    OutlinedTextField(
                        value = uiState.destinationCity,
                        onValueChange = onDestinationCityChange,
                        label = { Text("Destination City") },
                        placeholder = { Text("e.g., Cebu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    
                    // Transportation Method Dropdown
                    var showTransportationDropdown by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedTextField(
                            value = uiState.selectedTransportationMethod?.let { "${it.icon} ${it.displayName}" } ?: "",
                            onValueChange = { },
                            label = { Text("Transportation Method") },
                            placeholder = { Text("Select method") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (!uiState.isLoading) {
                                        showTransportationDropdown = true
                                    }
                                },
                            enabled = false,
                            readOnly = true
                        )
                        
                        DropdownMenu(
                            expanded = showTransportationDropdown,
                            onDismissRequest = { showTransportationDropdown = false }
                        ) {
                            TransportationMethod.values().forEach { method ->
                                DropdownMenuItem(
                                    text = { 
                                        Text("${method.icon} ${method.displayName}")
                                    },
                                    onClick = {
                                        onTransportationMethodChange(method)
                                        showTransportationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Schedule Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Departure Date with Date Picker
                    OutlinedTextField(
                        value = uiState.departureDate,
                        onValueChange = { },
                        label = { Text("Departure date") },
                        placeholder = { Text("Jul 12, 2025") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (!uiState.isLoading) onShowDepartureDatePicker(true) 
                            },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )
                    
                    // Departure Time
                    OutlinedTextField(
                        value = uiState.departureTime,
                        onValueChange = onDepartureTimeChange,
                        label = { Text("Departure time") },
                        placeholder = { Text("9:30 PM") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    
                    // Arrival Date with Date Picker
                    OutlinedTextField(
                        value = uiState.arrivalDate,
                        onValueChange = { },
                        label = { Text("Arrival date") },
                        placeholder = { Text("Jul 13, 2025") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (!uiState.isLoading) onShowArrivalDatePicker(true) 
                            },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )
                    
                    // Arrival Time
                    OutlinedTextField(
                        value = uiState.arrivalTime,
                        onValueChange = onArrivalTimeChange,
                        label = { Text("Arrival time") },
                        placeholder = { Text("9:30 PM") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
        
        // Available Capacity Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Available Capacity",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Weight capacity (required)
                    OutlinedTextField(
                        value = uiState.availableWeight,
                        onValueChange = onAvailableWeightChange,
                        label = { Text("Weight capacity") },
                        placeholder = { Text("kg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !uiState.isLoading
                    )
                    
                    // Space capacity (optional)
                    OutlinedTextField(
                        value = uiState.availableSpace,
                        onValueChange = onAvailableSpaceChange,
                        label = { Text("Space capacity (optional)") },
                        placeholder = { Text("liters") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !uiState.isLoading
                    )
                    
                    // Price per kg
                    OutlinedTextField(
                        value = uiState.pricePerKg,
                        onValueChange = onPricePerKgChange,
                        label = { Text("Price per kg ($)") },
                        placeholder = { Text("e.g., 0.60") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
        
        // Special Notes Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Special Notes (Optional)
                    OutlinedTextField(
                        value = uiState.specialNotes,
                        onValueChange = onSpecialNotesChange,
                        label = { Text("Special Notes (Optional)") },
                        placeholder = { Text("Any additional information...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
        
        // Form Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Review & Submit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!isFormValid && !uiState.isLoading) {
                        Text(
                            text = "Please fill in all required fields to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearForm,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Form")
                        }
                        
                        Button(
                            onClick = onSubmit,
                            enabled = isFormValid && !uiState.isLoading,
                            modifier = Modifier.weight(2f)
                        ) {
                            if (uiState.isLoading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Creating...")
                                }
                            } else {
                                Text("Create Trip")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialogs
    if (showDepartureDatePicker) {
        DatePickerDialog(
            onDismissRequest = { onShowDepartureDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    departureDatePickerState.selectedDateMillis?.let { millis ->
                        onDepartureDateChange(dateFormatter.format(Date(millis)))
                    }
                    onShowDepartureDatePicker(false)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDepartureDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = departureDatePickerState)
        }
    }
    
    if (showArrivalDatePicker) {
        DatePickerDialog(
            onDismissRequest = { onShowArrivalDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    arrivalDatePickerState.selectedDateMillis?.let { millis ->
                        onArrivalDateChange(dateFormatter.format(Date(millis)))
                    }
                    onShowArrivalDatePicker(false)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowArrivalDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = arrivalDatePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TripCreationScreenPreview() {
    PasabayanTheme {
        TripCreationScreen()
    }
} 