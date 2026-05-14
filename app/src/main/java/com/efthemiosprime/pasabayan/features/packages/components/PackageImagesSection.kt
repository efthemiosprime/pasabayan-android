package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * Step-1 photo-upload block on the shipper package-create wizard. Shows the
 * "Choose files" CTA plus a tips card. State-hoisted: the parent owns the
 * URIs list and the picker launcher.
 *
 * iOS parity: `Views/Components/PackageRequest/PackageImagesSection.swift`.
 */
@Composable
internal fun PackageImagesSection(
    selectedPhotoUris: List<Uri>,
    onPickPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        UploadCard(selectedPhotoUris = selectedPhotoUris, onPickPhotos = onPickPhotos)
        PhotoTipsCard()
    }
}

@Composable
private fun UploadCard(
    selectedPhotoUris: List<Uri>,
    onPickPhotos: () -> Unit,
) {
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(PasabayanSpacing.xxxl),
                )
                Text(
                    text = stringResource(R.string.packages_create_upload_photos_title),
                    style = PasabayanTextStyles.Heading.h5,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.packages_create_upload_photos_hint),
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PButton(
                    text = stringResource(R.string.packages_create_choose_files),
                    onClick = onPickPhotos,
                    size = PButtonSize.Small,
                )
                if (selectedPhotoUris.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.packages_create_selected_photos_count,
                            selectedPhotoUris.size,
                        ),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.packages_create_upload_formats),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PhotoTipsCard() {
    PCard {
        Text(
            text = stringResource(R.string.packages_create_photo_tips_title),
            style = PasabayanTextStyles.Heading.h6,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
        val tips = listOf(
            stringResource(R.string.packages_create_photo_tip_1),
            stringResource(R.string.packages_create_photo_tip_2),
            stringResource(R.string.packages_create_photo_tip_3),
            stringResource(R.string.packages_create_photo_tip_4),
        )
        tips.forEach { tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(PasabayanSpacing.sm),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {}
                Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
                Text(
                    text = tip,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "PackageImagesSection — light")
@Preview(showBackground = true, name = "PackageImagesSection — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageImagesSectionPreview() {
    PasabayanTheme {
        PackageImagesSection(selectedPhotoUris = emptyList(), onPickPhotos = {})
    }
}
