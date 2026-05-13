package com.efthemiosprime.pasabayan.features.shipper.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PChip
import com.efthemiosprime.pasabayan.features.shipper.model.RecentSearchEntry

/**
 * Horizontal "Recent" chip row for shipper-explore — fallback for the Top Carriers
 * section when no carriers qualify but the user has saved searches. iOS parity:
 * `ShipperRecentSearchesSectionView` in `ShipperHomeContent.swift`.
 *
 * Visibility decision lives in [com.efthemiosprime.pasabayan.features.shipper.model.ShipperExploreSectionVisibility]
 * — this composable just renders what it's given. Each [PChip] tap fills the
 * search field with the entry's `displayName` and re-applies the browse filter.
 */
@Composable
fun RecentSearchesSection(
    entries: List<RecentSearchEntry>,
    onEntryTap: (RecentSearchEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(RECENT_SEARCHES_SECTION_TEST_TAG),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.dashboard_shipper_recent_searches_title),
            style = PasabayanTextStyles.Heading.h6,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            for (entry in entries) {
                PChip(
                    label = entry.displayName,
                    leadingIcon = Icons.Filled.History,
                    onClick = { onEntryTap(entry) },
                )
            }
        }
    }
}

internal const val RECENT_SEARCHES_SECTION_TEST_TAG = "Shipper.RecentSearchesSection"

@Preview(showBackground = true, name = "RecentSearches — light")
@Preview(showBackground = true, name = "RecentSearches — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecentSearchesSectionPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            RecentSearchesSection(
                entries = listOf(
                    RecentSearchEntry(city = "Toronto", country = "CA"),
                    RecentSearchEntry(city = "Vancouver", country = "CA"),
                    RecentSearchEntry(city = "Manila", country = "PH"),
                ),
                onEntryTap = {},
            )
        }
    }
}
