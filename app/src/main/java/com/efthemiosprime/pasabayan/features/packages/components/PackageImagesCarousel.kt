package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.packages.model.PackageImage

/**
 * Horizontally-scrolling carousel for a package's attached images.
 *
 * iOS parity: shipper `PackageDetailView` shows the same horizontal strip when
 * `images` is non-empty. When [isProcessing] is true (server returned
 * `images_processing = true`), a spinner overlays a placeholder while the
 * `pollForProcessedImages` loop refreshes URLs.
 *
 * Reusable by construction — no Hilt or VM dependencies.
 */
@Composable
fun PackageImagesCarousel(
    images: List<PackageImage>,
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty() && !isProcessing) return
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.packages_detail_images_title),
                style = PasabayanTextStyles.Body.large,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isProcessing) {
                Text(
                    text = stringResource(R.string.packages_detail_images_processing),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(PasabayanRadius.md)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
            if (images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    items(images, key = { it.id }) { image ->
                        AsyncImage(
                            model = image.url,
                            contentDescription = image.originalFilename,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(PasabayanRadius.md)),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Images — light")
@Preview(showBackground = true, name = "Images — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageImagesCarouselPreview() {
    PasabayanTheme {
        PackageImagesCarousel(
            images = listOf(
                PackageImage(1, 100, "/img/1", 0, "1.jpg", "https://example.com/1.jpg", null),
                PackageImage(2, 100, "/img/2", 1, "2.jpg", "https://example.com/2.jpg", null),
            ),
            isProcessing = false,
        )
    }
}

@Preview(showBackground = true, name = "Images processing — light")
@Composable
private fun PackageImagesProcessingPreview() {
    PasabayanTheme {
        PackageImagesCarousel(
            images = emptyList(),
            isProcessing = true,
        )
    }
}
