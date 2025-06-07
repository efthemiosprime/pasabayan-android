package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.ui.components.packages.PackageRequestCard
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.common.EmptyStateData

/**
 * Recent Activity Section - Mirrors iOS RecentActivitySection (35 lines)
 * Pure component with configuration-based empty state
 */
@Composable
fun RecentActivitySection(
    title: String,
    packageRequests: List<PackageRequest>,
    emptyStateConfig: EmptyStateData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (packageRequests.isEmpty()) {
            EmptyStateView(
                icon = emptyStateConfig.icon,
                title = emptyStateConfig.title,
                description = emptyStateConfig.description
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                packageRequests.forEach { packageRequest ->
                    PackageRequestCard(packageRequest = packageRequest)
                }
            }
        }
    }
} 