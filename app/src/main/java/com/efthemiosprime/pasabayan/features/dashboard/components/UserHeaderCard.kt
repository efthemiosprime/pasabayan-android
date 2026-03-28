package com.efthemiosprime.pasabayan.features.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoSection
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole

@Composable
fun UserHeaderCard(
    userName: String,
    currentRole: UserRole,
    verificationLevel: String?,
    rating: String?,
    avatarUrl: String?,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_greeting, userName),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PUserInfoSection(
                    name = userName,
                    rating = rating,
                    verificationLevel = verificationLevel,
                )
            }
            IconButton(onClick = onSwitchRole) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = stringResource(R.string.dashboard_switch_role),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "UserHeader — light")
@Preview(showBackground = true, name = "UserHeader — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserHeaderCardPreview() {
    PasabayanTheme {
        UserHeaderCard(
            userName = "Bong Suyat",
            currentRole = UserRole.CARRIER,
            verificationLevel = "verified",
            rating = "4.8",
            avatarUrl = null,
            onSwitchRole = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
