package com.efthemiosprime.pasabayan.ui.screens.carrier.components

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
 * Accept/Decline Dialog for Carrier Shipper Requests
 * Mirrors iOS functionality with message input and decline reason selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptDeclineDialog(
    request: DeliveryMatch,
    isAcceptMode: Boolean,
    onAccept: (String) -> Unit,
    onDecline: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { 
        mutableStateOf(
            if (isAcceptMode) {
                "Hi! I am happy to help with your delivery. I will take good care of your package and deliver it safely. Thank you for choosing my service!"
            } else {
                "Thank you for your interest in my trip. Unfortunately, I do not have enough capacity for your package at this time. I apologize for any inconvenience."
            }
        )
    }
    
    var selectedDeclineReason by remember { mutableStateOf("insufficient_capacity") }
    
    val declineReasons = listOf(
        "insufficient_capacity" to "Insufficient Capacity",
        "schedule_conflict" to "Schedule Conflict", 
        "package_type_mismatch" to "Package Type Mismatch",
        "price_too_low" to "Price Too Low",
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
                        text = if (isAcceptMode) "Accept Request" else "Decline Request",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Package info
                PCardStandard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Package Request",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "From: ${request.packageRequest?.pickupLocation ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "To: ${request.packageRequest?.deliveryLocation ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Agreed Price:",
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
                            
                            declineReasons.forEach { (value, label) ->
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
                            text = "Message to Shipper",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                Text("Enter a message for the shipper...") 
                            },
                            minLines = 3,
                            maxLines = 5
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
                                onAccept(message)
                            } else {
                                onDecline(selectedDeclineReason, message)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAcceptMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ),
                        enabled = message.isNotBlank()
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