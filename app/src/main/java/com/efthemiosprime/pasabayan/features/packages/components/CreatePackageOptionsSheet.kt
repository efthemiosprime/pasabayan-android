package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider

@Composable
fun CreatePackageOptionsSheet(
    onClose: () -> Unit,
    onShipPackage: () -> Unit,
    onErrandService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.packages_create_options_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.packages_create_options_close),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onClose),
            )
        }

        PCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CreateOptionRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                title = androidx.compose.ui.res.stringResource(R.string.packages_create_options_ship_package_title),
                subtitle = androidx.compose.ui.res.stringResource(R.string.packages_create_options_ship_package_subtitle),
                titleColor = MaterialTheme.colorScheme.primary,
                onClick = onShipPackage,
            )

            PDivider(modifier = Modifier.padding(vertical = PasabayanSpacing.md))

            CreateOptionRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
                title = androidx.compose.ui.res.stringResource(R.string.packages_create_options_errand_service_title),
                subtitle = androidx.compose.ui.res.stringResource(R.string.packages_create_options_errand_service_subtitle),
                titleColor = MaterialTheme.colorScheme.onSurface,
                onClick = onErrandService,
            )
        }
    }
}

@Composable
private fun CreateOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h5,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(PasabayanSpacing.xs))
    }
}
