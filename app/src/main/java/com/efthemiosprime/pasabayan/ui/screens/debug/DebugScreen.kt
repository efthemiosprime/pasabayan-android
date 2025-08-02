package com.efthemiosprime.pasabayan.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.efthemiosprime.pasabayan.data.service.DebugUtils
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard

/**
 * Debug Screen for testing connectivity and diagnosing issues
 * Only visible in debug builds or when debugging is enabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isRunningDiagnostics by remember { mutableStateOf(false) }
    var diagnosticResults by remember { mutableStateOf<Map<String, Any>?>(null) }
    var quickTestResult by remember { mutableStateOf<String?>(null) }
    
    fun runQuickTest() {
        scope.launch {
            quickTestResult = "Testing..."
            quickTestResult = DebugUtils.quickConnectivityTest()
        }
    }
    
    fun runFullDiagnostics() {
        scope.launch {
            isRunningDiagnostics = true
            try {
                diagnosticResults = DebugUtils.runComprehensiveDiagnostics(context)
            } catch (e: Exception) {
                diagnosticResults = mapOf("error" to "Diagnostics failed: ${e.message}")
            }
            isRunningDiagnostics = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Debug & Diagnostics",
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
                },
                actions = {
                    IconButton(onClick = ::runQuickTest) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Quick Test"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Configuration
            item {
                PCardStandard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Current Configuration",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigurationItem("API Base URL", APIService.BASE_URL)
                    ConfigurationItem("Production URL", APIService.PRODUCTION_URL)
                    ConfigurationItem("Local URL", APIService.LOCAL_URL)
                    ConfigurationItem("Local Device URL", APIService.LOCAL_DEVICE_URL)
                }
            }
            
            // Quick Tests
            item {
                PCardStandard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Quick Tests",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = ::runQuickTest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Connectivity")
                        }
                        
                        Button(
                            onClick = ::runFullDiagnostics,
                            enabled = !isRunningDiagnostics,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isRunningDiagnostics) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Full Diagnostics")
                            }
                        }
                    }
                    
                    quickTestResult?.let { result ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = if (result.startsWith("✅")) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
            
            // Diagnostic Results
            diagnosticResults?.let { results ->
                item {
                    PCardStandard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Diagnostic Results",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        results.forEach { (category, data) ->
                            DiagnosticCategory(category, data)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            
            // Debugging Suggestions
            item {
                PCardStandard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Debugging Suggestions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val suggestions = DebugUtils.suggestAlternativeConfigurations()
                    suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigurationItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DiagnosticCategory(
    category: String,
    data: Any
) {
    Column {
        Text(
            text = category.replaceFirstChar { it.uppercaseChar() },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        when (data) {
            is Map<*, *> -> {
                data.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    ) {
                        Text(
                            text = "$key: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = when (value) {
                                true, "true" -> Color(0xFF4CAF50)
                                false, "false" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            else -> {
                Text(
                    text = data.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
} 