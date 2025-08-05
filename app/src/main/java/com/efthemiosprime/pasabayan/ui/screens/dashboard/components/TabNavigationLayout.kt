package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Immutable configuration for tab items
 * Supports both ImageVector and Painter icons for flexibility
 */
@Immutable
data class TabItem(
    val title: String,
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val content: @Composable () -> Unit
) {
    init {
        require(icon != null || painter != null) {
            "TabItem must have either an icon (ImageVector) or painter (Painter)"
        }
    }
}

/**
 * Convenience constructor for ImageVector icons
 */
fun TabItem(title: String, icon: ImageVector, content: @Composable () -> Unit) = 
    TabItem(title = title, icon = icon, painter = null, content = content)

/**
 * Convenience constructor for Painter icons
 */
fun TabItem(title: String, painter: Painter, content: @Composable () -> Unit) = 
    TabItem(title = title, icon = null, painter = painter, content = content)

/**
 * Configuration for tab navigation with "More" support
 */
@Immutable
data class TabNavigationConfig(
    val visibleTabs: List<TabItem>,
    val moreTabs: List<TabItem>
)

/**
 * Pure tab navigation layout component with "More" tab support
 * Shows only first 4 tabs + "More" tab for additional items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabNavigationLayout(
    config: TabNavigationConfig,
    accentColor: Color,
    modifier: Modifier = Modifier,
    initialSelectedTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialSelectedTabIndex) }
    var selectedMoreTabIndex by remember { mutableIntStateOf(0) }
    val isMoreTabSelected = selectedTabIndex == config.visibleTabs.size
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // Show visible tabs
                config.visibleTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            when {
                                tab.icon != null -> Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                                tab.painter != null -> Icon(
                                    painter = tab.painter,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = { Text(tab.title) },
                        selected = selectedTabIndex == index && !isMoreTabSelected,
                        onClick = { 
                            selectedTabIndex = index
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor
                        )
                    )
                }
                
                // Show "More" tab if there are additional tabs
                if (config.moreTabs.isNotEmpty()) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More"
                            )
                        },
                        label = { Text("More") },
                        selected = isMoreTabSelected,
                        onClick = { 
                            selectedTabIndex = config.visibleTabs.size
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isMoreTabSelected -> {
                    // Show More screen with additional tabs
                    MoreTabScreen(
                        moreTabs = config.moreTabs,
                        selectedIndex = selectedMoreTabIndex,
                        onTabSelected = { index ->
                            selectedMoreTabIndex = index
                        },
                        accentColor = accentColor
                    )
                }
                selectedTabIndex < config.visibleTabs.size -> {
                    // Show selected visible tab content
                    config.visibleTabs[selectedTabIndex].content()
                }
            }
        }
    }
}

/**
 * More tab screen showing additional tabs in a list
 */
@Composable
private fun MoreTabScreen(
    moreTabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var showingTabContent by remember { mutableStateOf(false) }
    
    if (showingTabContent && selectedIndex < moreTabs.size) {
        // Show selected more tab content with back navigation
        Column(modifier = modifier.fillMaxSize()) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = { showingTabContent = false }
                ) {
                    Text("← Back to More")
                }
            }
            
            // Selected tab content
            Box(modifier = Modifier.fillMaxSize()) {
                moreTabs[selectedIndex].content()
            }
        }
    } else {
        // Show More tabs list
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(moreTabs.size) { index ->
                val tab = moreTabs[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onTabSelected(index)
                        showingTabContent = true
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when {
                            tab.icon != null -> Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = accentColor
                            )
                            tab.painter != null -> Icon(
                                painter = tab.painter,
                                contentDescription = tab.title,
                                tint = accentColor
                            )
                        }
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Legacy support - converts old tab list to new config format
 */
@Composable
fun TabNavigationLayout(
    tabs: List<TabItem>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    initialSelectedTabIndex: Int = 0
) {
    val config = remember(tabs) {
        TabNavigationConfig(
            visibleTabs = tabs,
            moreTabs = emptyList()
        )
    }
    
    TabNavigationLayout(
        config = config,
        accentColor = accentColor,
        modifier = modifier,
        initialSelectedTabIndex = initialSelectedTabIndex
    )
} 