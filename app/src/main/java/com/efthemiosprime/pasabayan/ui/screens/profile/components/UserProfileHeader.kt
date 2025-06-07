package com.efthemiosprime.pasabayan.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.ui.components.VerificationStatusDisplay

/**
 * User Profile Header Component
 * Displays user avatar, basic info, and role badge
 * Follows immutable data patterns
 */
@Composable
fun UserProfileHeader(
    user: User?,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left Column - Avatar and Verification Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserProfileAvatar(
                    avatarUrl = user?.avatar,
                    size = 100.dp
                )
                
                VerificationStatusDisplay(
                    verificationLevel = user?.verificationLevel ?: "unverified",
                    showText = false
                )
            }
            
            // Right Column - User Information
            UserInfoSection(
                name = user?.name ?: "User",
                email = user?.email ?: "",
                phone = user?.phone,
                currentRole = currentRole,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UserProfileAvatar(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = "Profile Picture",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
    )
}

@Composable
private fun UserInfoSection(
    name: String,
    email: String,
    phone: String?,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        phone?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        RoleBadge(role = currentRole)
    }
}

@Composable
private fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.wrapContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Blue.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (role == UserRole.CARRIER) Icons.Default.LocalShipping else Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Blue
            )
            Text(
                text = if (role == UserRole.CARRIER) "Carrier" else "Shipper",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Blue
            )
        }
    }
} 