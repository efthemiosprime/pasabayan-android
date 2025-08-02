package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.components.TripStatusBadge
import com.efthemiosprime.pasabayan.ui.components.BadgeVariant

/**
 * TripCard component exactly matching iOS TripCard.swift
 * Displays trip information in a card format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCard(
    trip: Trip,
    onTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCancelled = trip.tripStatus == TripStatus.CANCELLED
    val cardBackgroundColor = if (isCancelled) {
        Color(0xFFF5F5F5) // Light gray background for cancelled trips
    } else {
        Color.White
    }
    val contentAlpha = if (isCancelled) 0.6f else 1.0f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCancelled) 1.dp else 2.dp, // Reduced elevation for cancelled trips
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTap?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(contentAlpha), // Apply alpha for cancelled trips
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with route and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trip.transportationMethod.icon,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = trip.route,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = trip.transportationMethod.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TripStatusBadge(
                    status = trip.tripStatus,
                    variant = BadgeVariant.COMPACT
                )
            }
            
            // Departure and arrival times
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trip_departure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.formattedDepartureDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trip_duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.formattedDuration,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Capacity and pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trip_available_capacity),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.formattedCapacity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trip_price),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.formattedPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50) // Green color for price
                        )
                    }
                }
            }
            
            // Special notes if available
            trip.specialNotes?.let { notes ->
                if (notes.isNotEmpty()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// MARK: - Preview
@Preview(showBackground = true, name = "Trip Card - Active")
@Composable
fun TripCardActivePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripCard(
                trip = Trip.getMockTrips()[0], // Active trip
                onTap = { println("Tapped active trip") }
            )
        }
    }
}

@Preview(showBackground = true, name = "Trip Card - Scheduled")
@Composable
fun TripCardScheduledPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripCard(
                trip = Trip.getMockTrips()[1], // Scheduled trip
                onTap = { println("Tapped scheduled trip") }
            )
        }
    }
}

@Preview(showBackground = true, name = "Trip Card - Completed")
@Composable
fun TripCardCompletedPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripCard(
                trip = Trip.getMockTrips()[2], // Completed trip
                onTap = { println("Tapped completed trip") }
            )
        }
    }
}

@Preview(showBackground = true, name = "Trip Cards - All Types")
@Composable
fun TripCardsAllTypesPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Trip.getMockTrips().take(4).forEach { trip ->
                TripCard(
                    trip = trip,
                    onTap = { println("Tapped trip: ${trip.route}") }
                )
            }
        }
    }
} 