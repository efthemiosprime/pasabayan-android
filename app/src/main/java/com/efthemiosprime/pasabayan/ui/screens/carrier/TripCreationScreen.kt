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
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Trip Creation Screen - Full activity for creating trips
 * Following the same pattern as DeliveryRequestScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreationScreen(
    viewModel: TripCreationViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToTripsTab: () -> Unit = {}
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
    
    // Set minimum date to tomorrow to prevent confusion with same-day past times
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowMillis = tomorrow.timeInMillis
    
    val departureDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = tomorrowMillis, // Default to tomorrow
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= today.timeInMillis
            }
        }
    )
    val arrivalDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= today.timeInMillis
            }
        }
    )
    
    // Time picker states
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    var showArrivalTimePicker by remember { mutableStateOf(false) }
    val departureTimePickerState = rememberTimePickerState(
        initialHour = 18, // Default to 6:00 PM
        initialMinute = 0
    )
    val arrivalTimePickerState = rememberTimePickerState(
        initialHour = 20, // Default to 8:00 PM  
        initialMinute = 0
    )
    
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
                is TripCreationEvent.NavigateToTripsTab -> {
                    onNavigateToTripsTab()
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
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    // Check if this is a success message by looking at the action label
                    val isSuccess = snackbarData.visuals.actionLabel == "OK"
                    
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (isSuccess) {
                            Color(0xFF4CAF50) // Green for success
                        } else {
                            MaterialTheme.colorScheme.errorContainer // Default error color
                        },
                        contentColor = if (isSuccess) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        actionColor = if (isSuccess) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            )
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
            showDepartureTimePicker = showDepartureTimePicker,
            onShowDepartureTimePicker = { showDepartureTimePicker = it },
            showArrivalTimePicker = showArrivalTimePicker,
            onShowArrivalTimePicker = { showArrivalTimePicker = it },
            departureTimePickerState = departureTimePickerState,
            arrivalTimePickerState = arrivalTimePickerState,
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
    showDepartureTimePicker: Boolean,
    onShowDepartureTimePicker: (Boolean) -> Unit,
    showArrivalTimePicker: Boolean,
    onShowArrivalTimePicker: (Boolean) -> Unit,
    departureTimePickerState: TimePickerState,
    arrivalTimePickerState: TimePickerState,
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
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Route Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
        
        // Schedule Section
        item {
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Departure Time with Time Picker
                OutlinedTextField(
                    value = uiState.departureTime,
                    onValueChange = { },
                    label = { Text("Departure time") },
                    placeholder = { Text("9:30 PM") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (!uiState.isLoading) onShowDepartureTimePicker(true) 
                        },
                    singleLine = true,
                    enabled = false,
                    readOnly = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Arrival Time with Time Picker
                OutlinedTextField(
                    value = uiState.arrivalTime,
                    onValueChange = { },
                    label = { Text("Arrival time") },
                    placeholder = { Text("9:30 PM") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (!uiState.isLoading) onShowArrivalTimePicker(true) 
                        },
                    singleLine = true,
                    enabled = false,
                    readOnly = true
                )
            }
        }
        
        // Available Capacity Section
        item {
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Available Capacity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
        
        // Special Notes Section
        item {
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
        
        // Form Actions
        item {
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Review & Submit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (!isFormValid && !uiState.isLoading) {
                    Text(
                        text = "Please fill in all required fields to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
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
    
    // Date Picker Dialogs
    if (showDepartureDatePicker) {
        DatePickerDialog(
            onDismissRequest = { onShowDepartureDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    departureDatePickerState.selectedDateMillis?.let { millis ->
                        // Fix timezone issue: Use UTC zone to extract the correct date
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        // Format LocalDate directly to avoid timezone conversion issues
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                        onDepartureDateChange(localDate.format(formatter))
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
                        // Fix timezone issue: Use UTC zone to extract the correct date
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        // Format LocalDate directly to avoid timezone conversion issues
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                        onArrivalDateChange(localDate.format(formatter))
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
    
    // Time Picker Dialogs
    if (showDepartureTimePicker) {
        TimePickerDialog(
            onDismissRequest = { onShowDepartureTimePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = departureTimePickerState.hour
                    val minute = departureTimePickerState.minute
                    
                    // Convert 24-hour to 12-hour format
                    val displayHour = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val amPm = if (hour < 12) "AM" else "PM"
                    
                    val timeString = String.format("%02d:%02d %s", displayHour, minute, amPm)
                    
                    println("🕐 Time Picker Debug - Departure:")
                    println("   Selected hour (24h): $hour")
                    println("   Selected minute: $minute")
                    println("   Display hour (12h): $displayHour")
                    println("   AM/PM: $amPm")
                    println("   Final string: $timeString")
                    
                    onDepartureTimeChange(timeString)
                    onShowDepartureTimePicker(false)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDepartureTimePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = departureTimePickerState)
        }
    }
    
    if (showArrivalTimePicker) {
        TimePickerDialog(
            onDismissRequest = { onShowArrivalTimePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = arrivalTimePickerState.hour
                    val minute = arrivalTimePickerState.minute
                    
                    // Convert 24-hour to 12-hour format
                    val displayHour = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val amPm = if (hour < 12) "AM" else "PM"
                    
                    val timeString = String.format("%02d:%02d %s", displayHour, minute, amPm)
                    
                    println("🕐 Time Picker Debug - Arrival:")
                    println("   Selected hour (24h): $hour")
                    println("   Selected minute: $minute")
                    println("   Display hour (12h): $displayHour")
                    println("   AM/PM: $amPm")
                    println("   Final string: $timeString")
                    
                    onArrivalTimeChange(timeString)
                    onShowArrivalTimePicker(false)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowArrivalTimePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = arrivalTimePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            content()
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TripCreationScreenPreview() {
    PasabayanTheme {
        TripCreationScreen()
    }
} 