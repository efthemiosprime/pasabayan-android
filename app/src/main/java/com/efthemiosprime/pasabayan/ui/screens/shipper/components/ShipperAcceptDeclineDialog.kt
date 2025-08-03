package com.efthemiosprime.pasabayan.ui.screens.shipper.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Accept/Decline Dialog for Shipper Carrier Requests
 * Mirrors iOS functionality with message input and decline reason selection for shippers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperAcceptDeclineDialog(
    request: DeliveryMatch,
    isAcceptMode: Boolean,
    onAccept: (String?) -> Unit,
    onDecline: (String?, String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { 
        mutableStateOf(
            if (isAcceptMode) {
                "Thank you for offering to carry my package! I accept your proposal and look forward to working with you."
            } else {
                "Thank you for your interest in carrying my package. Unfortunately, I have decided to go with another carrier at this time."
            }
        )
    }
    
    var selectedDeclineReason by remember { mutableStateOf("found_better_option") }
    
    val shipperDeclineReasons = listOf(
        "found_better_option" to "Found Better Option",
        "price_too_high" to "Price Too High", 
        "schedule_conflict" to "Schedule Conflict",
        "carrier_rating_low" to "Carrier Rating Concerns",
        "changed_mind" to "Changed Mind",
        "other" to "Other"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isAcceptMode) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isAcceptMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isAcceptMode) "Accept Carrier Request" else "Decline Carrier Request",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Carrier info
                PCardStandard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Carrier Request",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Carrier: ${request.carrier?.name ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Route: ${request.carrierTrip?.originCity ?: "Unknown"} → ${request.carrierTrip?.destinationCity ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Proposed Price:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "CAD ${String.format("%.2f", request.agreedPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Decline reason selection (only for decline mode)
                if (!isAcceptMode) {
                    PCardStandard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Decline Reason",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            shipperDeclineReasons.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedDeclineReason == value,
                                            onClick = { selectedDeclineReason = value }
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedDeclineReason == value,
                                        onClick = { selectedDeclineReason = value }
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Message input
                PCardStandard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Message to Carrier",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                Text("Enter a message for the carrier...") 
                            },
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        Text(
                            text = "Optional - you can leave this empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (isAcceptMode) {
                                onAccept(message.ifBlank { null })
                            } else {
                                onDecline(selectedDeclineReason, message.ifBlank { null })
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAcceptMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = if (isAcceptMode) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAcceptMode) "Accept" else "Decline")
                    }
                }
            }
        }
    }
}