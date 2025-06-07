package com.efthemiosprime.pasabayan.ui.components.packages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView

/**
 * Package List View component matching iOS PackageListView
 * Mirrors iOS PackageListView.swift structure with clean package display
 * Handles package listing with proper empty state handling
 */
@Composable
fun PackageListView(
    title: String,
    packages: List<PackageRequest>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (packages.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inventory2,
                title = "No packages found",
                description = "There are no packages to display at the moment."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(packages.size) { index ->
                    PackageRequestCard(packageRequest = packages[index])
                }
            }
        }
    }
} 