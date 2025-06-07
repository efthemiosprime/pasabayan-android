package com.efthemiosprime.pasabayan.ui.components.role

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Role Switcher View component matching iOS role switching behavior
 * Only switches to carrier when API confirms is_active_carrier: true
 * Provides visual feedback during role switching process
 */
@Composable
fun RoleSwitcherView(
    roleViewModel: RoleViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by roleViewModel.currentRole.collectAsState()
    val isLoading by roleViewModel.isLoading.collectAsState()
    val errorMessage by roleViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    // Initialize RoleViewModel with context if not already done
    LaunchedEffect(Unit) {
        roleViewModel.initialize(context)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { 
                    if (!isLoading) {
                        roleViewModel.switchRole(UserRole.SHIPPER)
                    }
                },
                label = { 
                    if (isLoading && currentRole == UserRole.SHIPPER) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Shipper")
                        }
                    } else {
                        Text("Shipper")
                    }
                },
                selected = currentRole == UserRole.SHIPPER,
                enabled = !isLoading,
                leadingIcon = if (currentRole == UserRole.SHIPPER && !isLoading) {
                    {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
            
            FilterChip(
                onClick = { 
                    if (!isLoading) {
                        roleViewModel.clearError() // Clear any previous errors
                        roleViewModel.switchRole(UserRole.CARRIER)
                    }
                },
                label = { 
                    if (isLoading && currentRole != UserRole.CARRIER) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Carrier")
                        }
                    } else {
                        Text("Carrier")
                    }
                },
                selected = currentRole == UserRole.CARRIER,
                enabled = !isLoading,
                leadingIcon = if (currentRole == UserRole.CARRIER && !isLoading) {
                    {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
        
        // Show error message if any
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// MARK: - Previews  
@Preview("Role Switcher Default")
@Composable
fun RoleSwitcherViewPreview() {
    PasabayanTheme {
        // Note: Preview shows static UI only - actual functionality requires ViewModel
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { },
                    label = { Text("Shipper") },
                    selected = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    onClick = { },
                    label = { Text("Carrier") },
                    selected = false
                )
            }
        }
    }
} 