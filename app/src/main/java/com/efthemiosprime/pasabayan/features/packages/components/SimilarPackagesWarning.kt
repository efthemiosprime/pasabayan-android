package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard

/**
 * iOS-parity warning surfaced on the review step when the shipper already has
 * active packages with the same pickup and delivery cities. Advisory — not a
 * hard block.
 */
@Composable
internal fun SimilarPackagesWarning(
    count: Int,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = stringResource(R.string.packages_similar_warning_title),
                    style = PasabayanTextStyles.Body.large,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.packages_similar_warning_body, count),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "SimilarPackagesWarning — light")
@Preview(showBackground = true, name = "SimilarPackagesWarning — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimilarPackagesWarningPreview() {
    PasabayanTheme {
        SimilarPackagesWarning(count = 2)
    }
}
