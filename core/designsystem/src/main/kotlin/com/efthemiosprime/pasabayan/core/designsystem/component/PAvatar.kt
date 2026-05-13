package com.efthemiosprime.pasabayan.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Circular user avatar — loads [url] via Coil 3, falls back to the first letter of
 * [fallbackName] on a primary-container circle when [url] is null/blank or while
 * loading. iOS parity with `UserProfileAvatar` in `UserProfileHeader.swift`.
 *
 * Pass [cacheBuster] to force a refresh after the avatar changes (e.g. immediately
 * after upload). Appended as a query parameter to the URL so identical-URL caches
 * invalidate. Mirrors iOS `UserProfileAvatar.cacheBustedURL` behavior.
 */
@Composable
fun PAvatar(
    url: String?,
    fallbackName: String?,
    modifier: Modifier = Modifier,
    size: Dp = DefaultAvatarSize,
    cacheBuster: String? = null,
) {
    val cacheBustedUrl = cacheBustedAvatarUrl(url, cacheBuster)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        // Always render the initial fallback under the image so it's visible
        // while loading and after a load failure (no extra error handling needed).
        AvatarInitialFallback(name = fallbackName, size = size)
        if (cacheBustedUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cacheBustedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
        }
    }
}

@Composable
private fun AvatarInitialFallback(name: String?, size: Dp) {
    val initial = name?.trim()?.firstOrNull()?.uppercase() ?: "?"
    val textStyle: TextStyle = when {
        size >= 80.dp -> PasabayanTextStyles.Heading.h2
        size >= 56.dp -> PasabayanTextStyles.Heading.h3
        size >= 40.dp -> PasabayanTextStyles.Heading.h5
        else -> PasabayanTextStyles.Heading.h6
    }
    Text(
        text = initial,
        style = textStyle,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

internal fun cacheBustedAvatarUrl(url: String?, cacheBuster: String?): String? {
    if (url.isNullOrBlank()) return null
    if (cacheBuster.isNullOrBlank()) return url
    val separator = if (url.contains("?")) "&" else "?"
    return "$url${separator}cb=$cacheBuster"
}

private val DefaultAvatarSize = 72.dp

@Preview(showBackground = true, name = "PAvatar — light")
@Preview(showBackground = true, name = "PAvatar — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PAvatarPreview() {
    PasabayanTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            modifier = Modifier.padding(PasabayanSpacing.md),
        ) {
            PAvatar(url = null, fallbackName = "Alex Carrier", size = 72.dp)
            PAvatar(url = null, fallbackName = "B", size = 56.dp)
            PAvatar(url = null, fallbackName = null, size = 40.dp)
        }
    }
}
