package com.efthemiosprime.pasabayan.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.R

/**
 * Action item for [PCardActionFooter] overflow menu.
 */
data class CardMenuAction(
    val title: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Divider + "View Details" link + optional overflow menu.
 * Parity with iOS `CardActionFooter`.
 */
@Composable
fun PCardActionFooter(
    modifier: Modifier = Modifier,
    onViewDetails: (() -> Unit)? = null,
    menuActions: List<CardMenuAction> = emptyList(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onViewDetails != null) {
                Text(
                    text = stringResource(R.string.ds_view_details),
                    style = PasabayanTextStyles.Caption.large,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewDetails),
                )
            }

            if (menuActions.isNotEmpty()) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.ds_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        menuActions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.title) },
                                onClick = {
                                    expanded = false
                                    action.onClick()
                                },
                                enabled = action.enabled,
                                leadingIcon = action.icon?.let { icon ->
                                    {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
