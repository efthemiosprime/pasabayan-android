package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.components.role.RoleSwitcherView

/**
 * User Header Card - Mirrors iOS UserHeaderCard (51 lines)
 * Pure UI component with welcome message, avatar, and role switcher
 */
@Composable
fun UserHeaderCard(
    welcomeMessage: String,
    userName: String,
    userAvatar: String?,
    roleViewModel: RoleViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatarView(
                    avatarUrl = userAvatar,
                    size = 50.dp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = welcomeMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Role Switcher
            RoleSwitcherView(roleViewModel = roleViewModel)
        }
    }
}

/**
 * Pure avatar component with fallback handling
 */
@Composable
fun UserAvatarView(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = "User Avatar",
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
        error = painterResource(id = android.R.drawable.ic_menu_gallery),
        contentScale = ContentScale.Crop
    )
} 